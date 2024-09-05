package com.postco.control.domain.repository;

import com.postco.control.domain.TargetMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetMaterialRepository extends JpaRepository<TargetMaterial, Long> {
    List<TargetMaterial> findByIsError(String isError);
}
