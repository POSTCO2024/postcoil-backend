package com.postco.control.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ExtractionFilter {
    FACTORY_CODE("factory_code"),
    MATERIAL_STATUS("material_status"),
    PROGRESS("progress"),
    CURRENT_PROCESS("curr_proc");

    private final String columnName;

    public static ExtractionFilter fromColumnName(String columnName) {
        return Arrays.stream(values())
                .filter(filter -> filter.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown extraction filter: " + columnName));
    }
}