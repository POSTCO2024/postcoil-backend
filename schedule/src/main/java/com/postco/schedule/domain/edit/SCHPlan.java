package com.postco.schedule.domain.edit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sch_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SCHPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String scheduleNo;
    private String process;     // 해당 공정에 대한 스케쥴 편성임을 명시
    private LocalDateTime planDate;     // 편성 날짜
    private Long scExpectedDuration;    // 스케쥴의 예상 작업 시간
    private int quantity;               // 한 스케쥴에 포함된 코일 재료 개수
    private String is_confirmed;        // 컨펌 여부

}