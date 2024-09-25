package com.postco.schedule.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info  {
        private Long id;
        private String scheduleNo;
        private Long scExpectedDuration;    // 추가 Sohyun Ahn 240925, 스케쥴의 예상 작업 시간 - 그래프 사이즈 width 받으려고
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoWithWorkStatus  {
        private Long id;
        private String scheduleNo;
        private String workStatus;
    }
}
