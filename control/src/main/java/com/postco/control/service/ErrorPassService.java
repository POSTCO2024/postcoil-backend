package com.postco.control.service;

import com.postco.control.domain.repository.TargetMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorPassService {
    private final TargetMaterialRepository targetMaterialRepository;

    /**
     * 에러 패스
     * @param errorMaterialIds
     */
    @Transactional
    public void errorPass(List<Long> errorMaterialIds) {
        int updatedCount = targetMaterialRepository.updateisError(errorMaterialIds);
        System.out.println(updatedCount);
        if (updatedCount == 0) {
            // 업데이트된 행이 없을 경우 로그 출력 (디버깅용)
            log.warn("업데이트된 행이 없습니다. errorMaterialIds: {}", errorMaterialIds);
        }
    }
}
