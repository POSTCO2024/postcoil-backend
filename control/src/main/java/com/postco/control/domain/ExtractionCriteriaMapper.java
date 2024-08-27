package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "extraction_criteria_mapper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionCriteriaMapper implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processCode;
    private String extractionGroup;

    @OneToMany(mappedBy = "mapper")
    private List<ExtractionCriteria> extractionCriteria;
}
