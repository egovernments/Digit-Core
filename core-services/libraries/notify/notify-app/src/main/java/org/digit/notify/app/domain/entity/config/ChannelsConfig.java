package org.digit.notify.app.domain.entity.config;

public class ChannelsConfig {
    private ChannelConfig sms;
    private EmailChannelConfig email;
    private ChannelConfig whatsapp;
    private PushChannelConfig push;

    public ChannelConfig getSms() { return sms; }
    public void setSms(ChannelConfig sms) { this.sms = sms; }
    public EmailChannelConfig getEmail() { return email; }
    public void setEmail(EmailChannelConfig email) { this.email = email; }
    public ChannelConfig getWhatsapp() { return whatsapp; }
    public void setWhatsapp(ChannelConfig whatsapp) { this.whatsapp = whatsapp; }
    public PushChannelConfig getPush() { return push; }
    public void setPush(PushChannelConfig push) { this.push = push; }
}
