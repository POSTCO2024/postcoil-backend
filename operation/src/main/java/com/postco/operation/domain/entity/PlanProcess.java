package com.postco.operation.domain.entity;
import lombok.*;
import javax.persistence.*;


@Entity
@Table(name = "plan_process")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanProcess implements com.postco.core.entity.Entity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_process_id")
    private Long id;

    @Column(name = "coil_type_code", nullable = false, length = 10)
    private String coilTypeCode;

    private String pcm;

    private String cal;

    private String egl;

    private String cgl;

    private String packing;
}
