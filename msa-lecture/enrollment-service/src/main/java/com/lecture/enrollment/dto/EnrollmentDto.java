package com.lecture.enrollment.dto;

import com.lecture.enrollment.entity.Enrollment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EnrollmentDto {

    // 수강신청 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollRequest {
        @NotNull(message = "수거 서비스 ID는 필수입니다")
        private Long collectionServiceId;
        @NotNull(message = "희망 수거일은 필수입니다")
        private LocalDate preferredCollectionDate;
        @NotNull(message = "희망 시작 시간은 필수입니다")
        private LocalTime preferredStartTime;
        @NotNull(message = "희망 종료 시간은 필수입니다")
        private LocalTime preferredEndTime;
        @jakarta.validation.constraints.NotBlank(message = "폐기물 정보는 필수입니다")
        private String wasteInformation;
        private String collectionRequirements;
    }

    // 강의 요약 정보 (내 수강 목록 표시용)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseSummary {
        private Long id;
        private String name;
        private String description;
        private String wasteType;
        private Integer price;
        private Long carrierId;
        private Integer requestCount;
    }

    // 수강 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentResponse {
        private Long id;
        private Long generatorId;
        private Long collectionServiceId;
        private LocalDate preferredCollectionDate;
        private LocalTime preferredStartTime;
        private LocalTime preferredEndTime;
        private String wasteInformation;
        private String collectionRequirements;
        private Enrollment.Status status;
        private LocalDateTime createdAt;

        // 추가
        private CourseSummary collectionService;

        public static EnrollmentResponse from(Enrollment enrollment) {
            return EnrollmentResponse.builder()
                    .id(enrollment.getId())
                    .generatorId(enrollment.getGeneratorId())
                    .collectionServiceId(enrollment.getCollectionServiceId())
                    .preferredCollectionDate(enrollment.getPreferredCollectionDate())
                    .preferredStartTime(enrollment.getPreferredStartTime())
                    .preferredEndTime(enrollment.getPreferredEndTime())
                    .wasteInformation(enrollment.getWasteInformation())
                    .collectionRequirements(enrollment.getCollectionRequirements())
                    .status(enrollment.getStatus())
                    .createdAt(enrollment.getCreatedAt())
                    .build();
        }

        public static EnrollmentResponse from(Enrollment enrollment, CourseSummary collectionService) {
            EnrollmentResponse response = from(enrollment);
            response.collectionService = collectionService;
            return response;
        }
    }

    // 추천 서비스용: 수강 이력 조회 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentHistoryResponse {
        private Long generatorId;
        private List<Long> confirmedCollectionServiceIds;
    }

    // 공통 API 응답 래퍼
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("성공")
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
