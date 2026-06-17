SET search_path TO minimaya, public;

-- Users
CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Projects
CREATE TABLE project (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    owner_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    default_branch VARCHAR(255) NOT NULL DEFAULT 'main',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- CAS Objects (geometry blobs — stored opaquely)
CREATE TABLE cas_object (
    sha256      VARCHAR(64) PRIMARY KEY,
    size_bytes  BIGINT NOT NULL,
    mime_type   VARCHAR(127) NOT NULL DEFAULT 'application/octet-stream',
    storage_url TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- VCS Trees (scene node references)
CREATE TABLE vcs_tree (
    sha256     VARCHAR(64) PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE vcs_tree_entry (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tree_sha    VARCHAR(64) NOT NULL REFERENCES vcs_tree(sha256) ON DELETE CASCADE,
    node_name   VARCHAR(255) NOT NULL,
    node_id     UUID NOT NULL,
    object_sha  VARCHAR(64) REFERENCES cas_object(sha256)
);

-- VCS Commits
CREATE TABLE vcs_commit (
    sha256      VARCHAR(64) PRIMARY KEY,
    project_id  UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    tree_sha    VARCHAR(64) NOT NULL REFERENCES vcs_tree(sha256),
    parent_sha  VARCHAR(64) REFERENCES vcs_commit(sha256),
    author_id   UUID NOT NULL REFERENCES app_user(id),
    message     TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- VCS Branches
CREATE TABLE vcs_branch (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    head_sha   VARCHAR(64) REFERENCES vcs_commit(sha256),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, name)
);

-- Render Jobs
CREATE TABLE render_job (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id       UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    commit_sha       VARCHAR(64) NOT NULL REFERENCES vcs_commit(sha256),
    user_id          UUID NOT NULL REFERENCES app_user(id),
    status           VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    width            INT NOT NULL DEFAULT 1920,
    height           INT NOT NULL DEFAULT 1080,
    samples          INT NOT NULL DEFAULT 128,
    engine           VARCHAR(32) NOT NULL DEFAULT 'CYCLES',
    use_denoiser     BOOLEAN NOT NULL DEFAULT TRUE,
    preview_samples  INT NOT NULL DEFAULT 16,
    result_asset_key TEXT,
    error_message    TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ
);

CREATE INDEX idx_project_owner ON project(owner_id);
CREATE INDEX idx_commit_project ON vcs_commit(project_id);
CREATE INDEX idx_branch_project ON vcs_branch(project_id);
CREATE INDEX idx_render_job_project ON render_job(project_id);
CREATE INDEX idx_render_job_status ON render_job(status);
