package com.postco.schedule.presentation;

import com.postco.schedule.presentation.dto.ManagementDTO;
import com.postco.schedule.service.ManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ManagementController {

    private final ManagementService managementService;

    @GetMapping("/{processCode}/{rollUnit}")
    public ManagementDTO getManagementDataByProcessAndUnit(
            @PathVariable String processCode,
            @PathVariable String rollUnit) {

        return managementService.findManagementDataByProcessCodeAndRollUnit(processCode, rollUnit);
    }
}
