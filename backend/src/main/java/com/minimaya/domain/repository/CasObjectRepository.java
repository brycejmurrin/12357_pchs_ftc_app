package com.minimaya.domain.repository;

import com.minimaya.domain.model.CasObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasObjectRepository extends JpaRepository<CasObject, String> {
    boolean existsBySha256(String sha256);
}
