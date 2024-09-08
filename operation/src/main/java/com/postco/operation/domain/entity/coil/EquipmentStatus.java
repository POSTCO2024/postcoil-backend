package com.postco.operation.domain.entity.coil;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentStatus implements com.postco.core.entity.Entity{
    @Id
    @GeneratedValue
    @Column(name = "equipment_status_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "eq_status", nullable = false)
    private EquipmentStatusType eqStatus;

    @Column(name = "is_operational", nullable = false, length = 1)
    private String isOperational;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "stop_time")
    private LocalDateTime stopTime;

    @Column(name = "cumulative_time", nullable = false)
    private Long cumulativeTime;

    @Column(name = "cumulative_amount", nullable = false)
    private Integer cumulativeAmount;
}
