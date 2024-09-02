package com.postco.control.presentation;

import com.postco.control.domain.Materials;
import com.postco.control.service.ControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;

@RestController
@RequestMapping("/control")
public class ControlController {

    private final ControlService controlService;

    @Autowired
    public ControlController(ControlService controlService) {
        this.controlService = controlService;
    }

    /**
     * 조건에 맞는 Orders를 반환하는 엔드포인트
     *
     * @return 조건에 맞는 Orders 리스트
     */
    @GetMapping("/target")
    public List<Materials> getFilteredMaterials() {
        return controlService.getFilteredMaterials();
    }
}