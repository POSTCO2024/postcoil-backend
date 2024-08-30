package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coil_standard")
public class CoilStandard {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @NotNull
    @Column(length = 15, unique = true, nullable = false)
    private String code;      // 코일 타입 코드

    @Column(length = 50)
    private String description;

    @NotNull
    @Column(length = 1, columnDefinition = "VARCHAR(1)")
    private String type;

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

    @OneToMany(mappedBy = "coilStandard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoilDetail> coilSpecs;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoilStandard coilStandard = (CoilStandard) o;
        return id.equals(coilStandard.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
