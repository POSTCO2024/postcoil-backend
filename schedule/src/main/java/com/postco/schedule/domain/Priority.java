package com.postco.schedule.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="priority")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Priority implements com.postco.core.entity.Entity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String description;

    @NotNull
    private Integer priorityOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PriorityApplyMethod applyMethod;

    @Column(length = 50)
    private String targetColumn;

    @Column(length = 50)
    private String option;

    // 새로 추가된 필드
    private String processCode;
    private String materialUnitCode;

}
