package com.postco.schedule.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="constraint_insertion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstraintInsertion implements com.postco.core.entity.Entity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ConstraintInsertionType type;

    @NotNull
    @Column(length = 50)
    private String targetColumn;

    @NotNull
    private Double targetValue;

    @Column(length = 50)
    private String description;

    // 새로 추가된 필드
    private String processCode;
    private String materialUnitCode;

}
