package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OptionDto {
    private GroupDto group;
    private List<ValueDto> values;

    @Getter
    @NoArgsConstructor
    public static class GroupDto {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class ValueDto {
        private Long id;
        private String name;
    }
}
