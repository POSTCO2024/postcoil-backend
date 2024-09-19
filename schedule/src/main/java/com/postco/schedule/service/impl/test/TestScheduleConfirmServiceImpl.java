package com.postco.schedule.service.impl.test;

import com.postco.schedule.domain.test.SCHConfirm;
import com.postco.schedule.domain.test.SCHMaterial;
import com.postco.schedule.domain.test.repo.SCHConfirmRepository;
import com.postco.schedule.domain.test.repo.SCHMaterialRepository;
import com.postco.schedule.infra.kafka.ScheduleProducer;
import com.postco.schedule.presentation.test.SCHConfirmDTO;
import com.postco.schedule.presentation.test.SCHMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestScheduleConfirmServiceImpl {
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
