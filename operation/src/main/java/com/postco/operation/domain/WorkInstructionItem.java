package com.postco.operation.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "work_instruction_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"workInstruction", "material"})
@Builder
public class WorkInstructionItem implements com.postco.core.entity.Entity, Serializable {
    @Id @GeneratedValue
    @Column(name = "work_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "work_instruction_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private WorkInstruction workInstruction;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "material_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Materials material;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_item_status", nullable = false)
    private WorkStatus workItemStatus;

    @Column(name = "sequence")
    private int sequence;

    @Column(name = "is_rejected")
    private String isRejected;

    @Column(name = "expected_item_duration")
    private Long expectedItemDuration;

    @Column(name = "start_time")
    private LocalDateTime startTime; // 실제 시작 시간

    @Column(name = "end_time")
    private LocalDateTime endTime; // 종료 작업 시간

    public void setWorkInstruction(WorkInstruction workInstruction) {
        this.workInstruction = workInstruction;
        if (workInstruction != null && !workInstruction.getItems().contains(this)) {
            workInstruction.getItems().add(this);
        }
    }

    public void setMaterial(Materials material) {
        this.material = material;
    }

    public void startWork() {
        this.startTime = LocalDateTime.now();
        this.workItemStatus = WorkStatus.IN_PROGRESS;
    }

    public void finishWork() {
        this.endTime = LocalDateTime.now();
        this.workItemStatus = WorkStatus.COMPLETED;
    }
}
