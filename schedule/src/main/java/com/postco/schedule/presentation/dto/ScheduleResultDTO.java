package com.postco.schedule.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ScheduleResultDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Status implements DTO{
        private Long id; // 스케줄 id
        private Long scheduleId;
        private String scheduleNo; // schedule 이름
        private String workStatus; // 작업 상태 FIXME
        private Long rejectedQuantity; // 총 reject 코일 수 FIXME
        private LocalDateTime planDateTime; // 날짜별 조회를 위해 FIXME
        private String startTime; // 작업시작시간
        private String endTime; // 작업종료시간
        private Long expectedDuration; // 스케줄 예상 시간
        private Long actualDuration; // 실제 스케줄 소요 시간
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Work implements DTO {
        private Long id; // 작업지시 id
        private String scheduleNo; // schedule 이름
        private String workStatus; // 작업 상태 FIXME
        private Long rejectQuantity; // 총 reject 코일 수 FIXME
        private String planDateTime; // 날짜별 조회를 위해 FIXME
        private String startTime; // 작업시작시간
        private String endTime; // 작업종료시간
        private Long expectedDuration; // 스케줄 예상 시간
        private Long actualDuration; // 실제 스케줄 소요 시간
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info implements DTO {
        private Long id;
        private String no;
    }

}
