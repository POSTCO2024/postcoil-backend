package com.postco.core.dto;

import lombok.Builder;

public class ApiResponseDTO<T> {
    private int status;
    private String message;
    private T result;

    @Builder
    public ApiResponseDTO(final int status, final String message, final T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }
}
