package com.postco.schedule.presentation.test;

import lombok.*;

import java.util.List;

/**
 * 코일 순서 업데이트 및 확정 관련 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SCHForm {
    private Long planId;             // 스케쥴 편성 ID
    private String confirmBy;        // 확정한 사람
    private List<UpdateMaterialInfo> updateMaterials;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMaterialInfo {
        private Long materialId;   // 재료 Id
        private int sequence;      // 새로운 시퀀스 값
    }
}
