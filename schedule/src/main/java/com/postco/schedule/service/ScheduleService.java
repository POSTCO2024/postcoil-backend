package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    // TODO: Repository -> Redis 에서 불러오기1
    private final ScheduleMaterialsRepository scheduleMaterialsRepository;

//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;

    private final PriorityService priorityService;
    private final ConstraintInsertionService constraintInsertionService;

    private final MaterialsService materialsService;

    public List<ScheduleMaterialsDTO.Request> findMaterialsByProcessCode(String processCode) {
        // TODO: Redis에서 데이터를 가져와 기존 데이터 삭제하고 리스트에 추가
//        List<ScheduleMaterialsDTO.Request> materials = redisTemplate.opsForValue().get("allMaterials");

        // 데이터 불러오기
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByCurProcCode(processCode);
        // 불러온 데이터에 work_time 계산해서 넣기!
        materialsService.insertMaterialsWithWorkTime(scheduleMaterials);

        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.Request.class);
    }


}
