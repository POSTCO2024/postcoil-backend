package com.postco.schedule.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.presentation.dto.ScheduleResultDTO;
import com.postco.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ScheduleController {

    private final ScheduleService scheduleService;

    // GET : fs001 Request
    @GetMapping("/plan/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterialsDTO.View>>> findAllMaterials(@PathVariable String processCode){
        List<ScheduleMaterialsDTO.View> results = scheduleService.findMaterialsByProcessCode(processCode);

        ApiResponseDTO<List<ScheduleMaterialsDTO.View>> response = ApiResponseDTO.<List<ScheduleMaterialsDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // POST : fs001 Response - ScheduleMaterials 생성
    @PostMapping("/plan/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterials>>> processScheduleMaterials(@PathVariable String processCode, @RequestBody List<Long> materialIds) {
        // 요청 받은 materialIds와 processCode를 이용해 results 생성
        List<ScheduleMaterials> results = scheduleService.createScheduleWithMaterials(materialIds, processCode);

        ApiResponseDTO<List<ScheduleMaterials>> response = ApiResponseDTO.<List<ScheduleMaterials>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs002 Request
    @GetMapping("/pending/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<ScheduleResultDTO.Info>>> getSchedulePlanId(@PathVariable String processCode) {
        // processCode가 오면, 편성되었던 롤단위(스케줄id)만 보내기
        List<ScheduleResultDTO.Info> results = scheduleService.findSchedulePlanByProcessCode(processCode);

        ApiResponseDTO<List<ScheduleResultDTO.Info>> response = ApiResponseDTO.<List<ScheduleResultDTO.Info>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs002 Request2
    @GetMapping("/pending/schedule")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterialsDTO.View>>> getSchedulePlanMaterials(@RequestParam("id") Long scheduleId) {
        List<ScheduleMaterialsDTO.View> results = scheduleService.findMaterialsByScheduleId(scheduleId);

        ApiResponseDTO<List<ScheduleMaterialsDTO.View>> response = ApiResponseDTO.<List<ScheduleMaterialsDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // POST : fs002 Request
    @PostMapping("/pending/schedule")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterialsDTO.View>>> confirmSchedule(@RequestParam("id") Long scheduleId, @RequestBody List<ScheduleMaterialsDTO.View> materials) {
        List<ScheduleMaterialsDTO.View> results = scheduleService.confirmSchedule(scheduleId, materials);

        ApiResponseDTO<List<ScheduleMaterialsDTO.View>> response = ApiResponseDTO.<List<ScheduleMaterialsDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs003 Request
    @GetMapping("/result/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<ScheduleResultDTO.Info>>> getScheduleResultsId(@PathVariable String processCode) {
        List<ScheduleResultDTO.Info> results = scheduleService.findScheduleResultsByProcessCode(processCode);

        ApiResponseDTO<List<ScheduleResultDTO.Info>> response = ApiResponseDTO.<List<ScheduleResultDTO.Info>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs003 Request2
    @GetMapping("/result/schedule")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterialsDTO.Result>>> getScheduleResultMaterials(@RequestParam("id") Long scheduleId) {
        List<ScheduleMaterialsDTO.Result> results = scheduleService.findScheduledMaterialsByScheduleId(scheduleId);

        ApiResponseDTO<List<ScheduleMaterialsDTO.Result>> response = ApiResponseDTO.<List<ScheduleMaterialsDTO.Result>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // TODO: fs003 WebSocket 통신 / (작업완료, 작업대기, 보급대기, 보급완료) 어떻게 보는지

    // GET : fs004 Request
    @GetMapping("/timeline/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<ScheduleResultDTO.Work>>> getScheduleResultsIdByDates(@PathVariable String processCode, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        // TODO: 해당하는 날짜에 생성되었던 schedule 이력 DB 데이터 전송
        List<ScheduleResultDTO.Work> results = scheduleService.findSchedulesByDates(processCode, startDate, endDate);

        ApiResponseDTO<List<ScheduleResultDTO.Work>> response = ApiResponseDTO.<List<ScheduleResultDTO.Work>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs004 Request2
    @GetMapping("/timeline/schedule")
    public ResponseEntity<ApiResponseDTO<List<ScheduleMaterialsDTO.Result>>> getScheduleMaterials(@RequestParam("id") Long scheduleId) {
        List<ScheduleMaterialsDTO.Result> results = scheduleService.findScheduledMaterialsByScheduleId(scheduleId);

        ApiResponseDTO<List<ScheduleMaterialsDTO.Result>> response = ApiResponseDTO.<List<ScheduleMaterialsDTO.Result>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }
}
