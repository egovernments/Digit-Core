package org.digit.notify.app.domain.entity.config;

import java.util.Map;

public class EmailChannelConfig extends ChannelConfig {
    private Map<String, String> subject;

    public Map<String, String> getSubject() { return subject; }
    public void setSubject(Map<String, String> subject) { this.subject = subject; }
}
