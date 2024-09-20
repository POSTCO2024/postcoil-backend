package com.postco.schedule.service;

import com.postco.schedule.presentation.dto.PriorityDTO;

import java.util.List;

public interface PriorityService {
    // 프로세스 코드와 롤 단위로 우선순위 조회
    List<PriorityDTO> findAllByProcessCodeAndRollUnit(String processCode, String rollUnit);

    // 프로세스 코드로 우선순위 조회
    List<PriorityDTO> findAllByProcessCode(String processCode);
}

