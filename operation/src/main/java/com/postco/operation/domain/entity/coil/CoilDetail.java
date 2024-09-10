package com.postco.operation.domain.entity.coil;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coil_detail")
public class CoilDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coil_detail_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coil_standard_id", nullable = false)
    private CoilStandard coilStandard;

    @NotNull
    @Column(name = "min_thickness")
    private Double minThickness;

    @NotNull
    @Column(name = "max_thickness")
    private Double maxThickness;

    @NotNull
    @Column(name = "min_width")
    private Double minWidth;

    @NotNull
    @Column(name = "max_width")
    private Double maxWidth;

}
