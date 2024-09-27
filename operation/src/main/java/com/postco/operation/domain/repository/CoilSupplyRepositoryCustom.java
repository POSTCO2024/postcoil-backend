package com.postco.operation.domain.repository;

import com.postco.operation.presentation.dto.websocket.ControlClientDTO;

import java.util.List;

public interface CoilSupplyRepositoryCustom {
    List<ControlClientDTO.TotalSupply> getTotalSupplyByProcess();
}
