package com.postco.schedule.service.impl;

import com.postco.schedule.domain.SCHMaterial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulingUtils {

    // 폭 기준 내림차순 정렬
    public static List<SCHMaterial> sortedWidthDesc(List<SCHMaterial> coils) {
        return coils.stream()
                .sorted(Comparator.comparingDouble(SCHMaterial::getGoalWidth).reversed())
                .collect(Collectors.toList());
    }

    // 폭 기준 동일폭 기준에 맞춰 그룹핑
    public static List<List<SCHMaterial>> groupByWidth(List<SCHMaterial> sortedCoils, Double standardWidth) {
        List<List<SCHMaterial>> coilGroups = new ArrayList<>();
        List<SCHMaterial> currentCoilGroup = new ArrayList<>();
        SCHMaterial firstCoil = sortedCoils.get(0);
        currentCoilGroup.add(firstCoil);
        double currentBaseWidth = firstCoil.getGoalWidth();

        for (int i = 1; i < sortedCoils.size(); i++) {
            SCHMaterial currentCoil = sortedCoils.get(i);
            if (currentBaseWidth - currentCoil.getGoalWidth() <= standardWidth) {
                currentCoilGroup.add(currentCoil);
            } else {
                coilGroups.add(new ArrayList<>(currentCoilGroup));
                currentCoilGroup.clear();
                currentCoilGroup.add(currentCoil);
                currentBaseWidth = currentCoil.getGoalWidth();
            }
        }
        coilGroups.add(currentCoilGroup);

        return coilGroups;
    }

    // 폭 기준 동일폭 그룹들을 각각 두께 오름차순 정렬
    public static List<List<SCHMaterial>> sortEachGroupByThicknessAsc(List<List<SCHMaterial>> groupCoils) {
        return groupCoils.stream()
                .map(group -> group.stream()
                        .sorted(Comparator.comparingDouble(SCHMaterial::getThickness))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    // 두께 배치에 사인 그래프 적용
    public static List<List<SCHMaterial>> applySineCurveToGroups(List<List<SCHMaterial>> groupCoils) {
        List<List<SCHMaterial>> optimizedGroups = new ArrayList<>();
        double previousEndThickness = 0.0;

        for (List<SCHMaterial> group : groupCoils) {
            final double currentPreviousEndThickness = previousEndThickness;

            List<SCHMaterial> increasingCoils = group.stream()
                    .filter(coil -> coil.getThickness() >= currentPreviousEndThickness)
                    .sorted(Comparator.comparingDouble(coil -> Math.abs(coil.getThickness() - currentPreviousEndThickness)))
                    .collect(Collectors.toList());

            List<SCHMaterial> decreasingCoils = group.stream()
                    .filter(coil -> coil.getThickness() < currentPreviousEndThickness)
                    .sorted(Comparator.comparingDouble(coil -> Math.abs(coil.getThickness() - currentPreviousEndThickness)))
                    .collect(Collectors.toList());

            List<SCHMaterial> sortedGroup = new ArrayList<>(increasingCoils);
            sortedGroup.addAll(decreasingCoils);

            List<SCHMaterial> bestOptimizedGroup = findBestOptimizedGroup(sortedGroup);

            previousEndThickness = bestOptimizedGroup.get(bestOptimizedGroup.size() - 1).getThickness();
            optimizedGroups.add(bestOptimizedGroup);
        }

        return optimizedGroups;
    }

    // 최적화된 그룹 찾기
    private static List<SCHMaterial> findBestOptimizedGroup(List<SCHMaterial> sortedGroup) {
        List<SCHMaterial> bestOptimizedGroup = null;
        double minThicknessDifferenceSum = Double.MAX_VALUE;

        for (int waveType = 1; waveType <= 5; waveType++) {
            double[] sineWave = generateSineWave(sortedGroup.size(), waveType);
            List<SCHMaterial> optimizedGroup = rearrangeBySineWave(sortedGroup, sineWave);
            double thicknessDifferenceSum = calculateThicknessDifferenceSum(optimizedGroup);

            if (thicknessDifferenceSum < minThicknessDifferenceSum) {
                minThicknessDifferenceSum = thicknessDifferenceSum;
                bestOptimizedGroup = optimizedGroup;
            }
        }

        return bestOptimizedGroup;
    }

    // 사인 곡선에 맞게 그룹을 재배치하는 메서드
    private static List<SCHMaterial> rearrangeBySineWave(List<SCHMaterial> sortedGroup, double[] sineWave) {
        List<Integer> sineIndices = new ArrayList<>();
        for (int i = 0; i < sineWave.length; i++) {
            sineIndices.add(i);
        }

        sineIndices.sort(Comparator.comparing(i -> sineWave[i]));

        List<SCHMaterial> optimizedGroup = new ArrayList<>();
        for (int i = 0; i < sortedGroup.size(); i++) {
            optimizedGroup.add(sortedGroup.get(sineIndices.get(i)));
        }

        return optimizedGroup;
    }

    // 두께 차이의 총합 계산
    private static double calculateThicknessDifferenceSum(List<SCHMaterial> group) {
        double thicknessDifferenceSum = 0;
        for (int i = 0; i < group.size() - 1; i++) {
            thicknessDifferenceSum += Math.abs(group.get(i).getThickness() - group.get(i + 1).getThickness());
        }
        return thicknessDifferenceSum;
    }

    // 사인 곡선 생성
    public static double[] generateSineWave(int size, int waveType) {
        double[] sineWave = new double[size];

        switch (waveType) {
            case 1:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = Math.sin(Math.PI * i / (size - 1));
                }
                break;
            case 2:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = Math.sin(2 * Math.PI * i / (size - 1));
                }
                break;
            case 3:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = Math.sin(Math.PI / 2 * i / (size - 1));
                }
                break;
            case 4:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = -Math.sin(Math.PI * i / (size - 1));
                }
                break;
            case 5:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = Math.cos(Math.PI * i / (size - 1));
                }
                break;
            default:
                for (int i = 0; i < size; i++) {
                    sineWave[i] = Math.sin(Math.PI * i / (size - 1));
                }
                break;
        }

        return sineWave;
    }

    // 코일 삽입 가능 여부 검사
    public static boolean canInsertCoil(List<SCHMaterial> finalCoilList, int index, SCHMaterial unassignedCoil, double flagWidth, double flagThickness) {
        if (index == 0) {
            SCHMaterial nextCoil = finalCoilList.get(index);
            return Math.abs(nextCoil.getGoalWidth() - unassignedCoil.getGoalWidth()) <= flagWidth &&
                    Math.abs(nextCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness;
        } else {
            SCHMaterial previousCoil = finalCoilList.get(index - 1);
            SCHMaterial nextCoil = finalCoilList.get(index);
            return (previousCoil.getGoalWidth() + flagWidth) <= unassignedCoil.getGoalWidth() &&
                    unassignedCoil.getGoalWidth() <= (nextCoil.getGoalWidth() + flagWidth) &&
                    Math.abs(previousCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness &&
                    Math.abs(nextCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness;
        }
    }
}
