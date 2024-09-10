package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.service.DashBoardService;
import com.postco.control.service.TargetMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:4000")
public class DashBoardController {
    private final DashBoardService dashBoardService;

    @Autowired
    public DashBoardController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    @GetMapping("/quality")
    public void getMaterialQuality(){
//        dashBoardService.getMaterialQuality();
    }

}
