package com.postco.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RefDataContainer {
    private List<EquipmentInfoDTO.View> equipmentInfo;
}
