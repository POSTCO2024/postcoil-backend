package com.postco.schedule.domain.edit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sch_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SCHMaterial implements com.postco.core.entity.Entity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rollUnit;
    private String currProc;
    private Double temperature;
    private Double width;
    private Double thickness;
    private Long schedulePlanId;
    private String isScheduled;  // 미편성 여부
    private int sequence;        // 순서
    private String isRejected;   // 리젝 여부
    private Long expectedDuration;   // 예상 작업 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private SCHPlan schPlan;
}
