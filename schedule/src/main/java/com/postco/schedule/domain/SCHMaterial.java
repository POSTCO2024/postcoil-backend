package com.postco.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    // 필요 컬럼 추가2 - Sohyun Ahn 2024-09-19
    private String materialNo;

    // 필요 컬럼 추가 - maxbort 2024-09-19
    private String nextProc;
    private Double goalWidth;
    private Double goalThickness;

    // 필요 컬럼 추가 - maxbort 2024-09-21
    private Double totalWeight;
    private Double goalLength;
    private String coilTypeCode;

    private String isScheduled;  // 미편성 여부
    private int sequence;        // 순서
    private String isRejected;   // 리젝 여부
    private Long expectedDuration;   // 예상 작업 시간

    @Enumerated(EnumType.STRING)
    private WorkStatus workStatus;   // 작업 상태 -> 일단 둠.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private SCHPlan schPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirm_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private SCHConfirm schConfirm;


    // ========= 연관관계 메서드 ==========

    // 1. 코일 순서 변경 메서드
    public void updateSequence(int newValue) {
        if (this.sequence != newValue) {
            this.sequence = newValue;
        }
    }

    // 2. 작업 상태 변경 메서드
    public void updateWorkStatus(WorkStatus newValue) {
        this.workStatus = newValue;
    }

    // 3. 미편성 처리 메서드 ( 확정이 안난 재료들 )
    public void unassignFromSchedule() {
        this.schPlan = null;
        this.isScheduled = "N";
    }
}
