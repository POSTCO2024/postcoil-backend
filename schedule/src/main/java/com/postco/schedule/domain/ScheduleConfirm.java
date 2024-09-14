package com.postco.schedule.domain;

import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sch_confirm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleConfirm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "schedule_no")
    private String scheduleNo;

    // 해당 공정코드
    @Column(name = "process_code")
    private String processCode;

    private LocalDate confirmDate;

    // 스케줄 확정한 사람
    private String confirmManager;

}
