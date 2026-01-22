package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.domain.OptionValue;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionUseCase {
    private final ProductSupport productSupport;

    @Transactional(readOnly = true)
    public Map<Long, OptionValue> findOptionValuesAsMap(@Valid List<Long> optionValueIds) {
        List<OptionValue> foundValues = productSupport.getAllOptionValues(optionValueIds);

        if (foundValues.size() != optionValueIds.size()) {
            throw new CustomException(FailureCode.OPTION_VALUE_NOT_FOUND);
        }

        return foundValues.stream()
                .collect(Collectors.toMap(OptionValue::getId, value -> value));
    }
}
