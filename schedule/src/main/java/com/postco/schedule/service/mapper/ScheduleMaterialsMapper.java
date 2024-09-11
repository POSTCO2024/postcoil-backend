package com.postco.schedule.service.mapper;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.ScheduleMaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.schedule.presentation.dto.CompositeMaterialDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;

public class ScheduleMaterialsMapper {

    // 공통 필드 매핑 메소드
    private static ScheduleMaterialsDTO.View.ViewBuilder mapCommonFields(MaterialDTO.View material, TargetMaterialDTO.View target) {
        return ScheduleMaterialsDTO.View.builder()
                // material 필드 매핑
                .id(material.getId())
                .no(material.getNo())
                .status(material.getStatus())
                .opCode(material.getOpCode())
                .currProc(material.getCurrProc())
                .progress(material.getProgress())
                .outerDia(material.getOuterDia())
                .innerDia(material.getInnerDia())
                .width(material.getWidth())
                .thickness(material.getThickness())
                .length(material.getLength())
                .weight(material.getWeight())
                .totalWeight(material.getTotalWeight())
                .passProc(material.getPassProc())
                .remProc(material.getRemProc())
                .preProc(material.getPreProc())
                .nextProc(material.getNextProc())
                .storageLoc(material.getStorageLoc())
                .yard(material.getYard())
                .coilTypeCode(material.getCoilTypeCode())

                // Target 필드 매핑
                .goalWidth(target.getGoalWidth())
                .goalThickness(target.getGoalThickness())
                .goalLength(target.getGoalLength())
                // .temperature(target.getTemperature())
                .rollUnitName(target.getRollUnitName())
                .targetId(target.getId());
    }

    // CompositeMaterialDTO.Target을 ScheduleMaterialsDTO.View로 매핑
    public static ScheduleMaterialsDTO.View mapTargetToView(CompositeMaterialDTO.Target targetDTO) {
        MaterialDTO.View material = targetDTO.getMaterial();
        TargetMaterialDTO.View target = targetDTO.getTarget();

        // 공통 필드 매핑 후 추가 필드 설정
        return mapCommonFields(material, target)
                .build();
    }

    // CompositeMaterialDTO.Schedule을 ScheduleMaterialsDTO.View로 매핑
    public static ScheduleMaterialsDTO.View mapScheduleToView(CompositeMaterialDTO.Schedule scheduleDTO) {
        MaterialDTO.View material = scheduleDTO.getMaterial();
        TargetMaterialDTO.View target = scheduleDTO.getTarget();
        ScheduleMaterialDTO.View schedule = scheduleDTO.getSchedule();

        // 공통 필드 매핑 후 추가 필드 설정
        return mapCommonFields(material, target)
                // Schedule 필드 추가 매핑
                .expectedItemDuration(schedule.getExpectedItemDuration()) // 추가한 필드 매핑
                .scheduleId(schedule.getScheduleId())
                .scheduleNo(schedule.getScheduleNo()) // 추가한 필드 매핑
                .sequence(schedule.getSequence()) // 추가한 필드 매핑
                .build();
    }
}

