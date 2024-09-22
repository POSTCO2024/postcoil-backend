package com.postco.operation.domain.entity;

import lombok.*;

import javax.persistence.*;

import java.util.concurrent.CompletableFuture;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "coil_supply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoilSupply {
    @Id
    @GeneratedValue
    @Column(name = "coil_supply_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "work_instruction_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private WorkInstruction workInstruction;

    private int totalCoils;  // 전체 코일 수
    private int suppliedCoils;  // 보급 완료 된 코일 수(설비 앞 도착)
    private int totalProgressed;  // 현재까지 실제 작업이 진행된 코일 수
    private int totalRejects;  // 리젝트된 코일 수 (개별 리젝트는 작업 아이템에서 관리)


    // ===== 연관관계 메서드 ====

    // 보급 요구된 수 update
    public void updateSupply(int suppliedCount) {
        this.suppliedCoils += suppliedCount;
    }

    // 리젝트 된 코일 update
    public void updateRejects(int rejectsCount) {
        this.totalRejects += rejectsCount;
    }

    // 작업이 완료 코일 업데이트
    public void updateProgressed() {
        this.totalProgressed += 1;
    }
}
