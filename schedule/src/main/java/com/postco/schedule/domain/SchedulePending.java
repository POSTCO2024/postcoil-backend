package com.postco.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    @Column(name = "cur_proc_code")
    private String curProcCode;

    private Long targetQuantity;

    private String planDate;
}
