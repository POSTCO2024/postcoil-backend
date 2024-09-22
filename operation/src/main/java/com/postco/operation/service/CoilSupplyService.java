package com.postco.operation.service;

import com.postco.operation.domain.entity.CoilSupply;

public interface CoilSupplyService {
    boolean updateCoilSupply(Long workInstructionId, int supplyCount);
    boolean updateRejectCount(Long workInstructionId);
}
