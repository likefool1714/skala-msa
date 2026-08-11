package com.lecture.enrollment.repository;

import com.lecture.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByGeneratorId(Long generatorId);

    List<Enrollment> findAllByOrderByCreatedAtDesc();

    List<Enrollment> findByGeneratorIdAndStatus(Long generatorId, Enrollment.Status status);

    Optional<Enrollment> findByIdAndGeneratorId(Long id, Long generatorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Enrollment e where e.id = :id and e.generatorId = :generatorId")
    Optional<Enrollment> findByIdAndGeneratorIdForUpdate(
            @Param("id") Long id, @Param("generatorId") Long generatorId);

    boolean existsByGeneratorIdAndCollectionServiceIdAndPreferredCollectionDateAndPreferredStartTime(
            Long generatorId, Long collectionServiceId, java.time.LocalDate date, java.time.LocalTime startTime);

    // 수강 완료(ACTIVE)된 강의 ID 목록 - 추천 서비스용
    List<Enrollment> findByGeneratorIdAndStatusIn(Long generatorId, List<Enrollment.Status> statuses);
}
