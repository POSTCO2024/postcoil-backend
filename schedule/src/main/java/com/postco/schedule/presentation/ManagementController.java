package com.postco.schedule.presentation;

import com.postco.schedule.presentation.dto.ManagementDTO;
import com.postco.schedule.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/schedule")
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ManagementController {

    @Autowired
    private ManagementService managementService;

    @GetMapping("/{processCode}/{materialUnitCode}")
    public ManagementDTO getManagementDataByProcessAndUnit(
            @PathVariable String processCode,
            @PathVariable String materialUnitCode) {

        return managementService.findManagementDataByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode);
    }
}
