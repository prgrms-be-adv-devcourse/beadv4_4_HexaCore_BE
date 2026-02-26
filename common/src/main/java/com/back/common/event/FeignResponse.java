package com.back.common.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeignResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;
}
