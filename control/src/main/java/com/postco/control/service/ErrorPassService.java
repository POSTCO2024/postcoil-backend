package com.postco.control.service;

import com.postco.control.domain.repository.TargetMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ErrorPassService {
    private final TargetMaterialRepository targetMaterialRepository;

    /**
     * 에러 패스
     * @param errorMaterialIds
     */
    public void errorPass(List<Long> errorMaterialIds) {
        targetMaterialRepository.updateisError(errorMaterialIds);
    }
}
