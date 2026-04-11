package com.microservices.contract.specification;

import com.microservices.contract.dto.PaymentFilterDTO;
import com.microservices.contract.entity.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    public static Specification<Payment> withFilters(PaymentFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null)
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));

            if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isBlank())
                predicates.add(cb.like(cb.lower(root.get("paymentMethod")),
                        "%" + filter.getPaymentMethod().toLowerCase() + "%"));

            if (filter.getContractId() != null)
                predicates.add(cb.equal(root.get("contractId"), filter.getContractId()));

            if (filter.getMinAmount() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));

            if (filter.getMaxAmount() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));

            if (filter.getDateFrom() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), filter.getDateFrom()));

            if (filter.getDateTo() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), filter.getDateTo()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
