package com.lecture.enrollment.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Payment Service: 결제 요청 (동기 REST)
     */
    public PaymentResult requestPayment(Long collectionRequestId, Long generatorId, Long collectionServiceId, BigDecimal amount) {
        try {
            PaymentRequest request = new PaymentRequest(collectionRequestId, generatorId, collectionServiceId, amount);

            PaymentResult result = webClientBuilder.build()
                    .post()
                    .uri("http://payment-service:8084/api/payments/internal/request")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResult.class)
                    .block();

            log.info("[PaymentServiceClient] 결제 요청 완료 - userId: {}, courseId: {}, result: {}",
                    generatorId, collectionServiceId, result != null ? result.getStatus() : "null");

            return result;
        } catch (Exception e) {
            log.error("[PaymentServiceClient] 결제 요청 실패 - userId: {}, courseId: {}, error: {}",
                    generatorId, collectionServiceId, e.getMessage(), e);
            throw new RuntimeException("Payment Service 연결 실패");
        }
    }

    @Getter
    @NoArgsConstructor
    static class PaymentRequest {
        private Long collectionRequestId;
        private Long generatorId;
        private Long collectionServiceId;
        private BigDecimal amount;

        PaymentRequest(Long collectionRequestId, Long generatorId, Long collectionServiceId, BigDecimal amount) {
            this.collectionRequestId = collectionRequestId;
            this.generatorId = generatorId;
            this.collectionServiceId = collectionServiceId;
            this.amount = amount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentResult {
        private Long paymentId;
        private String status; // COMPLETED / FAILED
    }
}
