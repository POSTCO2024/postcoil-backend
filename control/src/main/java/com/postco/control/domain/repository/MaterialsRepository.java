package com.postco.control.domain.repository;

import com.postco.control.domain.Materials;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MaterialsRepository extends JpaRepository<Materials, Long>  {
    List<Materials> findAllByfCodeAndStatusAndProgressAndCurrProc(String fCode, String status, String progress, String currProc);
}
