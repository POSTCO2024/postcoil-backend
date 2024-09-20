package com.postco.schedule.service;

import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;

import java.util.List;

public interface ConstraintInsertionService {
    List<ConstraintInsertionDTO> findAllByProcessCodeAndRollUnit(String processCode, String rollUnit);
}
