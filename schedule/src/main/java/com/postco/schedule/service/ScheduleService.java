package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.domain.ScheduleConfirm;
import com.postco.schedule.domain.SchedulePlan;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.domain.repository.SchedulePlanRepository;
import com.postco.schedule.domain.repository.ScheduleConfirmRepository;
import com.postco.schedule.domain.test.SCHMaterial;
import com.postco.schedule.domain.test.repo.SCHMaterialRepository;
import com.postco.schedule.domain.test.repo.SCHPlanRepository;
import com.postco.schedule.presentation.dto.CompositeMaterialDTO;
import com.postco.schedule.presentation.test.SCHMaterialDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import com.postco.schedule.presentation.dto.ScheduleResultDTO;
import com.postco.schedule.service.mapper.ScheduleMaterialsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final SCHMaterialRepository schMaterialRepository;
    private final SCHPlanRepository schPlanRepository;
    private final ModelMapper modelMapper;

    // GET : fs001 Request
    public List<SCHMaterialDTO> findMaterialsByProcessCode(String processCode) {
        // TODO (Yerim): Redis에서 targetId 존재, scheduleId가 비존재 && processCode == currProc(1CAL or 2CAL) 인 데이터를 가져오기
        // List<CompositeMaterialDTO.Target> targets = redis로 받기
        // List<ScheduleMaterialsDTO.View> scheduleMaterials =  ScheduleMaterialsMapper.mapTargetToView(targets);
        // scheduleMaterials = dataInsertService.insertExpectedItemDuration(scheduleMaterials); // 작업 예상 시간 삽입
        // TODO: repository에서 targetId를 보고 이미 있는 거면 추가 안하고 없는 것만 추가하기!
        // scheduleMaterialsRepository.save(MapperUtils.mapList(scheduleMaterials, ScheduleMaterials.class));
        // ScheduleMaterial currentScheduledMaterials = scheduleMaterialsRepository.findByScheduleIdIsNullAndCurrProc(String processCode);
        // return MapperUtils.mapList(currentScheduledMaterials, ScheduleMaterialsDTO.View.class);

        // TODO: redis로 가져오면, 삭제하기!
        //List<ScheduleMaterials> scheduleMaterials = scheduleMaterialsRepository.findAllByCurrProc(processCode);
        //return MapperUtils.mapList(scheduleMaterials, ScheduleMaterialsDTO.View.class);


        return MapperUtils.mapList(schMaterialRepository.findBySchPlanIsNullAndSchConfirmIsNullAndCurrProc(processCode), SCHMaterialDTO.class);
    }

    // POST : fs001 Response
    @Transactional
    public List<ScheduleMaterials> createScheduleWithMaterials(List<Long> materialIds, String processCode) {
        System.out.println("createScheduleWithMaterials 메서드 실행 시작");
        List<ScheduleMaterials> allScheduledMaterials = new ArrayList<>();
        // Step 1: materialIds에 해당하는 ScheduleMaterials 조회
        List<ScheduleMaterials> materials = scheduleMaterialsRepository.findAllById(materialIds);
        // Step 2: rollUnitName별로 materialIds를 그룹화
        Map<String, List<ScheduleMaterials>> materialsGroupedByRollUnitName = materials.stream()
                .collect(Collectors.groupingBy(
                        ScheduleMaterials::getOpCode,  // rollUnitName으로 그룹화 -> rollUnitName 일단 null이라서 로직 돌리기 위해 코일타입으로 바꿔놓음
                        Collectors.toList() // 각 그룹에 속하는 material의 id만 추출
                ));

        log.info("Materials grouped by rollUnitName: {}", materialsGroupedByRollUnitName);

//        for (Map.Entry<String, List<ScheduleMaterials>> entry : materialsGroupedByRollUnitName.entrySet()) {
//            String rollUnitName = entry.getKey();
//            List<ScheduleMaterials> groupedMaterials = entry.getValue();
//
//            log.info("Executing planSchedule for rollUnitName: {} with materialIds: {}", rollUnitName, groupedMaterials.stream().map(ScheduleMaterials::getId).collect(Collectors.toList()));
//
//            // rollUnitName별로 planSchedule 호출
//            List<ScheduleMaterialsDTO.View> scheduledMaterials = schedulingService.planSchedule(MapperUtils.mapList(groupedMaterials, ScheduleMaterialsDTO.View.class), processCode);
//
//            log.info("Materials after scheduling: {}", scheduledMaterials);
        for (Map.Entry<String, List<ScheduleMaterials>> entry : materialsGroupedByRollUnitName.entrySet()) {
            String rollUnitName = entry.getKey();
            List<ScheduleMaterials> groupedMaterials = entry.getValue();

            log.info("Executing planSchedule for rollUnitName: {} with materialIds: {}", rollUnitName, groupedMaterials.stream().map(ScheduleMaterials::getId).collect(Collectors.toList()));

            // ScheduleMaterials의 id를 추출하여 List<Long>으로 변환
            List<Long> materialId = groupedMaterials.stream()
                    .map(ScheduleMaterials::getId) // ScheduleMaterials에서 id 추출
                    .collect(Collectors.toList());

            // planSchedule 호출 시 List<Long>을 전달
            List<ScheduleMaterialsDTO.View> scheduledMaterials = schedulingService.planSchedule(materialId, processCode);

            log.info("Materials after scheduling: {}", scheduledMaterials);

            // Step 3: SchedulePlan 엔티티를 DB에 저장
            SchedulePlan schedulePlan = dataInsertService.createSchedulePlan(scheduledMaterials, processCode);
            SchedulePlan savedSchedulePlan = schedulePlanRepository.save(schedulePlan);
            log.info("Saved SchedulePlan: {}", savedSchedulePlan.getMaterialIds());

            scheduledMaterials.forEach(material -> {
                material.setScheduleId(savedSchedulePlan.getId());
                material.setScheduleNo(savedSchedulePlan.getNo());
            });

            // Step 4: ScheduleMaterials에 저장
            List<ScheduleMaterials> scheduledMaterialsToEntity = MapperUtils.mapList(scheduledMaterials, ScheduleMaterials.class);

            // 확인: 저장하기 전 로그 출력
            scheduledMaterialsToEntity.forEach(material -> {
                log.info("Saving material: {}", material);
            });

            // Step 5: Update
            scheduleMaterialsRepository.saveAll(scheduledMaterialsToEntity); // sequence update

            log.info("Scheduled materials saved: {}", scheduledMaterials);

            // 각 그룹의 결과를 전체 결과 리스트에 추가
            allScheduledMaterials.addAll(scheduledMaterialsToEntity);
        }
        return allScheduledMaterials;

    }

    // GET : fs002 Request
    public List<ScheduleResultDTO.Info> findSchedulePlanByProcessCode(String processCode){
        return schedulePlanRepository.findByProcessCode(processCode).stream()
                .filter(schedulePlan -> "N".equals(schedulePlan.getIsConfirmed()))
                .filter(schedulePlan -> {
                            // planDate가 현재 날짜를 기준으로 하루 전 것부터만 보이게 구현
                            LocalDate today = LocalDate.now();
                            LocalDate oneDayAgo = today.minusDays(1); // 하루 전 날짜 계산
                            LocalDate planDate = schedulePlan.getPlanDate();
                            return planDate != null && (planDate.isAfter(oneDayAgo) || planDate.isEqual(oneDayAgo))
                                    && (planDate.isBefore(today) || planDate.isEqual(today));
                        }
                )
                .map(schedulePlan -> new ScheduleResultDTO.Info(schedulePlan.getId(), schedulePlan.getNo()))  // DTO로 매핑
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

        // Step 1: 해당 id로 SchedulePlan 엔터티 찾기
        SchedulePlan schedulePlan = schedulePlanRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("SchedulePlan not found with Id: " + scheduleId));

        // Step 2: isConfirmed 필드를 "Y"로 업데이트
        schedulePlan.setIsConfirmed("Y");
        schedulePlanRepository.save(schedulePlan);

        // Step 3: confirmed Schedule db에 넣기
        ScheduleConfirm scheduleConfirm = dataInsertService.createScheduleConfirm(schedulePlan);
        scheduleConfirmRepository.save(scheduleConfirm);

        // Step 4: 기존 ScheduleMaterials 엔터티 가져오기 및 업데이트할 필드만 수정
        List<Long> materialIds = materials.stream().map(ScheduleMaterialsDTO.View::getId).collect(Collectors.toList());
        List<ScheduleMaterials> existingMaterials = scheduleMaterialsRepository.findAllById(materialIds);

        for (ScheduleMaterialsDTO.View materialDto : materials) {
            existingMaterials.stream()
                    .filter(existing -> existing.getId().equals(materialDto.getId()))
                    .forEach(existing -> {
                        existing.setSequence(materialDto.getSequence());  // sequence 업데이트
                        existing.setScheduleId(schedulePlan.getId());     // scheduleId 업데이트
                    });
        }

        scheduleMaterialsRepository.saveAll(existingMaterials);


        // TODO (Yerim):
        //        - redis cache-server 에
        //           schedulePlan의 id, no, processCode, quantity, materialIds, expectedDuration 와
        //           scheduleConfirm의 confirmDate, confirmManager 보내기
        //        - redis cache-server 에서 재료(MapperUtils.mapList(existingMaterials, ~~DTO.class)에 해당하는 sequence 삽입 및 scheduleId 컬럼을 schedulePlan의 id값 삽입


        return materials;
    }

    // TODO: !!! 여기서부터 다시 DTO 설계~!!

    // GET : fs003 Request - 드롭박스를 위한 스케줄이름 데이터
    public List<ScheduleResultDTO.Info> findScheduleResultsByProcessCode(String processCode){
        // TODO (Yerim, Ash): processCode랑도 맞지만 현재 작업중인 것부터 예정인 것만 보내야함!
        //       Redis에서 workInstruction을 보고 요청. 거기의 startTime이 없는 것이나 현재 시간 이후의 값인 것들과,
        //       현재시간보다 이전의 startTime을 가지고 있으면 endTime이 없거나 endTime이 현재 시간 이후 값인 경우 가져오게 하기
        //
        return MapperUtils.mapList(scheduleConfirmRepository.findByProcessCode(processCode), ScheduleResultDTO.Info.class);
    }


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
