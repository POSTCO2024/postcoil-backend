package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
=======
>>>>>>> c6b813b (Feat: 에러재 댓글 추가ê¸ °기능 구현)
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorPassService {
    private final TargetMaterialRepository targetMaterialRepository;
    private final ModelMapper modelMapper;


    /**
     * 에러 패스
     *
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

    public TargetMaterialDTO errorComment(Long id, String comment) {
        TargetMaterial targetMaterial = targetMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("다음과 같은 작업대상재가 존재하지 않습니다. " + id));
        targetMaterial.setRemarks(comment);
        return modelMapper.map(targetMaterialRepository.save(targetMaterial), TargetMaterialDTO.class);
    }
}
