package com.postco.operation.domain.entity;

import com.postco.core.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Random;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order"})
public class Materials extends BaseEntity implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "material_id")
    private Long id;

    @Column(name = "material_no", nullable = false)
    private String no;

    private String status;     // 재료 진행 상태

    @Column(name = "factory_code")
    private String factoryCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "curr_proc")
    private String currProc;

    @Column(name = "material_type")
    private String materialType;

    @Enumerated(EnumType.STRING)
    private MaterialProgress progress;   // 재료 진도

    @Column(name = "outer_dia")
    private double outerDia;

    @Column(name = "inner_dia")
    private double innerDia;

    private double width;

    private double thickness;

    private double length;

    private double weight;

    @Column(name = "temperature", columnDefinition = "double default 0")
    // temparture nullable 피하기 위해 default 값 추가 - maxbort 2024-09-19
    private double temperature;

    @Column(name = "total_weight")
    private double totalWeight;

    @Column(name = "pass_proc")
    private String passProc;

    @Column(name = "rem_proc")
    private String remProc;

    @Column(name = "pre_proc")
    private String preProc;

    @Column(name = "next_proc")
    private String nextProc;

    @Column(name = "storage_loc")
    private String storageLoc;

    private String remarks;

    private String yard;

    @Column(name = "coil_type_code")
    private String coilTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @OneToOne(mappedBy = "material", fetch = LAZY)
    private WorkInstructionItem workInstructionItem;

    public void setOrder(Order order) {
        this.order = order;
        if (order != null && !order.getMaterials().contains(this)) {
            order.getMaterials().add(this);
        }
    }

    public void updateProgress(MaterialProgress newProgress) {
        this.progress = newProgress;
    }

    public void updateThickAndWidth(double reduceThickValue, double reduceWidthValue) {
        this.thickness = Math.max(0.001, this.thickness - reduceThickValue) + new Random().nextDouble() * 0.0005;
        this.width = Math.max(0.001, this.width - reduceWidthValue) + new Random().nextDouble() * 0.0005;
    }

    public void updateEntireProgress() {
        // 1. 통과 공정 업데이트
        this.passProc = (this.passProc == null) ? this.currProc : this.passProc + this.currProc;

        // 2. 잔공정 업데이트
        if (this.remProc != null && this.remProc.startsWith(this.currProc)) {
            this.remProc = this.remProc.substring(this.currProc.length());
        }
    }

    public void updateYardAfterWork(String workValue) {
        this.yard = this.currProc + workValue;
    }

    public void finishDelivery() {
        // 잔공정 업테이트
        this.preProc = this.currProc;
        // 현공정 업데이트
        this.currProc = this.nextProc;

        // 차공정 업데이트
        if (this.remProc != null && this.remProc.startsWith(this.currProc)) {
            this.nextProc = this.remProc.substring(this.currProc.length());
            if (this.currProc.matches("\\d{3}")) {
                this.nextProc = "FINAL";
            }
        }
    }

}
