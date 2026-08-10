package com.lecture.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "collection_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"generator_id", "collection_service_id", "preferred_collection_date", "preferred_start_time"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generator_id", nullable = false)
    private Long generatorId;

    @Column(name = "collection_service_id", nullable = false)
    private Long collectionServiceId;

    @Column(name = "preferred_collection_date", nullable = false)
    private java.time.LocalDate preferredCollectionDate;

    @Column(name = "preferred_start_time", nullable = false)
    private java.time.LocalTime preferredStartTime;

    @Column(name = "preferred_end_time", nullable = false)
    private java.time.LocalTime preferredEndTime;

    @Column(name = "waste_information", nullable = false, length = 500)
    private String wasteInformation;

    @Column(name = "collection_requirements", length = 1000)
    private String collectionRequirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,   // 수거 신청 생성, 결제 대기
        CONFIRMED, // 결제 완료, 수거업체 확인 대기
        ACCEPTED,  // 수거업체 수락, 수거 예정
        COMPLETED, // 수거 완료
        REJECTED,  // 수거업체 거절
        CANCELLED  // 취소
    }

    public void confirm() {
        this.status = Status.CONFIRMED;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void accept() {
        if (this.status != Status.CONFIRMED) {
            throw new IllegalArgumentException("업체 확인 대기 상태의 신청만 수락할 수 있습니다");
        }
        this.status = Status.ACCEPTED;
    }

    public void complete() {
        if (this.status != Status.ACCEPTED) {
            throw new IllegalArgumentException("수락된 신청만 수거 완료 처리할 수 있습니다");
        }
        this.status = Status.COMPLETED;
    }

    public void reject() {
        if (this.status != Status.CONFIRMED) {
            throw new IllegalArgumentException("업체 확인 대기 상태의 신청만 거절할 수 있습니다");
        }
        this.status = Status.REJECTED;
    }
}
