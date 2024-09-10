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
    @Column(name = "roll_unit_name")
    private String rollUnitName;
    @Column(name = "criteria_value")
    private double criteriaValue;
    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type")
    private RollCriteriaType criteriaType;

    // **** 연관관계 메서드 ****
    public boolean isInRange(double thickness) {
        if (criteriaType == RollCriteriaType.LESS_THAN) {
            return thickness < criteriaValue; // 박물
        }
        return thickness >= criteriaValue;   // 후물
    }
}