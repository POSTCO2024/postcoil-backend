package com.postco.control.service;

import com.postco.control.presentation.dto.response.CriteriaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class CriteriaServiceTest {
    @Autowired
    CriteriaService criteriaService;

    @Test
    void 공정별_에러기준_조회() throws Exception {
        // given
        String processCode = "1PCM";

        // when
        CriteriaDTO result = criteriaService.findErrorCriteriaByProcessCode(processCode);

        // then
        assertNotNull(result);
        assertEquals(processCode, result.getProcessCode());
        assertFalse(result.getCriteriaDetails().isEmpty());
    }

    @Test
    void 공정별_추출기준_조회() throws Exception {
        // given
        String processCode = "2CAL";

        // when
        CriteriaDTO result = criteriaService.findExtractionCriteriaByProcessCode(processCode);

        // then
        assertNotNull(result);
        assertEquals(processCode, result.getProcessCode());
        assertFalse(result.getCriteriaDetails().isEmpty());

    }
}