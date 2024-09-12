package com.postco.core.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiResponseDTO<T> {
    private int status;
    private String resultMsg;
    private T result;

    @Builder
    public ApiResponseDTO(final int status, final String resultMsg, final T result) {
        this.status = status;
        this.resultMsg = resultMsg;
        this.result = result;
    }
}