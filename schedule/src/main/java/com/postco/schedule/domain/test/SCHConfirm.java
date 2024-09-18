package com.postco.schedule.domain.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_sch_confirm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SCHConfirm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "confirm_id")
    private Long id;
    private String scheduleNo;
    private String process;     // 해당 공정에 대한 스케쥴 편성임을 명시
    private String rollUnit;    // 롤 단위
    private LocalDateTime confirmDate;     // 편성 날짜
    private Long scExpectedDuration;    // 스케쥴의 예상 작업 시간
    private int quantity;               // 한 스케쥴에 포함된 코일 재료 개수
    private String confirmedBy;         // 컨펌한 사용자

    @Enumerated(EnumType.STRING)
    private WorkStatus workStatus;     // 작업 상태

    @OneToMany(mappedBy = "schConfirm")
    private List<SCHMaterial> materials = new ArrayList<>();
}
