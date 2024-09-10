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
@Table(name = "sch_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResult {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "schedule_no")
    private String no;

    // 해당 공정코드
    @Column(name = "cur_proc")
    private String curProc;

    private Long rejectQuantity = 0L;

    private String planDateTime; // "yyMMddHHmm"

    // 작업시작시간
    private String startTime;

    // 작업종료시간
    private String endTime;

    // 작업 상태 "SCHEDULED" "IN_PROGRESS" "COMPLETED"
    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    private ScheduleWorkStatus workStatus = ScheduleWorkStatus.SCHEDULED;

    @Transient
    private List<ScheduleMaterialsDTO.Schedule> materials;

    @PreUpdate
    public void preUpdate(){
        // 엔터티가 업데이트 되기 전에 실행될 로직

        if(materials != null && !materials.isEmpty()){
            // rejectQuantity 계산: isRejected가 "Y"인 것의 개수
            this.rejectQuantity = (Long) materials.stream()
                    .filter(material -> "Y".equals(material.getIsRejected()))
                    .count();
        }

        // 첫 번째 코일의 startTime이 null이 아니거나 빈 문자열이 아닌지 확인하여 startTime 설정
        ScheduleMaterialsDTO.Schedule firstMaterial = materials.get(0);
        if (firstMaterial.getStartTime() != null && !firstMaterial.getStartTime().isEmpty()) {
            this.startTime = firstMaterial.getStartTime();
        }

        // 마지막 코일의 endTime이 null이 아니거나 빈 문자열이 아닌지 확인하여 endTime 설정
        ScheduleMaterialsDTO.Schedule lastMaterial = materials.get(materials.size() - 1);
        if (lastMaterial.getEndTime() != null && !lastMaterial.getEndTime().isEmpty()) {
            this.endTime = lastMaterial.getEndTime();
        }

        // workStatus 설정
        if (this.endTime != null && !this.endTime.isEmpty()) {
            this.workStatus = ScheduleWorkStatus.COMPLETED;  // endTime이 있으면 "COMPLETED"
        } else if (this.startTime != null && !this.startTime.isEmpty()) {
            this.workStatus = ScheduleWorkStatus.IN_PROGRESS;  // endTime은 없으나 startTime이 있으면 "INPROGRESS"
        } else {
            this.workStatus = ScheduleWorkStatus.SCHEDULED;  // 둘 다 없으면 "SCHEDULED"
        }
    }

}
