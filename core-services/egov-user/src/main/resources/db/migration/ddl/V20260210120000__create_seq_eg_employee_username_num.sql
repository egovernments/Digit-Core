-- Sequence for SSO/HRMS employee username suffix (per-tenant).
-- Used by JwtExchangeAuthenticationProvider to generate unique EMP-{tenantId}-{provider}-{idpRole}-{number} usernames.
CREATE SEQUENCE IF NOT EXISTS seq_eg_employee_username_num
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
