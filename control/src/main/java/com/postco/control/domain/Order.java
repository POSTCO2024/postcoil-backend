package com.postco.control.domain;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name="goal_thickness",nullable = false)
    private double goalThickness;

    @Column(name = "goal_width", nullable = false)
    private double goalWidth;

    @Column(name = "goal_length", nullable = false)
    private double goalLength;

    @Column(name = "coil_type", nullable = false)
    private String coilType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column
    private String remarks;

}
