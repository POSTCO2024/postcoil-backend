package com.postco.control.service.impl;

import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.core.dto.MaterialDTO;

import java.util.Map;

public class ProcessCounter {
    public static void countNextProc(MaterialDTO.View view, Map<String, Fc001aDTO.Table> resultMap) {
        String coilTypeCode = view.getCoilTypeCode();
        String nextProc = view.getNextProc();

        // 기존에 있던 테이블을 가져오거나 새로 생성
        Fc001aDTO.Table table = resultMap.getOrDefault(coilTypeCode, new Fc001aDTO.Table(coilTypeCode, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));

        // 각 공정에 맞게 Count
        switch (nextProc) {
            case "1CAL":
                table.setProc1CAL(table.getProc1CAL() + 1);
                break;
            case "2CAL":
                table.setProc2CAL(table.getProc2CAL() + 1);
                break;
            case "1EGL":
                table.setProc1EGL(table.getProc1EGL() + 1);
                break;
            case "2EGL":
                table.setProc2EGL(table.getProc2EGL() + 1);
                break;
            case "1CGL":
                table.setProc1CGL(table.getProc1CGL() + 1);
                break;
            case "2CGL":
                table.setProc2CGL(table.getProc2CGL() + 1);
                break;
            case "101":
                table.setProc1Packing(table.getProc1Packing() + 1);
                break;
            case "201":
                table.setProc2Packing(table.getProc2Packing() + 1);
                break;
        }

        // 총 합계 (Total Cnt) 계산하기
        table.setTotalCnt(table.getTotalCnt() + 1);

        resultMap.put(coilTypeCode, table); // HashMap에 저장
    }
}
