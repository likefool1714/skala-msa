package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.kafka.EnrollmentKafkaProducer;
import com.lecture.enrollment.kafka.KafkaEvent;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseServiceClient courseServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EnrollmentKafkaProducer kafkaProducer;
    private final EnrollmentWriteService enrollmentWriteService;

    /**
     * 수강신청 전체 흐름
     * 1. 강의 존재 확인
     * 2. 중복 수강 확인
     * 3. Enrollment 생성 및 즉시 커밋 (PENDING)
     * 4. 결제 요청
     */
    public EnrollmentDto.EnrollmentResponse reserve(Long generatorId, EnrollmentDto.EnrollRequest request) {
        Long collectionServiceId = request.getCollectionServiceId();
        if (!courseServiceClient.existsCourse(collectionServiceId)) {
            throw new IllegalArgumentException("존재하지 않는 수거 서비스입니다: " + collectionServiceId);
        }

        if (request.getPreferredCollectionDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜로 수거를 신청할 수 없습니다");
        }
        if (request.getPreferredEndTime().equals(request.getPreferredStartTime())) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 달라야 합니다");
        }
        if (enrollmentRepository.existsByGeneratorIdAndCollectionServiceIdAndPreferredCollectionDateAndPreferredStartTime(
                generatorId, collectionServiceId, request.getPreferredCollectionDate(), request.getPreferredStartTime())) {
            throw new IllegalArgumentException("같은 수거 서비스와 시간으로 이미 신청했습니다");
        }

        Map<String, Object> collectionService = courseServiceClient.getCourse(collectionServiceId);
        BigDecimal price = new BigDecimal(collectionService.get("price").toString());
        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(generatorId, request);

        paymentServiceClient.requestPayment(enrollment.getId(), generatorId, collectionServiceId, price);

        log.info("[CollectionRequestService] 수거 신청 완료 (결제 대기) - requestId: {}", enrollment.getId());
        return EnrollmentDto.EnrollmentResponse.from(enrollment);
    }

    /**
     * 수강 활성화
     */
    @Transactional
    public void confirmReservation(Long requestId, Long generatorId, Long collectionServiceId) {
        Enrollment enrollment = enrollmentRepository.findByIdAndGeneratorIdForUpdate(requestId, generatorId)
                .orElseThrow(() -> new IllegalArgumentException("수거 신청을 찾을 수 없습니다: " + requestId));

        if (!enrollment.confirmIfPending()) {
            log.info("[CollectionRequestService] 이미 처리된 결제 이벤트 무시 - requestId: {}, status: {}",
                    requestId, enrollment.getStatus());
            return;
        }

        courseServiceClient.increaseEnrollmentCount(collectionServiceId);

        kafkaProducer.publishEnrollmentCompleted(
                KafkaEvent.EnrollmentCompletedEvent.builder()
                        .collectionRequestId(enrollment.getId())
                        .generatorId(generatorId)
                        .collectionServiceId(collectionServiceId)
                        .build()
        );

        log.info("[CollectionRequestService] 수거 접수 확정 - requestId: {}", enrollment.getId());
    }

    /**
     * 사용자 수강 목록 조회
     * - course-service에서 강의 상세 정보를 붙여서 반환
     */
    public List<EnrollmentDto.EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByGeneratorId(userId);

        return enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> courseInfo = courseServiceClient.getCourse(enrollment.getCollectionServiceId());

                    EnrollmentDto.CourseSummary courseSummary = EnrollmentDto.CourseSummary.builder()
                            .id(toLong(courseInfo.get("id")))
                            .name((String) courseInfo.get("name"))
                            .description((String) courseInfo.get("description"))
                            .wasteType(normalizeCategory((String) courseInfo.get("wasteType")))
                            .price(toInteger(courseInfo.get("price")))
                            .carrierId(toLong(courseInfo.get("carrierId")))
                            .requestCount(toInteger(courseInfo.get("requestCount")))
                            .build();

                    return EnrollmentDto.EnrollmentResponse.from(enrollment, courseSummary);
                })
                .collect(Collectors.toList());
    }

    /**
     * 수집·운반 업체가 자신의 수거 서비스로 받은 신청을 조회한다.
     */
    public List<EnrollmentDto.EnrollmentResponse> getRequestsByCarrier(Long carrierId) {
        return enrollmentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(enrollment -> {
                    Map<String, Object> serviceInfo = courseServiceClient.getCourse(enrollment.getCollectionServiceId());
                    Long serviceCarrierId = toLong(serviceInfo.get("carrierId"));
                    if (!carrierId.equals(serviceCarrierId)) {
                        return null;
                    }
                    return EnrollmentDto.EnrollmentResponse.from(
                            enrollment, toCourseSummary(serviceInfo));
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentDto.EnrollmentResponse acceptRequest(Long requestId, Long carrierId) {
        Enrollment enrollment = getOwnedRequest(requestId, carrierId);
        enrollment.accept();
        log.info("[CollectionRequestService] 수거업체 접수 수락 - requestId: {}, carrierId: {}",
                requestId, carrierId);
        return EnrollmentDto.EnrollmentResponse.from(
                enrollment, toCourseSummary(courseServiceClient.getCourse(enrollment.getCollectionServiceId())));
    }

    @Transactional
    public EnrollmentDto.EnrollmentResponse rejectRequest(Long requestId, Long carrierId) {
        Enrollment enrollment = getOwnedRequest(requestId, carrierId);
        enrollment.reject();
        log.info("[CollectionRequestService] 수거업체 접수 거절 - requestId: {}, carrierId: {}",
                requestId, carrierId);
        return EnrollmentDto.EnrollmentResponse.from(
                enrollment, toCourseSummary(courseServiceClient.getCourse(enrollment.getCollectionServiceId())));
    }

    @Transactional
    public EnrollmentDto.EnrollmentResponse completeRequest(Long requestId, Long carrierId) {
        Enrollment enrollment = getOwnedRequest(requestId, carrierId);
        enrollment.complete();
        log.info("[CollectionRequestService] 수거 완료 - requestId: {}, carrierId: {}",
                requestId, carrierId);
        return EnrollmentDto.EnrollmentResponse.from(
                enrollment, toCourseSummary(courseServiceClient.getCourse(enrollment.getCollectionServiceId())));
    }

    /**
     * 수강 이력 조회 - 추천 서비스용
     */
    public EnrollmentDto.EnrollmentHistoryResponse getEnrollmentHistory(Long userId) {
        List<Long> activeCourseIds = enrollmentRepository
                .findByGeneratorIdAndStatusIn(userId, List.of(
                        Enrollment.Status.CONFIRMED,
                        Enrollment.Status.ACCEPTED,
                        Enrollment.Status.COMPLETED))
                .stream()
                .map(Enrollment::getCollectionServiceId)
                .collect(Collectors.toList());

        return EnrollmentDto.EnrollmentHistoryResponse.builder()
                .generatorId(userId)
                .confirmedCollectionServiceIds(activeCourseIds)
                .build();
    }

    private Enrollment getOwnedRequest(Long requestId, Long carrierId) {
        Enrollment enrollment = enrollmentRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("수거 신청을 찾을 수 없습니다: " + requestId));
        Map<String, Object> serviceInfo = courseServiceClient.getCourse(enrollment.getCollectionServiceId());
        if (!carrierId.equals(toLong(serviceInfo.get("carrierId")))) {
            throw new IllegalArgumentException("해당 수거 신청을 처리할 권한이 없습니다");
        }
        return enrollment;
    }

    private EnrollmentDto.CourseSummary toCourseSummary(Map<String, Object> serviceInfo) {
        return EnrollmentDto.CourseSummary.builder()
                .id(toLong(serviceInfo.get("id")))
                .name((String) serviceInfo.get("name"))
                .description((String) serviceInfo.get("description"))
                .wasteType(normalizeCategory((String) serviceInfo.get("wasteType")))
                .price(toInteger(serviceInfo.get("price")))
                .carrierId(toLong(serviceInfo.get("carrierId")))
                .requestCount(toInteger(serviceInfo.get("requestCount")))
                .build();
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;

        return switch (category) {
            case "GENERAL_MEDICAL" -> "일반의료폐기물";
            case "SHARPS" -> "손상성폐기물";
            case "PATHOLOGICAL" -> "조직물류폐기물";
            default -> category;
        };
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Object firstNonNullObject(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
