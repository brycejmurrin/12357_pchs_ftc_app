package com.minimaya.domain.repository;

import com.minimaya.domain.model.VcsBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VcsBranchRepository extends JpaRepository<VcsBranch, UUID> {
    List<VcsBranch> findByProjectId(UUID projectId);
    Optional<VcsBranch> findByProjectIdAndName(UUID projectId, String name);
}
