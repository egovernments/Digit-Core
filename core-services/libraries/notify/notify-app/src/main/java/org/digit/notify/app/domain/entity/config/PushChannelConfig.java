package org.digit.notify.app.domain.entity.config;

import java.util.Map;

public class PushChannelConfig extends ChannelConfig {
    private Map<String, String> title;

    public Map<String, String> getTitle() { return title; }
    public void setTitle(Map<String, String> title) { this.title = title; }
}
