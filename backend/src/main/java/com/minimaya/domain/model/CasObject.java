package com.minimaya.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cas_object", schema = "minimaya")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CasObject {

    // SHA-256 hash is the primary key.
    @Id
    @Column(name = "sha256", length = 64)
    private String sha256;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "storage_url", nullable = false)
    private String storageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
