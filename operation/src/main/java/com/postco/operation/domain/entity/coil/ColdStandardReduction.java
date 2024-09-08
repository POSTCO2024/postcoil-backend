package com.postco.operation.domain.entity.coil;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "coil_standard_reduction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColdStandardReduction implements com.postco.core.entity.Entity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coil_reduction_id")
    private Long id;

    @Column(name = "coil_type_code", nullable = false, length = 10)
    private String coilTypeCode;

    @Column(name = "process", nullable = false, length = 10)
    private String process;

    @Column(name = "thickness_reduction")
    private Double thicknessReduction;

    @Column(name = "width_reduction")
    private Double widthReduction;

    @Column(name = "temperature")
    private Double temperature;
}
