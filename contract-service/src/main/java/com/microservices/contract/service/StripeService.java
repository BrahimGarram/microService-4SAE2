package com.microservices.contract.service;

import com.microservices.contract.dto.StripePaymentRequestDTO;
import com.microservices.contract.dto.StripePaymentResponseDTO;
import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.Payment;
import com.microservices.contract.entity.PaymentStatus;
import com.microservices.contract.repository.ContractRepository;
import com.microservices.contract.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Creates a Stripe PaymentIntent for a contract and saves a Payment record.
     */
    public StripePaymentResponseDTO createPaymentIntent(StripePaymentRequestDTO request) throws StripeException {

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found: " + request.getContractId()));

        // Stripe amounts are in smallest currency unit (cents)
        long amountInCents = contract.getContractAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Stripe minimum is 50 cents
        if (amountInCents < 50) {
            throw new IllegalArgumentException("Contract amount is too low for Stripe (minimum $0.50)");
        }

        String currency = request.getCurrency() != null ? request.getCurrency() : "usd";

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setDescription(request.getDescription() != null
                        ? request.getDescription()
                        : "Payment for contract: " + contract.getContractTitle())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build());

        // Only set receipt email if provided and non-empty
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            paramsBuilder.setReceiptEmail(request.getCustomerEmail());
        }

        PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());

        // Persist payment record
        Payment payment = new Payment();
        payment.setAmount(contract.getContractAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(mapStripeStatus(intent.getStatus()));
        payment.setContractId(contract.getIdContract());
        payment.setPaymentMethod("Stripe");
        payment.setTransactionId(intent.getId());
        payment.setDescription("Stripe PaymentIntent for: " + contract.getContractTitle());
        Payment saved = paymentRepository.save(payment);

        return new StripePaymentResponseDTO(
                intent.getId(),
                intent.getClientSecret(),
                intent.getStatus(),
                saved.getIdPayment()
        );
    }

    /**
     * Confirms a PaymentIntent after frontend completes payment.
     */
    public StripePaymentResponseDTO confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        // Update our payment record
        paymentRepository.findAll().stream()
                .filter(p -> paymentIntentId.equals(p.getTransactionId()))
                .findFirst()
                .ifPresent(p -> {
                    p.setStatus(mapStripeStatus(intent.getStatus()));
                    paymentRepository.save(p);
                });

        return new StripePaymentResponseDTO(
                intent.getId(),
                intent.getClientSecret(),
                intent.getStatus(),
                null
        );
    }

    private PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "canceled"  -> PaymentStatus.CANCELLED;
            case "requires_payment_method", "requires_confirmation",
                 "requires_action", "processing" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }
}
