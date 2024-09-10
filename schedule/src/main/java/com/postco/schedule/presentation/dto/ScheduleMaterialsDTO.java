package com.postco.schedule.presentation.dto;

import com.postco.core.dto.DTO;
import com.postco.schedule.domain.ScheduleWorkStatus;
import lombok.*;

import java.util.List;

public class ScheduleMaterialsDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View implements DTO{ // 스케줄 편성-관리 용
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String curProc;
        private String type;
        private String progress;
        private double outerDia;
        private double innerDia;
        private double width;
        private double thickness;
        private double length;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;

        // 작업대상재 필드
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private double temperature;
        private String rollUnit;
        private Long targetId;

        // 추가한 필드
        private double workTime;
        private String scheduleNo;
        private List<Integer> sequence;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Target implements DTO { // cache-server, 스케줄 편성-관리 용
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String curProc;
        private String type;
        private String progress;
        private double outerDia;
        private double innerDia;
        private double width;
        private double thickness;
        private double length;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;

        // 작업대상재 필드
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private String rollUnit;
        private Long targetId;
        private double temperature;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Schedule implements DTO { // cache-server, 스케줄 결과-이력 용
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String curProc;
        private String type;
        private String progress;
        private double outerDia;
        private double innerDia;
        private double width;
        private double thickness;
        private double length;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;

        // 작업대상재 필드
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private String rollUnit;
        private Long targetId;
        private double temperature;
        private String startTime; // 재료의 작업 시작 시간
        private String endTime; // 재료의 작업 종료 시간
        private String isRejected; // 재료의 reject 여부 "Y" or "N"

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result implements DTO { // 스케줄 결과-이력 용
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String curProc;
        private String type;
        private String progress;
        private double outerDia;
        private double innerDia;
        private double width;
        private double thickness;
        private double length;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;

        // 작업대상재 필드
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private String rollUnit;
        private Long targetId;
        private double temperature;
        private String startTime; // 재료의 작업 시작 시간
        private String endTime; // 재료의 작업 종료 시간
        private String isRejected; // 재료의 reject 여부 "Y" or "N"

        // 추가한 필드
        private double workTime; // 재료의 작업하는데 걸리는 시간
        private Long scheduleId;
        private List<Integer> sequence;

    }

}
