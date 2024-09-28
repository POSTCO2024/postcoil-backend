package com.postco.operation.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_instruction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "items")
@Builder
public class WorkInstruction implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "work_instruction_id")
    private Long id;
    private String workNo;
    private String scheduleId;
    private String scheduleNo;
    private String process;
    private String rollUnit;
    private int totalQuantity;
    private Long expectedDuration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "sch_status", nullable = false)
    private WorkStatus workStatus;

    @OneToMany(mappedBy = "workInstruction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<WorkInstructionItem> items = new ArrayList<>();

    // ============= 연관관계 메서드 ============
    public void updateStatus() {
        boolean allCompleted = items.stream().allMatch(item -> item.getWorkItemStatus() == WorkStatus.COMPLETED);
        boolean anyInProgress = items.stream().anyMatch(item -> item.getWorkItemStatus() == WorkStatus.IN_PROGRESS);

        if (allCompleted) {
            completeInstruction();
        } else if (anyInProgress) {
            startInstruction();
        }
    }

    public void startInstruction() {
        if (this.workStatus != WorkStatus.IN_PROGRESS) {
            this.workStatus = WorkStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
        }
    }

    public void completeInstruction() {
        if (this.workStatus != WorkStatus.COMPLETED) {
            this.workStatus = WorkStatus.COMPLETED;
            this.endTime = LocalDateTime.now();
        }
    }
}
