package com.postco.operation.domain.entity.coil;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentStatus implements com.postco.core.entity.Entity{
    @Id
    @GeneratedValue
    @Column(name = "equipment_status_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "eq_status", nullable = false)
    private EquipmentStatusType eqStatus;

    @Column(name = "is_operational", nullable = false, length = 1)
    private String isOperational;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "stop_time")
    private LocalDateTime stopTime;

    @Column(name = "cumulative_time", nullable = false)
    private Long cumulativeTime;

    @Column(name = "cumulative_amount", nullable = false)
    private Integer cumulativeAmount;

    // ==== 연관관계 메서드 ====
    // 설비 가동 시작 메서드
    public void startEquipment() {
        this.isOperational = "Y";
        this.eqStatus = EquipmentStatusType.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    // 긴급 정지 메서드
    public void stopEquipment() {
        this.isOperational = "N";
        this.eqStatus = EquipmentStatusType.STOPPED;
        this.stopTime = LocalDateTime.now();
        if (this.startTime != null) {
            long elapsedSeconds = java.time.Duration.between(this.startTime, this.stopTime).getSeconds();
            this.cumulativeTime += elapsedSeconds;
        }
    }

    // 설비 고장 시, 메서드
    public void brokenEquipment() {
        this.isOperational = "N";
        this.eqStatus = EquipmentStatusType.BROKEN;
        this.stopTime = LocalDateTime.now();
    }

    // 현재 시점에 따른 작업시간 계산
    public long calculateCurrentOperationalTime() {
        if (this.isOperational.equals("Y") && this.startTime != null) {
            long elapsedSeconds = java.time.Duration.between(this.startTime, LocalDateTime.now()).getSeconds();
            // 누적 시간을 업데이트 (필요할 때마다 업데이트 가능)
            this.cumulativeTime += elapsedSeconds;
            this.startTime = LocalDateTime.now();
            return this.cumulativeTime;
        }
        return this.cumulativeTime;
    }
}
