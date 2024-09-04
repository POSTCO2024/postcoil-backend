package com.postco.control.domain.repository;
import com.postco.control.domain.JoinTables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinTablesRepository extends JpaRepository<JoinTables, Long> {
}
