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
@ToString(exclude = {"order"})
@Builder
public class Materials implements com.postco.core.entity.Entity, Serializable {
    @Id
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "material_no", nullable = false)
    private String materialNo;

    private String status;     // 재료 진행 상태

    @Column(name = "f_code")
    private String fCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "cur_proc_code")
    private String curProcCode;

    private String type;

    @Enumerated(EnumType.STRING)
    private MaterialProgress progress;   // 재료 진도

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
    @JoinColumn(name = "order_no", referencedColumnName = "no", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @Column(name = "order_id")
    private String orderId;
}

