package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "error_criteria_mapper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorCriteriaMapper implements com.postco.core.entity.Entity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processCode;
    private String errorGroup;

    @OneToMany(mappedBy = "mapper")
    private List<ErrorCriteria> errorCriteria;

    @Override
    public String toString() {
        return "ErrorCriteriaMapper [" +
                "id=" + id +
                ", processCode=" + processCode + '\'' +
                ", errorGroup=" + errorGroup + '\'' +
                ", errorCriteria='" + errorCriteria + '\'' +
                "]";
    }
}
