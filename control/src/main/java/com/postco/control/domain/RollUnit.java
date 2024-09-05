package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "roll_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollUnit implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roll_unit_name", nullable = false)
    private String rollUnitName;

    @Column(name = "criteria_value", nullable = false)
    private double criteriaValue;
}