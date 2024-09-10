package com.postco.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

public class OrderDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View implements DTO {
        private Long id;
        private String no;
        private String customer;
        private double thickness;
        private double width;
        private double length;
        private String coilType;
        private int quantity;
        private LocalDateTime dueDate;
        private LocalDateTime orderDate;
        private String remarks;
    }
}
