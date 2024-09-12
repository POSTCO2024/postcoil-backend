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
public class SCHMaterial {
    private Long id;
    private Long targetMaterialId;
    private Long scheduleId;
    private String isScheduled;  // 미편성 여부
    private int sequence;        // 순서
    private String isRejected;   // 리젝 여부
    private Long expectedDuration;   // 예상 작업 시간
}
