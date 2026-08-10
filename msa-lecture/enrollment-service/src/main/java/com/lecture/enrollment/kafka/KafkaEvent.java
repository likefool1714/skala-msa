package com.lecture.enrollment.kafka;

import lombok.*;

/**
 * Kafka 이벤트 메시지 DTO
 */
public class KafkaEvent {

    /**
     * Payment Service → Enrollment Service
     * 결제 완료 이벤트 수신
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentCompletedEvent {
        private Long paymentId;
        private Long collectionRequestId;
        private Long generatorId;
        private Long collectionServiceId;
        private String status; // COMPLETED
    }

    /**
     * Enrollment Service → Recommend Service
     * 수강 활성화 완료 이벤트 발행
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentCompletedEvent {
        private Long collectionRequestId;
        private Long generatorId;
        private Long collectionServiceId;
    }
}
