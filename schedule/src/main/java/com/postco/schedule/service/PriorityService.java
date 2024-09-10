package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.repository.PriorityRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriorityService {

    private final PriorityRepository priorityRepository;

    public List<PriorityDTO> findAllByProcessCodeAndRollUnit(String processCode, String rollUnit){
        return MapperUtils.mapList(priorityRepository.
                findByProcessCodeAndRollUnit(processCode, rollUnit), PriorityDTO.class);
    }

    public List<PriorityDTO> findAllByProcessCode(String processCode){
        return MapperUtils.mapList(priorityRepository.findByProcessCode(processCode), PriorityDTO.class);
    }
}
