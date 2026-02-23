package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.OptionUseCase;
import com.back.product.app.usecase.ProductUseCase;
import com.back.product.dto.command.OptionCreateCommand;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.request.OptionListCreateRequestDto;
import com.back.product.dto.request.OptionValueModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.dto.response.OptionValueModifyResponseDto;
import com.back.product.mapper.OptionCreateCommandMapper;
import com.back.product.mapper.OptionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionFacade {
    private final OptionUseCase optionUseCase;
    private final ProductUseCase productUseCase;
    private final OptionMapper optionMapper;
    private final OptionCreateCommandMapper optionCreateCommandMapper;

    @Loggable
    @Transactional
    public OptionListResponseDto createOptions(@Valid OptionListCreateRequestDto request) {
        List<OptionCreateCommand> optionCreateCommands = request.options().stream().map(optionCreateCommandMapper::toCommand).toList();
        List<OptionDto> optionDtos = optionUseCase.createOptions(optionCreateCommands);
        return optionMapper.toListResponseDto(optionDtos);
    }

    @Loggable
    @Transactional
    public OptionResponseDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        OptionDto optionDto = optionUseCase.appendOptions(optionGroupId, request.values());
        return optionMapper.toResponseDto(optionDto);
    }

    @Loggable
    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, @Valid OptionGroupModifyRequestDto request) {
        return optionUseCase.modifyOptionGroup(optionGroupId, request.name());
    }

    @Loggable
    @Transactional
    public OptionValueModifyResponseDto modifyOptionValue(Long optionValueId, @Valid OptionValueModifyRequestDto request) {
        return optionUseCase.modifyOptionValue(request.optionGroupId(), optionValueId, request.name());
    }

    @Loggable
    @Transactional
    public void deleteOptionGroup(Long optionGroupId) {
        Boolean isUsed = productUseCase.isOptionGroupInUse(optionGroupId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_GROUP_IN_USE);
        }

        optionUseCase.deleteOptions(optionGroupId);
    }

    @Loggable
    @Transactional
    public void deleteOptionValue(Long optionValueId) {
        Boolean isUsed = productUseCase.isOptionValueInUse(optionValueId);

        if (isUsed) {
            throw new CustomException(FailureCode.OPTION_VALUE_IN_USE);
        }

        optionUseCase.deleteOption(optionValueId);
    }

    @Loggable
    @Transactional(readOnly = true)
    public OptionListResponseDto getOptions() {
        List<OptionDto> optionDtos = optionUseCase.findAllOptions();
        return optionMapper.toListResponseDto(optionDtos);
    }
}
