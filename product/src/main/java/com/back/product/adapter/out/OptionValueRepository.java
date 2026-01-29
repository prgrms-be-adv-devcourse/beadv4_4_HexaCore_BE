package com.back.product.adapter.out;

import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long>
{
    List<OptionValue> findAllByOptionGroupIn(List<OptionGroup> optionGroups);
}
