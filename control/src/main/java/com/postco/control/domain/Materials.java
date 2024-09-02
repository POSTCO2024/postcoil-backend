package com.postco.control.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Materials implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String no;

    private String status;

    @Column(name = "f_code")
    private String fCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "curr_proc")
    private String currProc;

    private String type;

    private String progress;

    @Column(name = "outer_dia")
    private double outerDia;

    @Column(name = "inner_dia")
    private double innerDia;

    private double width;

    private double thickness;

    private double length;

    private double weight;

    @Column(name = "total_weight")
    private double totalWeight;

    @Column(name = "pass_proc")
    private String passProc;

    @Column(name = "rem_proc")
    private String remProc;

    @Column(name = "pre_proc")
    private String preProc;

    @Column(name = "next_proc")
    private String nextProc;

    @Column(name = "storage_loc")
    private String storageLoc;

    private String yard;

    @Column(name = "coil_type_code")
    private String coilTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonBackReference // 순환 참조 방지
    private Order order;

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            order.getMaterials().add(this);
        }
    }
}
