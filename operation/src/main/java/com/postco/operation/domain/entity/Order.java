package com.postco.operation.domain.entity;
import com.postco.core.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "materials")
public class Order extends BaseEntity implements com.postco.core.entity.Entity, Serializable {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true)
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

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<Materials> materials = new ArrayList<>();

    public void addMaterial(Materials material) {
        this.materials.add(material);
        material.setOrder(this);
    }
}
