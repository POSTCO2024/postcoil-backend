package com.postco.operation.domain;
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
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String no;

    private String customer;

    private double thickness;

    private double width;

    private double length;

    @Column(name = "coil_type")
    private String coilType;

    private int quantity;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    private String remarks;

    @OneToMany(mappedBy = "order")
    private List<Materials> materials = new ArrayList<>();

    public void addMaterial(Materials material) {
        this.materials.add(material);
        material.setOrder(this);
    }
}
