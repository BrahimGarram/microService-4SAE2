package com.microservices.contract.specification;

import com.microservices.contract.dto.ContractFilterDTO;
import com.microservices.contract.entity.Contract;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ContractSpecification {

    public static Specification<Contract> withFilters(ContractFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null)
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));

            if (filter.getContractType() != null)
                predicates.add(cb.equal(root.get("contractType"), filter.getContractType()));

            if (filter.getPaymentType() != null)
                predicates.add(cb.equal(root.get("paymentType"), filter.getPaymentType()));

            if (filter.getFreelancerEmail() != null && !filter.getFreelancerEmail().isBlank())
                predicates.add(cb.equal(cb.lower(root.get("freelancerEmail")),
                        filter.getFreelancerEmail().toLowerCase()));

            if (filter.getClientEmail() != null && !filter.getClientEmail().isBlank())
                predicates.add(cb.equal(cb.lower(root.get("clientEmail")),
                        filter.getClientEmail().toLowerCase()));

            if (filter.getMinAmount() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("contractAmount"), filter.getMinAmount()));

            if (filter.getMaxAmount() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("contractAmount"), filter.getMaxAmount()));

            if (filter.getStartDateFrom() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDateFrom()));

            if (filter.getStartDateTo() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filter.getStartDateTo()));

            if (filter.getEndDateFrom() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), filter.getEndDateFrom()));

            if (filter.getEndDateTo() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), filter.getEndDateTo()));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String like = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("contractTitle")), like),
                        cb.like(cb.lower(root.get("projectDescription")), like),
                        cb.like(cb.lower(root.get("deliverables")), like),
                        cb.like(cb.lower(root.get("freelancerName")), like),
                        cb.like(cb.lower(root.get("clientName")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
