package com.postco.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sch_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SCHHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tableName;     // 변경된 테이블 명 ( 예 : SCHMaterial)
    private Long schPlanId;       // 대상재가 속한 스케쥴 ID
    private Long schMaterialId;   // 스케쥴 대상재 ID
    private String columnName;    // 변경된 컬럼 이름 (예 : sequence )
    private String oldValue;      // 이전 값
    private String newValue;      // 바뀐 값

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime changedAt;

    @LastModifiedBy
    private String changedBy;
}
