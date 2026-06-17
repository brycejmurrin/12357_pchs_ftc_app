package com.minimaya.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vcs_commit", schema = "minimaya")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VcsCommit {

    // SHA-256 hash is the PK — content-addressable.
    @Id
    @Column(name = "sha256", length = 64)
    private String sha256;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "tree_sha", length = 64, nullable = false)
    private String treeSha;

    @Column(name = "parent_sha", length = 64)
    private String parentSha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(nullable = false)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
