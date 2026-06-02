package org.digit.notify.app.domain.entity.config;

import java.util.Map;

public class ChannelConfig {
    private boolean enabled;
    private String priority;
    private Map<String, String> body;
    private Map<String, String> payloadBindings;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Map<String, String> getBody() { return body; }
    public void setBody(Map<String, String> body) { this.body = body; }
    public Map<String, String> getPayloadBindings() { return payloadBindings; }
    public void setPayloadBindings(Map<String, String> payloadBindings) { this.payloadBindings = payloadBindings; }
}
