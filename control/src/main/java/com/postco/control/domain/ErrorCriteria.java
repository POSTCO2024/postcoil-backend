package com.postco.control.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "error_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorCriteria implements com.postco.core.entity.Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mapper_id")
    private ErrorCriteriaMapper mapper;

    private String errorType;
    private String columnName;
    private String columnValue;

    @Override
    public String toString() {
        return "ErrorCriteria{" +
                "id=" + id +
                ", columnName='" + columnName + '\'' +
                ", columnValue='" + columnValue + '\'' +
                '}';
    }
}
