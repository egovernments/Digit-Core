package org.digit.notify.app.domain.entity;

import jakarta.persistence.*;
import org.digit.notify.app.domain.entity.config.ChannelsConfig;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "notification_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "template_code"}))
public class NotificationConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private ChannelsConfig channels;

    @Embedded
    private AuditDetail auditDetail = new AuditDetail();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public ChannelsConfig getChannels() { return channels; }
    public void setChannels(ChannelsConfig channels) { this.channels = channels; }
    public AuditDetail getAuditDetail() { return auditDetail; }
    public void setAuditDetail(AuditDetail auditDetail) { this.auditDetail = auditDetail; }
}
