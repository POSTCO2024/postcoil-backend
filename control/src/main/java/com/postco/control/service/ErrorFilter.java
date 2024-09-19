package com.postco.control.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ErrorFilter {
    MIN_THICKNESS("min_thickness"),
    MAX_THICKNESS("max_thickness"),
    MAX_WIDTH("max_width"),
    MIN_WIDTH("min_width"),
    COIL_TYPE_CODE("coil_type_code"),
    FACTORY_CODE("factory_code"),
    ORDER_ID("order_id"),
    REM_PROC("rem_proc"),
    ORDER_NO("order_no"); // 빠진 필터 기준 추가 - maxbort 2024-09-19


    private final String columnName;

    public static ErrorFilter fromColumnName(String columnName) {
        return Arrays.stream(values())
                .filter(filter -> filter.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown error filter: " + columnName));
    }
}
