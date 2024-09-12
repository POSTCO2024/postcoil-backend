package com.postco.operation.domain.entity;

import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "work_time_view")
public class WorkTimeView {
    @Id
    @Column(name = "work_instruction_id")
    private Long workInstructionId;

    @Column(name = "work_code")
    private String workCode;

    @Column(name = "sch_code")
    private String schCode;

    @Column(name = "sch_status")
    private String schStatus;

    @Column(name = "expected_duration")
    private Integer expectedDuration;

    @Column(name = "instruction_start_time")
    private LocalDateTime instructionStartTime;

    @Column(name = "instruction_end_time")
    private LocalDateTime instructionEndTime;

    @Column(name = "total_actual_duration")
    private Integer totalActualDuration;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "first_item_start_time")
    private LocalDateTime firstItemStartTime;

    @Column(name = "last_item_end_time")
    private LocalDateTime lastItemEndTime;

}
