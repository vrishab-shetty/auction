package me.vrishab.auction.item;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ItemSpecification {

    public static Specification<Item> filterSpecification(ItemFilterParams filter) {
        return (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                String pattern = "%" + filter.name() + "%";
                final Predicate name = criteriaBuilder.like(root.get("name"), pattern);
                predicates.add(name);
            }


            if (filter.location() != null) {
                final Predicate location = criteriaBuilder.equal(root.get("location"), filter.location());
                predicates.add(location);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public record ItemFilterParams(String name, String location) {
    }
}
