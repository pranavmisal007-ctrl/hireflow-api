package com.hireflow.repository;

import com.hireflow.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findBySlug(String slug);
    Optional<Company> findByName(String name);
    boolean existsByName(String name);
    Page<Company> findAll(Pageable pageable);
}
