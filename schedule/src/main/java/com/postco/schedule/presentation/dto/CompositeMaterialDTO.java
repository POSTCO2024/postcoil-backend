package com.postco.schedule.presentation.dto;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.ScheduleMaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CompositeMaterialDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Target{
        private MaterialDTO.View material;
        private TargetMaterialDTO.View target;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Schedule{
        private MaterialDTO.View material;
        private TargetMaterialDTO.View target;
        private ScheduleMaterialDTO.View schedule;
    }

}
