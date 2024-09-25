package com.postco.schedule.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "test_sch_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SCHPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;
    private String scheduleNo;
    private String process;     // 해당 공정에 대한 스케쥴 편성임을 명시
    private String rollUnit;    // 롤 단위
    private LocalDateTime planDate;     // 편성 날짜
    private Long scExpectedDuration;    // 스케쥴의 예상 작업 시간
    private int quantity;               // 한 스케쥴에 포함된 코일 재료 개수
    private String isConfirmed;        // 컨펌 여부

    @OneToMany(mappedBy = "schPlan")
    private List<SCHMaterial> materials = new ArrayList<>();

    // ========= 연관관계 메서드 ===========
    // 1. 스케쥴 확정 시, 업데이트 메서드
    public void confirmSchedule() {
        this.isConfirmed = "Y";
    }

    public void addMaterial(SCHMaterial material) {
        materials.add(material);
        material.setSchPlan(this);
    }
}