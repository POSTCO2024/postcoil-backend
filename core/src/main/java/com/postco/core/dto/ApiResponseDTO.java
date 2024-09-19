package com.postco.core.dto;

import lombok.*;

@Getter
public class ApiResponseDTO<T> {
    private final int status;
    private final String resultMsg;
    private final T result;

    @Builder
    public ApiResponseDTO(final int status, final String resultMsg, final T result) {
        this.status = status;
        this.resultMsg = resultMsg;
        this.result = result;
    }
}
