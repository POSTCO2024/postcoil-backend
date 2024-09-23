package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ErrorPassService {
    private final TargetMaterialRepository targetMaterialRepository;
    private final ModelMapper modelMapper;


    /**
     * 에러 패스
     *
     * @param errorMaterialIds
     */
    public void errorPass(List<Long> errorMaterialIds) {
        targetMaterialRepository.updateisError(errorMaterialIds);
    }

    public TargetMaterialDTO errorComment(Long id, String comment) {
        TargetMaterial targetMaterial = targetMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("다음과 같은 작업대상재가 존재하지 않습니다. " + id));
        targetMaterial.setRemarks(comment);
        return modelMapper.map(targetMaterialRepository.save(targetMaterial), TargetMaterialDTO.class);
    }
}
