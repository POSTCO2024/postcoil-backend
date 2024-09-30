package com.postco.operation.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "work_instruction_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"workInstruction", "material"})
//@ToString(exclude = "workInstruction")
@Builder
public class WorkInstructionItem implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "work_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
//    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "work_instruction_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private WorkInstruction workInstruction;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "material_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Materials material;

    private Long targetId;

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

    @Column(name = "initial_thickness")
    private Double initialThickness;

    @Column(name = "initial_goal_width")
    private Double initialGoalWidth;

    @Column(name = "initial_width")
    private Double initialWidth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkInstructionItem)) return false;
        WorkInstructionItem that = (WorkInstructionItem) o;
        return Objects.equals(getMaterial().getId(), that.getMaterial().getId()) &&
                Objects.equals(getTargetId(), that.getTargetId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaterial().getId(), getTargetId());
    }

    public void setWorkInstruction(WorkInstruction workInstruction) {
        if (this.workInstruction != workInstruction) {
            WorkInstruction oldWorkInstruction = this.workInstruction;
            this.workInstruction = workInstruction;
            if (oldWorkInstruction != null) {
                oldWorkInstruction.getItems().remove(this);
            }
            if (workInstruction != null && !workInstruction.getItems().contains(this)) {
                workInstruction.getItems().add(this);
            }
        }
    }

    public void startWork() {
        if (this.workItemStatus != WorkStatus.IN_PROGRESS) {
            this.startTime = LocalDateTime.now();
            this.workItemStatus = WorkStatus.IN_PROGRESS;
            this.getWorkInstruction().updateStatus();
        }
    }

    public void finishWork() {
        if (this.workItemStatus != WorkStatus.COMPLETED) {
            this.endTime = LocalDateTime.now();
            this.workItemStatus = WorkStatus.COMPLETED;
            this.getWorkInstruction().updateStatus();
        }
    }

    // 리젝트된 코일 처리 -> Y 로 변경
    public void updateReject() {
        if (this.workItemStatus == WorkStatus.PENDING) {
            // 진행중이 아닌 코일에 대해서만 가능
            this.isRejected = "Y";
        }
    }
}
