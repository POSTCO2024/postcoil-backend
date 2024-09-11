package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.domain.ScheduleConfirm;
import com.postco.schedule.domain.SchedulePlan;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.domain.repository.SchedulePlanRepository;
import com.postco.schedule.domain.repository.ScheduleConfirmRepository;
import com.postco.schedule.presentation.dto.CompositeMaterialDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.presentation.dto.ScheduleResultDTO;
import com.postco.schedule.service.mapper.ScheduleMaterialsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleMaterialsRepository scheduleMaterialsRepository;
    private final SchedulePlanRepository schedulePlanRepository;
    private final ScheduleConfirmRepository scheduleConfirmRepository;

//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private DataInsertService dataInsertService;

    // GET : fs001 Request
    public List<ScheduleMaterialsDTO.View> findMaterialsByProcessCode(String processCode) {
        // TODO: Redis에서 targetId 존재 && processCode == currProc(1CAL or 2CAL) 인 데이터를 가져오기
        // List<CompositeMaterialDTO.Target> targets = redis로 받기
        // List<ScheduleMaterialsDTO.View> scheduleMaterials =  ScheduleMaterialsMapper.mapTargetToView(targets);
        // scheduleMaterials = dataInsertService.insertExpectedItemDuration(scheduleMaterials); // 작업 예상 시간 삽입
        // scheduleMaterialsRepository.save(MapperUtils.mapList(scheduleMaterials, ScheduleMaterials.class));
        // return scheduleMaterials;

        // redis로 가져오면, 삭제하기!
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByCurrProc(processCode);

        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.View.class);
    }

    // POST : fs001 Response
    @Transactional
    public List<ScheduleMaterials> createScheduleWithMaterials(List<Long> materialIds, String processCode) {
        System.out.println("createScheduleWithMaterials 메서드 실행 시작");
        try {
            log.info("Starting createScheduleWithMaterials for processCode: {}", processCode);
            // Step 1: 스케줄링 알고리즘
            // TODO: 롤단위로 먼저 나눠서 스케줄링 알고리즘 돌리기
            List<ScheduleMaterialsDTO.View> materials = schedulingService.planSchedule(materialIds, processCode);

            log.info("Materials after scheduling: {}", materials);

            // Step 2: SchedulePlan 엔티티를 DB에 저장
            SchedulePlan schedulePlan = dataInsertService.createSchedulePlan(materials, processCode);
            SchedulePlan savedSchedulePlan = schedulePlanRepository.save(schedulePlan);
            log.info("Saved SchedulePlan: {}", savedSchedulePlan);

            materials.forEach(material -> {
                material.setScheduleId(savedSchedulePlan.getId());
                material.setScheduleNo(savedSchedulePlan.getNo());
            });

            // Step 3: ScheduleMaterials에 저장 및 TODO: Redis에 scheduleId, ExpectedItemDuration 보내기
            List<ScheduleMaterials> scheduledMaterials = MapperUtils.mapList(materials, ScheduleMaterials.class);
            scheduleMaterialsRepository.saveAll(scheduledMaterials); // sequence update

            log.info("Scheduled materials saved: {}", scheduledMaterials);

            return scheduledMaterials;

        } catch (Exception e) {
            log.error("Error occurred in createScheduleWithMaterials: {}", e.getMessage(), e);
            throw e;  // 트랜잭션 롤백을 위해 예외를 다시 던짐
        }

    }

    // GET : fs002 Request
    public List<ScheduleResultDTO.Info> findSchedulePlanByProcessCode(String processCode){
        return schedulePlanRepository.findByProcessCode(processCode).stream()
                // TODO: 펀성날짜 하루 내로 조회하기
                .filter(schedulePending -> "N".equals(schedulePending.getIsConfirmed()))
                .map(schedulePending -> new ScheduleResultDTO.Info(schedulePending.getId(), schedulePending.getNo()))  // DTO로 매핑
                .collect(Collectors.toList());
    }

    // GET : fs002 Request2
    public List<ScheduleMaterialsDTO.View> findMaterialsByScheduleId(Long scheduleId){
        SchedulePlan schedulePlan = schedulePlanRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("SchedulePlan not found"));

        // SchedulePlan에서 materialIds 가져오기
        List<Long> materialIds = schedulePlan.getMaterialIds();

        // materialIds를 이용하여 Material 정보 조회
        List<ScheduleMaterials> materials = scheduleMaterialsRepository.findByIdIn(materialIds);

        return MapperUtils.mapList(materials, ScheduleMaterialsDTO.View.class);

    }

    // POST : fs002 Request
    @Transactional
    public List<ScheduleMaterialsDTO.View> confirmSchedule(Long scheduleId, List<ScheduleMaterialsDTO.View> materials){

        // 전달받은 새로운 처리 순서 삽입
        List<ScheduleMaterials> scheduleMaterials = MapperUtils.mapList(materials, ScheduleMaterials.class);

        // Step 1: 해당 id로 SchedulePlan 엔터티 찾기
        SchedulePlan schedulePlan = schedulePlanRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("SchedulePlan not found with Id: " + scheduleId));

        schedulePlan.setMaterials(materials);

        // Step 2: isConfirmed 필드를 "Y"로 업데이트
        schedulePlan.setIsConfirmed("Y");
        schedulePlanRepository.save(schedulePlan);

        // Step 3: confirmed Schedule db에 넣기
        ScheduleConfirm scheduleConfirm = dataInsertService.createScheduleConfirm(schedulePlan);
        scheduleConfirmRepository.save(scheduleConfirm);

        // TODO: redis cache-server에 schedulePlan의 scheduleId 삽입~

        scheduleMaterials.forEach(material -> material.setScheduleId(schedulePlan.getId()));
        scheduleMaterialsRepository.saveAll(scheduleMaterials);

        return materials;
    }

    // GET : fs003 Request
    public List<ScheduleResultDTO.Info> findScheduleResultsByProcessCode(String processCode){
        return MapperUtils.mapList(scheduleConfirmRepository.findByProcessCode(processCode), ScheduleResultDTO.Info.class);
    }


    // TODO: !!! 여기서부터 다시 DTO 설계~!!

    // GET : fs003 Request2, fs004 Request2
    public List<ScheduleMaterialsDTO.Result> findScheduledMaterialsByScheduleId(Long scheduleId){
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByScheduleId(scheduleId);
        // TODO: cache-server에서 scheduleMaterials에 있는 id로 정보 가져와서 Mapping하기
        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.Result.class);
    }

    // GET : fs004 Request
    public List<ScheduleResultDTO.Work> findSchedulesByDates(String processCode, String startDate, String endDate){
        // TODO: redis에서 먼저 값을 scheduleResultRepository에 save 하기

        // Validate date format (assuming yyyyMMdd for the dates)
        if (startDate.length() != 6 || endDate.length() != 6) {
            throw new IllegalArgumentException("Dates must be in the format yyMMdd");
        }

        // Convert dates from yyMMdd to LocalDate
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            startLocalDate = LocalDate.parse(startDate, formatter);
            endLocalDate = LocalDate.parse(endDate, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Dates must be in the format yyMMdd", e);
        }

        // Check if startDate is greater than endDate
        if (startLocalDate.isAfter(endLocalDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Fetch results based on date range
        List<ScheduleConfirm> results = scheduleConfirmRepository.findByConfirmDateBetween(startDate, endDate);

        // Convert to DTO
        return MapperUtils.mapList(results, ScheduleResultDTO.Work.class);
    }
}
