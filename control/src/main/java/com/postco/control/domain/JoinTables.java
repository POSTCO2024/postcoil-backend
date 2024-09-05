package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "jointables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JoinTables implements com.postco.core.entity.Entity, Serializable {

    private Long id;

    // Order 테이블의 필드

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "goal_thickness")
    private double goalThickness;

    @Column(name = "goal_width")
    private double goalWidth;

    @Column(name = "goal_length")
    private double goalLength;

    @Column(name = "coil_type")
    private String coilType;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "remarks")
    private String remarks;

    // Material 테이블의 필드
    @Id
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "material_no")
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

    // 추가 필드들
//    private String isError;
//    private String errorType;

}