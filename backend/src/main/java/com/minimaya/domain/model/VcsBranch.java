package com.minimaya.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "vcs_branch",
    schema = "minimaya",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "name"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VcsBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(name = "head_sha", length = 64)
    private String headSha;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
