package com.postco.operation.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "work_schedule_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "items")
@Builder
public class WorkScheduleSummary {
    @Id
    private String process;
    private int totalWorkInstructions;
    private int totalGoalCoils;
    private int totalCompleteCoils;
    private int totalScheduledCoils;
}
