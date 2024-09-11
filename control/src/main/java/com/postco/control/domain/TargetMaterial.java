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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private double weight;

    // processPlan 확인필요
    @Column(name = "process_plan")
    private String processPlan;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "due_date")
    private String dueDate;

    @Column(name = "roll_unit_name")
    private String rollUnitName;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "coil_type_code")
    private String coilTypeCode;

    @Column(name = "is_error", nullable = false)
    private String isError = "N";

    @Column(name = "is_error_passed")
    private String isErrorPassed;    // 수동 에러패스 여부 (데이터 분석을 위한 컬럼)

    @PrePersist
    public void prePersist() {
        if (this.isError == null) {
            this.isError = "N";
        }
    }

}