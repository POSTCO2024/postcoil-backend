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

    // GET : fs001에 보내주기

    // POST : fs001에서 보낸 결과 처리
    @PostMapping("/plan/{processCode}")
    public ResponseEntity<String> processScheduleMaterials(@RequestBody List<Long> materialIds, @PathVariable String processCode) {
        List<ScheduleMaterialsDTO.View> scheduleMaterials = scheduleService.executeScheduling(materialIds, processCode);
        return ResponseEntity.ok(scheduleMaterials.toString());
    }
}
