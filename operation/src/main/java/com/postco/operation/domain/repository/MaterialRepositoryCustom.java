package com.postco.operation.domain.repository;

import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;

import java.util.List;

public interface MaterialRepositoryCustom {
    public List<ControlClientDTO.CurrentInfo> getCurrentInfo();
    public List<AnalysisDashboardClientDTO.CurrentInfo> getCurrentInfo(String SchProcess);
}
