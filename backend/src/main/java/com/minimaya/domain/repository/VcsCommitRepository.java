package com.minimaya.domain.repository;

import com.minimaya.domain.model.VcsCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface VcsCommitRepository extends JpaRepository<VcsCommit, String> {
    List<VcsCommit> findByProjectIdOrderByCreatedAtDesc(java.util.UUID projectId, Pageable pageable);
}
