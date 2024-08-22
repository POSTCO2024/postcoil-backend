package com.postco.control.domain;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "Order")
@Table(name = "orders")
public class Order implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String no;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private double thickness;

    @Column(nullable = false)
    private double width;

    @Column(nullable = false)
    private double length;

    @Column(nullable = false)
    private String coilType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column
    private String remarks;

}
