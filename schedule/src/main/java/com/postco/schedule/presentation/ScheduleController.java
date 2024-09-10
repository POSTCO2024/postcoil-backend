package com.postco.schedule.presentation;

import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.presentation.dto.ScheduleResultDTO;
import com.postco.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<ScheduleMaterialsDTO.Target> findAllMaterials(@PathVariable String processCode){
        return scheduleService.findMaterialsByProcessCode(processCode);
    }

    // POST : fs001 Response
    @PostMapping("/plan/{processCode}")
    public ResponseEntity<String> processScheduleMaterials(@PathVariable String processCode, @RequestBody List<Long> materialIds) {
        List<ScheduleMaterials> scheduleMaterials = scheduleService.createScheduleWithMaterials(materialIds, processCode);
        return ResponseEntity.ok(scheduleMaterials.toString()); // 데이터 확인용
    }

    // GET : fs002 Request
    @GetMapping("/pending/{processCode}")
    public List<ScheduleResultDTO.Info> getSchedulePendingId(@PathVariable String processCode) {
        // processCode가 오면, 편성되었던 롤단위(스케줄id)만 보내기
        return scheduleService.findSchedulePendingsByProcessCode(processCode);
    }

    // GET : fs002 Request2
    @GetMapping("/pending/schedule")
    public List<ScheduleMaterialsDTO.View> getSchedulePendingMaterials(@RequestParam("no") String scheduleNo) {
        // processCode가 오면, (pendingDB) 편성되었던 롤단위(스케줄no)에 해당하는 재료 보내기!
        return scheduleService.findMaterialsByScheduleNo(scheduleNo);
    }

    // POST : fs002 Request
    @PostMapping("/pending/schedule")
    public ResponseEntity<String> confirmSchedule(@RequestParam("no") String scheduleNo, @RequestBody List<ScheduleMaterialsDTO.View> materials) {
        // 스케줄 확정
        return ResponseEntity.ok(scheduleService.confirmSchedule(scheduleNo, materials).toString());
    }

    // GET : fs003 Request
    @GetMapping("/result/{processCode}")
    public List<ScheduleResultDTO.Info> getScheduleResultId(@PathVariable String processCode) {
        // processCode가 오면, 확정되었던 롤단위(스케줄id)만 보내기
        return scheduleService.findScheduleResultsByProcessCode(processCode);
    }

    // GET : fs003 Request2
    @GetMapping("/result/schedule")
    public List<ScheduleMaterialsDTO.Result> getScheduleResultMaterials(@RequestParam("id") Long scheduleId) {
        // processCode가 오면, (확정DB) 확정되었던 롤단위(스케줄id)에 해당하는 재료 보내기!
        return scheduleService.findScheduledMaterialsByScheduleId(scheduleId);
    }

    // TODO: fs003 WebSocket 통신 / (작업완료, 작업대기, 보급대기, 보급완료) 어떻게 보는지

    // GET : fs004 Request
    @GetMapping("/timeline/{processCode}")
    public List<ScheduleResultDTO.Work> getScheduleMaterials(@PathVariable String processCode, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        // TODO: 해당하는 날짜에 생성되었던 schedule 이력 DB 데이터 전송
        return scheduleService.findSchedulesByDates(processCode, startDate, endDate);
    }

    // GET : fs004 Request2
    @GetMapping("/timeline/schedule")
    public List<ScheduleMaterialsDTO.Result> getScheduleMaterials(@RequestParam("id") Long scheduleId) {
        // TODO: scheduleId 보고 workStatus 조회해서 현재 진행중인지, 예정인지, 완료인지 찾기
        return scheduleService.findScheduledMaterialsByScheduleId(scheduleId);
    }

}
