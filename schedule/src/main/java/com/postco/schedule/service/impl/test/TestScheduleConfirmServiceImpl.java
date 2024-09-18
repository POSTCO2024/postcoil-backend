package com.postco.schedule.service.impl.test;

import com.postco.schedule.domain.edit.SCHConfirm;
import com.postco.schedule.domain.edit.repo.SCHConfirmRepository;
import com.postco.schedule.presentation.test.SCHConfirmDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestScheduleConfirmServiceImpl {
    private final SCHConfirmRepository schConfirmRepository;

    // 스케쥴 확정 결과 확인
    public List<SCHConfirmDTO.View> getAllConfirmedSchedules() {
        List<SCHConfirm> confirmedSchedules = schConfirmRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();

        return confirmedSchedules.stream()
                .map(confirm -> modelMapper.map(confirm, SCHConfirmDTO.View.class))
                .collect(Collectors.toList());
    }


    // 카프카로 확정된 스케쥴 및 재료 데이터 전송


}
