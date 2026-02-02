package com.back.product.adapter.out;

import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long>
{
    List<OptionValue> findAllByOptionGroupIn(List<OptionGroup> optionGroups);

    @Modifying
    @Query("update OptionValue ov set ov.deletedAt = CURRENT_TIMESTAMP where ov.optionGroup.id = :optionGroupId")
    void deleteAllByOptionGroup_Id(Long optionGroupId);
}
