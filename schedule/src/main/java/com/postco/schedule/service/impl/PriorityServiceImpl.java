package com.postco.schedule.service.impl;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.repository.PriorityRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.service.PriorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorityServiceImpl implements PriorityService {

    private final PriorityRepository priorityRepository;

    public List<PriorityDTO> findAllByProcessCodeAndRollUnit(String processCode, String rollUnit){
        return MapperUtils.mapList(priorityRepository.
                findByProcessCodeAndRollUnit(processCode, rollUnit), PriorityDTO.class);
    }

    public List<PriorityDTO> findAllByProcessCode(String processCode){
        return MapperUtils.mapList(priorityRepository.findByProcessCode(processCode), PriorityDTO.class);
    }
}
