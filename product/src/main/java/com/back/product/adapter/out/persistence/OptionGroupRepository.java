package com.back.product.adapter.out.persistence;

import com.back.product.domain.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
    OptionGroup findByName(String name);
}
