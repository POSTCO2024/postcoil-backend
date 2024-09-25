package com.postco.operation.service;

public interface EquipmentStatusService {
    boolean startEquipment(String process);
    boolean stopEquipment(Long equipmentId);
    boolean brokenEquipment(Long equipmentId);
    long getCurrentOperationalTime(Long equipmentId);
}
