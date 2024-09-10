package com.postco.schedule.domain;

import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sch_pending")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulePending {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "schedule_no")
    private String no;

    @Column(name = "cur_proc")
    private String curProc;

    @Transient
    private Long targetQuantity = 0L;

    private String planDateTime; // "yyMMddHHmm"

    @Column(name = "is_confirmed")
    private String isConfirmed = "N";

    @Transient
    private  List<ScheduleMaterialsDTO.View> materials;

}
