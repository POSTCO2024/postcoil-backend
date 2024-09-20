package com.postco.schedule.service.impl;

import com.postco.schedule.domain.SCHConfirm;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.domain.WorkStatus;
import com.postco.schedule.domain.repository.SCHConfirmRepository;
import com.postco.schedule.domain.repository.SCHMaterialRepository;
import com.postco.schedule.infra.kafka.ScheduleProducer;
import com.postco.schedule.presentation.dto.SCHConfirmDTO;
import com.postco.schedule.presentation.SCHForm;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleConfirmServiceImpl {
    private final SCHConfirmRepository schConfirmRepository;
    private final SCHMaterialRepository schMaterialRepository;
    private final ScheduleProducer scheduleProducer;
    private final ModelMapper modelMapper;

    // 스케쥴 확정 결과 확인
    public List<SCHConfirmDTO.View> getAllConfirmedSchedules() {
        List<SCHConfirm> confirmedSchedules = schConfirmRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();

        return confirmedSchedules.stream()
                .map(confirm -> modelMapper.map(confirm, SCHConfirmDTO.View.class))
                .collect(Collectors.toList());
    }

    // IN_PROGRESS 상태인 항목 가져오기
    public List<SCHConfirm> getInProgressSchedules(String processCode) {
        return schConfirmRepository.findByWorkStatusAndProcess(WorkStatus.IN_PROGRESS, processCode);
    }

    // PENDING 상태인 항목을 confirmDate로 정렬하여 가져오기
    public List<SCHConfirm> getPendingSchedulesByConfirmDate(String processCode) {
        return schConfirmRepository.findByWorkStatusAndProcessOrderByConfirmDateAsc(WorkStatus.PENDING, processCode);
    }

    public List<SCHConfirmDTO.View> getAllConfirmedSchedulesBetweenDates(String startDate, String endDate, String processCode) {
        // 문자열을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDateTime = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);

        List<SCHConfirm> confirmedSchedules = schConfirmRepository.findByConfirmDateBetweenAndProcess(startDateTime, endDateTime, processCode);
        ModelMapper modelMapper = new ModelMapper();

        return confirmedSchedules.stream()
                .map(confirm -> modelMapper.map(confirm, SCHConfirmDTO.View.class))
                .collect(Collectors.toList());

    }

    // 스케줄 확정 결과 -> id들 조회, 현재 진행중인 스케줄부터 예정된 스케줄 id list 반환
    public List<SCHForm.Info> getAllConfirmedScheduleIdsFromInProgressToPending(String processCode) {
        // IN_PROGRESS 상태와 PENDING 상태의 스케줄을 모두 가져오기
        List<SCHConfirm> inProgressSchedules = getInProgressSchedules(processCode);
        List<SCHConfirm> pendingSchedules = getPendingSchedulesByConfirmDate(processCode);

        // 두 리스트를 하나로 합치기
        List<SCHConfirm> allConfirmedSchedules = new ArrayList<>();
        allConfirmedSchedules.addAll(inProgressSchedules);
        allConfirmedSchedules.addAll(pendingSchedules);

        return allConfirmedSchedules.stream()
                .map(confirm -> new SCHForm.Info(confirm.getId(), confirm.getScheduleNo()))
                .collect(Collectors.toList());
    }

    public List<SCHMaterialDTO> getScheduleMaterialsByConfirmId(Long confirmId) {
        List<SCHMaterial> schMaterials = schMaterialRepository.findBySchConfirmId(confirmId);

        return schMaterials.stream()
                .map(material -> {
                    SCHMaterialDTO dto = new SCHMaterialDTO();
                    dto.setId(material.getId());
                    dto.setRollUnit(material.getRollUnit());
                    dto.setCurrProc(material.getCurrProc());
                    dto.setTemperature(material.getTemperature());
                    dto.setWidth(material.getWidth());
                    dto.setThickness(material.getThickness());
                    dto.setIsScheduled(material.getIsScheduled());
                    dto.setSequence(material.getSequence());
                    dto.setIsRejected(material.getIsRejected());
                    dto.setExpectedDuration(material.getExpectedDuration());
                    dto.setWorkStatus(String.valueOf(material.getWorkStatus()));
                    dto.setGoalWidth(material.getGoalWidth());
                    dto.setGoalThickness(material.getGoalThickness());
                    dto.setNextProc(material.getNextProc());
                    dto.setMaterialNo(material.getMaterialNo());

                    // SCHConfirm의 id를 scheduleConfirmId 필드에 매핑
                    if (material.getSchConfirm() != null) {
                        dto.setSchedulePlanId(material.getSchConfirm().getId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 카프카 전송
    // 따로 카프카 서비스로 별도로 빼는 것이 좋다.. 수정 예정
    @Transactional(readOnly = true)
    public void sendConfirmedSchedule(Long scheduleId) {
        SCHConfirm confirm = schConfirmRepository.findWithMaterialsById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("확정된 스케줄을 찾을 수 없습니다. ID : " + scheduleId));

        SCHConfirmDTO.View dto = modelMapper.map(confirm, SCHConfirmDTO.View.class);

        List<SCHMaterialDTO> materialDTOs = confirm.getMaterials().stream()
                .map(material -> modelMapper.map(material, SCHMaterialDTO.class))
                .collect(Collectors.toList());

        dto.setMaterials(materialDTOs);

        // 카프카 전송
        scheduleProducer.sendConfirmedSchedule(dto);
    }
}
