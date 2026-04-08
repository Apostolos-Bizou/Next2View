package com.next2me.next2view.repository;

import com.next2me.next2view.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findAllByActiveTrueOrderByName();
    boolean existsByCode(String code);
}
