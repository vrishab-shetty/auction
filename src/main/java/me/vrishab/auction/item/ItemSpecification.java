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

            if (filter.name() != null && !filter.name().isBlank()) {
                String pattern = "%" + filter.name().toLowerCase() + "%";
                final Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
                final Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                predicates.add(criteriaBuilder.or(nameLike, descriptionLike));
            }


            if (filter.location() != null && !filter.location().isBlank()) {
                final Predicate location = criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("location")),
                        filter.location().toLowerCase()
                );
                predicates.add(location);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public record ItemFilterParams(String name, String location) {
    }
}
