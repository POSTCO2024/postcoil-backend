package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.domain.SchedulePending;
import com.postco.schedule.domain.ScheduleResult;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.domain.repository.SchedulePendingRepository;
import com.postco.schedule.domain.repository.ScheduleResultRepository;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.presentation.dto.ScheduleResultDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleMaterialsRepository scheduleMaterialsRepository;
    private final SchedulePendingRepository schedulePendingRepository;
    private final ScheduleResultRepository scheduleResultRepository;

//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SchedulingService schedulingService;

    // GET : fs001 Request
    public List<ScheduleMaterialsDTO.Target> findMaterialsByProcessCode(String processCode) {
        // TODO: Redis에서 processCode == currProc 인 데이터를 가져오기
        // List<ScheduleMaterialsDTO.Target> materials = redisTemplate.opsForValue().get("allMaterials");

        // 데이터 불러오기 (삭제예정)
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByCurProc(processCode);
        log.info("===fs001 REQUEST===");
        // TODO: 데이터 자체로 넘기기
        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.Target.class);
    }

    // POST : fs001 Response
    @Transactional
    public List<ScheduleMaterials> createScheduleWithMaterials(List<Long> materialIds, String processCode) {
        System.out.println("createScheduleWithMaterials 메서드 실행 시작");
        try {
            log.info("Starting createScheduleWithMaterials for processCode: {}", processCode);
            // Step 1: 스케줄링 알고리즘
            List<ScheduleMaterialsDTO.View> materials = schedulingService.planSchedule(materialIds, processCode);

            log.info("Materials after scheduling: {}", materials);

            // Step 2: 스케줄ID 생성
            SchedulePending schedulePending = new SchedulePending();
            schedulePending.setMaterials(materials);
            schedulePending.setCurProc(processCode);

            LocalDateTime now = LocalDateTime.now();
            schedulePending.setPlanDateTime(now.format(DateTimeFormatter.ofPattern("yyMMddHHmm")));

            ScheduleMaterialsDTO.View firstMaterial = materials.get(0); // 첫 번째 Material 사용 (다른 로직이 필요하면 변경 가능)

            schedulePending.setNo(firstMaterial.getCurProc() + now.format(DateTimeFormatter.ofPattern("yyMMddHHmmssnnn")) + firstMaterial.getRollUnit());
            schedulePending.setTargetQuantity((long) materials.size());

            // SchedulePending 엔티티를 DB에 저장
            SchedulePending savedSchedulePending = schedulePendingRepository.save(schedulePending);
            log.info("Saved SchedulePending: {}", savedSchedulePending);

            materials = schedulingService.insertMaterialsWithWorkTime(materials); // workTime 계산
            materials.forEach(material -> material.setScheduleNo(savedSchedulePending.getNo()));
            // TODO: Step 3: 변환 후 scheduleMaterialsRepository에 save 하기
            List<ScheduleMaterials> scheduledMaterials = MapperUtils.mapList(materials, ScheduleMaterials.class);
            scheduleMaterialsRepository.saveAll(scheduledMaterials);

            log.info("Scheduled materials saved: {}", scheduledMaterials);

            return scheduledMaterials;

        } catch (Exception e) {
            log.error("Error occurred in createScheduleWithMaterials: {}", e.getMessage(), e);
            throw e;  // 트랜잭션 롤백을 위해 예외를 다시 던짐
        }

    }

    // GET : fs002 Request
    public List<ScheduleResultDTO.Info> findSchedulePendingsByProcessCode(String processCode){
        return schedulePendingRepository.findByCurProc(processCode).stream()
                .filter(schedulePending -> "N".equals(schedulePending.getIsConfirmed()))
                .map(schedulePending -> new ScheduleResultDTO.Info(schedulePending.getId(), schedulePending.getNo()))  // DTO로 매핑
                .collect(Collectors.toList());
    }

    // GET : fs002 Request2
    public List<ScheduleMaterialsDTO.View> findMaterialsByScheduleNo(String scheduleNo){
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByScheduleNo(scheduleNo);
        // TODO: cache-server에서 scheduleMaterials에 있는 id로 정보 가져와서 Mapping하기
        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.View.class);
    }

    // POST : fs002 Request
    @Transactional
    public List<ScheduleMaterialsDTO.View> confirmSchedule(String scheduleNo, List<ScheduleMaterialsDTO.View> materials){

        // 전달받은 새로운 처리 순서 삽입
        List<ScheduleMaterials> scheduleMaterials = MapperUtils.mapList(materials, ScheduleMaterials.class);

        // Step 1: 해당 id로 SchedulePending 엔터티 찾기
        SchedulePending schedulePending = schedulePendingRepository.findByNo(scheduleNo)
                .orElseThrow(() -> new EntityNotFoundException("SchedulePending not found with No: " + scheduleNo));

        schedulePending.setMaterials(materials);

        // Step 2: isConfirmed 필드를 "Y"로 업데이트
        schedulePending.setIsConfirmed("Y");

        // Step 3: 엔터티를 저장하여 DB에 반영
        schedulePendingRepository.save(schedulePending);

        ScheduleResult scheduleResult = new ScheduleResult();

        scheduleResult.setNo(schedulePending.getNo());
        scheduleResult.setCurProc(schedulePending.getCurProc());
        scheduleResult.setPlanDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm")));

        scheduleResultRepository.save(scheduleResult);

        // TODO: redis cache-server에 scheduleResult의 scheduleId 넣기

        scheduleMaterials.forEach(material -> material.setScheduleId(scheduleResult.getId()));
        scheduleMaterialsRepository.saveAll(scheduleMaterials);

        return materials;
    }

    // GET : fs003 Request
    public List<ScheduleResultDTO.Info> findScheduleResultsByProcessCode(String processCode){
        return MapperUtils.mapList(scheduleResultRepository.findByCurProc(processCode), ScheduleResultDTO.Info.class);
    }

    // GET : fs003 Request2, fs004 Request2
    public List<ScheduleMaterialsDTO.Result> findScheduledMaterialsByScheduleId(Long scheduleId){
        List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByScheduleId(scheduleId);
        // TODO: cache-server에서 scheduleMaterials에 있는 id로 정보 가져와서 Mapping하기
        return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.Result.class);
    }

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
        List<ScheduleResult> results = scheduleResultRepository.findByPlanDateTimeBetween(startDate, endDate);

        // Convert to DTO
        return MapperUtils.mapList(results, ScheduleResultDTO.Work.class);
    }
}
