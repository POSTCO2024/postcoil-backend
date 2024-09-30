package com.postco.operation.domain.repository;

import com.postco.operation.presentation.dto.websocket.ControlClientDTO;

import java.util.List;

public interface WorkInstructionRepositoryCustom {
    List<ControlClientDTO.StatisticsInfo> getStatisticsInfo();
    List<ControlClientDTO.CurrentInfo> getCurrentInfo();
}
