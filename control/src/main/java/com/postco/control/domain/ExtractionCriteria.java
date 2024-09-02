package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "extraction_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionCriteria implements com.postco.core.entity.Entity, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mapper_id")
    private ExtractionCriteriaMapper mapper;

    private String columnName;
    private String columnValue;

    @Override
    public String toString() {
        return "ExtractionCriteria{" +
                "id=" + id +
                ", columnName='" + columnName + '\'' +
                ", columnValue='" + columnValue + '\'' +
//                ", mapperId=" + (mapper != null ? mapper.getId() : null) +
                '}';
    }
}
