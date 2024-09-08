package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "target_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TargetMaterial implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "target_material_id")
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "material_no", nullable = false)
    private String materialNo;

    @Column(name = "goal_width")
    private double goalWidth;

    @Column(name = "goal_thickness")
    private double goalThickness;

    @Column(name = "goal_length")
    private double goalLength;

    @Column
    private double weight;

    // processPlan 확인필요
    @Column(name = "process_plan")
    private String processPlan;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "due_date")
    private String dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roll_unit_name", referencedColumnName = "roll_unit_name")
    private RollUnit rollUnitName;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "is_error", nullable = false)
    private String isError;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "coil_type_code")
    private String coilTypeCode;

    @Column(name = "target_material_group_id", nullable = false)      // 작업 대상재 추출 ID
    private String targetMaterialGroupId;

    @Column(name = "criteria", nullable = false)      // 공정 기준
    private String criteria;

    @Column(name = "remarks")
    private String remarks;
}