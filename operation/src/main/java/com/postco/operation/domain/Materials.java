package com.postco.operation.domain;

import lombok.*;
import org.springframework.data.util.Lazy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order"})
@Builder
public class Materials implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue
    @Column(name = "material_id")
    private Long id;

    @Column(nullable = false)
    private String no;

    private String status;     // 재료 진행 상태

    @Column(name = "f_code")
    private String fCode;

    @Column(name = "op_code")
    private String opCode;

    @Column(name = "curr_proc")
    private String currProc;

    private String type;

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
}
