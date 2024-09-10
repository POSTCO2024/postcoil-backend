package com.postco.schedule.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;



@Entity
@Table(name = "sch_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMaterials implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "material_id")
    private Long id;

    @Column(nullable = false, name="material_no")
    private String no;

    @Column(name = "coil_type_code")
    private String coilTypeCode;

    private String type;

    @Column(name = "cur_proc")
    private String curProc;

    @Column(name = "f_code")
    private String fCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "outer_dia", nullable = true)
    private double outerDia;

    @Column(name = "inner_dia", nullable = true)
    private double innerDia;

    private Double weight;

    private Double width;

    private Double thickness;

    private Double length;

    private String progress;

    private String status;

    @Column(name = "total_weight", nullable = true)
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

    private Double yard;

    // 추가한 필드1
    private Long targetId;

    @Column(name = "goal_thickness")
    private double goalThickness;

    @Column(name = "goal_width")
    private double goalWidth;

    @Column(name = "goal_length")
    private double goalLength;

    @Column(name = "temperature", nullable = true)
    private double temperature;

    @Column(name = "roll_unit")
    private String rollUnit;

    // 추가한 필드2 - 111111
    @Column(name = "schedule_id")
    private Long scheduleId; // 확정 전 null, 확정 후 존재

    @Column(name = "schedule_no")
    private String scheduleNo;

    @Column(name = "work_time", nullable = true)
    private Long workTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "material_sequences", joinColumns = @JoinColumn(name = "material_id"))
    @Column(name = "sequence", nullable = true)
    @OrderColumn(name = "sequence_order") // Optional: 사용 순서 보존
    private List<Integer> sequence;


}
