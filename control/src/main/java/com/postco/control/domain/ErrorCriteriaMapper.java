package com.postco.control.domain;

import com.postco.control.presentation.dto.response.ErrorStandardDTO;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    public void updateErrorCriteria(ErrorStandardDTO errorStandardDTO) {
        Map<String, Supplier<String>> fieldMapping = new HashMap<>();
        fieldMapping.put("min_thickness", errorStandardDTO::getMinThickness);
        fieldMapping.put("max_thickness", errorStandardDTO::getMaxThickness);
        fieldMapping.put("min_width", errorStandardDTO::getMinWidth);
        fieldMapping.put("max_width", errorStandardDTO::getMaxWidth);
        fieldMapping.put("coil_type_code", errorStandardDTO::getCoilTypeCode);
        fieldMapping.put("factory_code", errorStandardDTO::getFactoryCode);
        fieldMapping.put("order_no", errorStandardDTO::getOrderNo);
        fieldMapping.put("rem_proc", errorStandardDTO::getRemProc);
        fieldMapping.put("roll_unit", errorStandardDTO::getRollUnit);

        for (ErrorCriteria individualErrorCriteria : errorCriteria) {
            Supplier<String> valueSupplier = fieldMapping.get(individualErrorCriteria.getColumnName());
            if (valueSupplier != null) {
                String newValue = valueSupplier.get();
                if (newValue != null) {
                    individualErrorCriteria.setColumnValue(newValue);
                }
            }
        }
    }

}
