package com.postco.operation.domain.entity.coil;


import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment implements com.postco.core.entity.Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long id;

    @Column(name = "eq_code", nullable = false, length = 10)
    private String eqCode;

    private String process;

    @Column(name = "min_width_in")
    private double minWidthIn;

    @Column(name = "max_width_in")
    private double maxWidthIn;

    @Column(name = "min_thickness_in")
    private double minThicknessIn;

    @Column(name = "max_thickness_in")
    private double maxThicknessIn;

    @Column(name = "min_width_out")
    private double minWidthOut;

    @Column(name = "max_width_out")
    private double maxWidthOut;

    @Column(name = "min_thickness_out")
    private double minThicknessOut;

    @Column(name = "max_thickness_out")
    private double maxThicknessOut;

    private double speed;

    @Column(name = "max_weight")
    private double maxWeight;

    @Column(name = "ton_for_hour")
    private double tonForHour;
}
