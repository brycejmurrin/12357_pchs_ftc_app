package com.minimaya.api.dto;

import com.minimaya.domain.model.Project;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    UUID ownerId,
    String defaultBranch,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
            p.getId(), p.getName(), p.getDescription(),
            p.getOwner().getId(), p.getDefaultBranch(),
            p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
