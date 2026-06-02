package org.digit.notify.app.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_mapping")
public class ProviderMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "country")
    private String country;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "providers", columnDefinition = "jsonb", nullable = false)
    private List<String> providers;

    @Embedded
    private AuditDetail auditDetail = new AuditDetail();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public List<String> getProviders() { return providers; }
    public void setProviders(List<String> providers) { this.providers = providers; }
    public AuditDetail getAuditDetail() { return auditDetail; }
    public void setAuditDetail(AuditDetail auditDetail) { this.auditDetail = auditDetail; }
}
