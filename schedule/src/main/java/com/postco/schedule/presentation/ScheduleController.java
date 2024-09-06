package com.postco.schedule.presentation;

import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    // GET : fs001 Request
    @GetMapping("/plan/{processCode}")
    public List<ScheduleMaterialsDTO.Request> findAllMaterials(@PathVariable String processCode){
        return scheduleService.findMaterialsByProcessCode(processCode);
    }

    // POST : fs001 Response
    @PostMapping("/plan/{processCode}")
    public void processScheduleMaterials(@PathVariable String processCode, @RequestBody List<Long> materialIds) {
        //List<ScheduleMaterialsDTO.View> scheduleMaterials = scheduleService.planSchedule(materialIds, processCode);
       // return ResponseEntity.ok(scheduleMaterials.toString()); // TODO : DTO로 변경하기~
    }

//    // GET : fs002 Request
//    @GetMapping("/pending/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode) {
//        // TODO: processCode가 오면, 편성되었던 롤단위(스케줄id)만 보내기
//        return scheduleService;
//    }
//
//    // GET : fs002 Request
//    @GetMapping("/pending/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode, @RequestParam("id") String id) {
//        // TODO: processCode가 오면, (pendingDB) 편성되었던 롤단위(스케줄id)에 해당하는 재료 보내기!
//        return scheduleService;
//    }
//
//
//    // POST : fs002 Response
//    @PostMapping("/pending/{scheduleList}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials() {
//        // TODO: 스케줄 (편성) db에 있는 스케줄id(A, B, 전체)와 재료 id, seq가 같이 와야함.
//        // TODO: 스케줄 확정짓는 service 구현
//        return scheduleService.;
//    }
//
//    // GET : fs003 Request
//    @GetMapping("/result/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode) {
//        // TODO: processCode가 오면, 확정되었던 롤단위(스케줄id)만 보내기
//        return scheduleService.
//    }
//
//    // GET : fs003 Request
//    @GetMapping("/result/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode, @RequestParam("id") String id) {
//        //  TODO: processCode가 오면, (확정DB) 편성되었던 롤단위(스케줄id)에 해당하는 재료 보내기!
//        return scheduleService.
//    }
//
//    // POST : fs003 Response
//    @GetMapping("/result/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode) {
//        // TODO: 스케줄 (확정) db에 있는 스케줄id(A, B, 전체)와 재료 id, seq가 같이 와야함.
//        // TODO: 스케줄 확정짓는 service 구현
//        return scheduleService.
//    }
//
//    // GET : fs004 Request
//    @GetMapping("/timeline/{processCode}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@PathVariable String processCode, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
//        // TODO: 해당하는 날짜에 생성되었던 schedule 이력 DB 데이터 전송
//        return scheduleService.
//    }
//
//    // GET : fs004 Request
//    @GetMapping("/timeline/{scheduleID}")
//    public List<ScheduleMaterialsDTO.View> getScheduleMaterials(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
//        // TODO: 스케줄 Id에 해당하는 재료들 다 보내기
//        return scheduleService.
//    }

}
