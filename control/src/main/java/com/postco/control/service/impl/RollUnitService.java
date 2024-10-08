package com.postco.control.service.impl;

import com.postco.control.domain.RollUnit;
import com.postco.control.domain.repository.RollUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RollUnitService {
    private final RollUnitRepository rollUnitRepository;

    public String determineRollUnit(double thickness) {
        List<RollUnit> rollUnits = rollUnitRepository.findAll();

        return rollUnits.stream()
                .filter(rollUnit -> rollUnit.isInRange(thickness))
                .findFirst()
                .map(RollUnit::getRollUnitName)
                .orElse("A");
    }
}
