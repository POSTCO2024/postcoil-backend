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

    @Column(name = "work_code", nullable = false)
    private String workCode;

    @Column(name = "sch_code", nullable = false)
    private String scheduleCode;

    @Column(name = "expected_duration")
    private Long expectedDuration;

    @Column(name = "actual_duration")
    private Long actualDuration;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "sch_status", nullable = false)
    private WorkStatus workStatus;

    @OneToMany(mappedBy = "workInstruction")
    @Builder.Default
    @JsonManagedReference
    private List<WorkInstructionItem> items = new ArrayList<>();

    public void addItem(WorkInstructionItem item) {
        items.add(item);
        item.setWorkInstruction(this);
    }

    public void removeItem(WorkInstructionItem item) {
        items.remove(item);
        item.setWorkInstruction(null);
    }
}
