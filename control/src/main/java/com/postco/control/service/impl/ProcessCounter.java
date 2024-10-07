package com.postco.control.service.impl;

import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.core.dto.MaterialDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ProcessCounter {
    public static void countNextProc(Fc001aDTO.Table tableEntry, String nextProc) {
        switch (nextProc) {
            case "1CAL":
                tableEntry.setProc1CAL(tableEntry.getProc1CAL() + 1);
                break;
            case "2CAL":
                tableEntry.setProc2CAL(tableEntry.getProc2CAL() + 1);
                break;
            case "1EGL":
                tableEntry.setProc1EGL(tableEntry.getProc1EGL() + 1);
                break;
            case "2EGL":
                tableEntry.setProc2EGL(tableEntry.getProc2EGL() + 1);
                break;
            case "2CGL":
                tableEntry.setProc2EGL(tableEntry.getProc2CGL() + 1);
                break;
            case "101":
                tableEntry.setProc1Packing(tableEntry.getProc1Packing() + 1);
                break;
            case "201":
                tableEntry.setProc2Packing(tableEntry.getProc2Packing() + 1);
                break;
            // 다른 차공정이 추가되면 여기에 계속 추가
            default:
                log.warn("알 수 없는 차공정: " + nextProc);
                break;
        }
    }
}
