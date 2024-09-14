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
@Table(name = "sch_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_no")
    private String no;

    @Column(name = "process_code")
    private String processCode;

    private LocalDate planDate;

    private Long expectedDuration; // 스케줄 예상 작업 시간

    private Long quantity; // 총 코일 수

    @Column(name = "is_confirmed")
    private String isConfirmed;

    @Transient
    private List<ScheduleMaterialsDTO.View> materials;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sch_plan_materials", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "material_id")
    private List<Long> materialIds;  // List of material IDs

}
