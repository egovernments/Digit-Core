CREATE TABLE eg_pg_program (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    programId character varying(64),
    name character varying(64),
    description character varying(256),
    startDate bigint,
    endDate bigint,
    objective JSONB,
    criteria JSONB,
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_pg_program UNIQUE (id)
);

CREATE TABLE eg_ag_agency (
    id character varying(64),
    tenantId character varying(64),
    agencyId character varying(64),
    agencyType character varying(64),
    programCode character varying(64),
    orgNumber character varying(64),
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_ag_agency UNIQUE (id)
);

CREATE TABLE eg_pj_project (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    agencyId character varying(64),
    projectCode character varying(64),
    projectId character varying(64),
    name character varying(64),
    description character varying(256),
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_pj_project UNIQUE (id)
);

CREATE TABLE eg_es_estimate (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    projectCode character varying(64),
    estimateId character varying(64),
    description character varying(256),
    netAmount bigint,
    grossAmount bigint,
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_es_estimate UNIQUE (id)
);

CREATE TABLE eg_sa_sanction (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    projectCode character varying(64),
    sanctionId character varying(64),
    netAmount bigint,
    grossAmount bigint,
    availableAmount bigint,
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_sa_sanction UNIQUE (id)
);

CREATE TABLE eg_al_allocation (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    projectCode character varying(64),
    sanctionId character varying(64),
    allocationId character varying(64),
    netAmount bigint,
    grossAmount bigint,
    allocationType character varying(64),
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_al_allocation UNIQUE (id)
);

CREATE TABLE eg_di_disburse (
    id character varying(64),
    tenantId character varying(64),
    programCode character varying(64),
    projectCode character varying(64),
    disburseId character varying(64),
    targetId character varying(64),
    transactionId character varying(64),
    sanctionId character varying(64),
    amountCode character varying(64),
    netAmount bigint,
    grossAmount bigint,
    statusCode character varying(64),
    statusMessage character varying(64),
    additionalDetails JSONB,
    createdBy character varying(64),
    createdTime bigint,
    lastModifiedBy character varying(64),
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_di_disburse UNIQUE (id)
);