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

    @Column(name = "cur_proc_code")
    private String curProcCode;

    @Column(name = "f_code")
    private String fCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "outer_dia")
    private double outerDia;

    @Column(name = "inner_dia")
    private double innerDia;

    private Double weight;

    private Double width;

    private Double thickness;

    private Double length;

    private String progress;

    private String status;

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

    private Double yard;

    @Column(name = "goal_thickness")
    private double goalThickness;

    @Column(name = "goal_width")
    private double goalWidth;

    @Column(name = "goal_length")
    private double goalLength;

    // 추가한 필드
    @Column(name = "temperature", nullable = true)
    private double temperature;

    @Column(name = "roll_unit")
    private String rollUnit;

    @Column(name = "work_time", nullable = true)
    private Long workTime;

    // 추가한 필드2 - 111111
    private Long targetId;

    private Long scheduleId;



//    public <T> ScheduleMaterials(long id, String name, String status, String fCode, String opCode, String currProc, String type, String progress, double width, double thickness, double length, double weight, double totalWeight, String passProc, String remProc, String preProc, String nextProc, String storageLoc, String yard, String coilTypeCode, double targetWidth, double targetThickness, double temperature, double time, List<T> list) {
//    }

}
