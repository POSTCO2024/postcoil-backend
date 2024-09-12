package com.postco.schedule.domain.edit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "sch_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleConfirm {
    private Long id;
    private String scheduleId;
    private LocalDateTime confirmDate;
    private String confirmManager;
}
