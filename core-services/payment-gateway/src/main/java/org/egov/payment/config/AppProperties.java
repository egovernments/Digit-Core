package org.egov.payment.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Getter
@ToString
@Configuration
@PropertySource("classpath:application.properties")
public class AppProperties {
    // App Configuration
    private final Integer earlyReconcileJobRunInterval;
    private final String egovPgReconciliationSystemUserUuid;

    // Message Broker Configuration
    private final Boolean messageBrokerEnabled;
    private final String messageBrokerType;
    private final String createTxnTopic;
    private final String updateTxnTopic;

    // IdGen Configuration
    private final String idGenHost;
    private final String idGenGenerateIdPath;
    private final String idGenTxnIdTemplateCode;

    // Registry Configuration
    private final String registryHost;
    private final String registryDataSearchPath;
    private final String registryBankAccountSchema;

    // Individual Configuration
    private final String individualHost;
    private final String individualCreatePath;
    private final String individualSearchPath;
    private final Boolean isUserCreationEnable;

    // Billing Configuration
    private final String billingHost;
    private final String billingPaymentCreatePath;
    private final String billingPaymentValidatePath;

    // Notification Configuration
    private final Boolean notificationEnabled;
    private final String notificationType;
    private final String notificationEmailTopic;
    private final String notificationSmsTopic;
    private final String notificationHost;
    private final String notificationEmailPath;
    private final String notificationSmsPath;

    // App Pay Configuration
    private final String applicationPayHost;
    private final String applicationPayLink;

    // URL Shortner Configuration
    private final String urlShortnerHost;
    private final String urlShortnerPath;

    // Single Instance Configuration
    private final Integer stateLevelTenantLength;
    private final Boolean isEnvironmentCentralInstance;

    @Autowired
    public AppProperties(Environment environment) {
        this.earlyReconcileJobRunInterval = Integer.valueOf(environment.getRequiredProperty("pg.earlyReconcileJobRunInterval.mins"));
        this.egovPgReconciliationSystemUserUuid = environment.getRequiredProperty("egov.pg.reconciliation.system.user.uuid");

        this.messageBrokerEnabled = Boolean.parseBoolean(environment.getRequiredProperty("message.broker.enabled"));
        this.messageBrokerType = environment.getRequiredProperty("message.broker.type");
        this.createTxnTopic = environment.getRequiredProperty("messaging.broker.topic.create.txn");
        this.updateTxnTopic = environment.getRequiredProperty("messaging.broker.topic.update.txn");

        this.idGenHost = environment.getRequiredProperty("idgen.host");
        this.idGenGenerateIdPath = environment.getRequiredProperty("idgen.generate.id.path");
        this.idGenTxnIdTemplateCode = environment.getRequiredProperty("idgen.txn.id.template.code");

        this.registryHost = environment.getRequiredProperty("registry.host");
        this.registryDataSearchPath = environment.getRequiredProperty("registry.data.search.path");
        this.registryBankAccountSchema = environment.getRequiredProperty("registry.bank.account.schema");

        this.individualHost = environment.getRequiredProperty("individual.host");
        this.individualCreatePath = environment.getRequiredProperty("individual.create.path");
        this.individualSearchPath = environment.getRequiredProperty("individual.search.path");
        this.isUserCreationEnable = Boolean.valueOf(environment.getRequiredProperty("pg.is.user.create.enabled"));

        this.billingHost = environment.getRequiredProperty("billing.host");
        this.billingPaymentCreatePath = environment.getRequiredProperty("billing.payments.create.path");
        this.billingPaymentValidatePath = environment.getRequiredProperty("billing.payments.validate.path");

        this.notificationEnabled = Boolean.parseBoolean(environment.getRequiredProperty("notification.enabled"));
        this.notificationType = environment.getRequiredProperty("notification.type");
        this.notificationEmailTopic = environment.getRequiredProperty("notification.email.topic");
        this.notificationSmsTopic = environment.getRequiredProperty("notification.sms.topic");
        this.notificationHost = environment.getRequiredProperty("notification.host");
        this.notificationEmailPath = environment.getRequiredProperty("notification.email.path");
        this.notificationSmsPath = environment.getRequiredProperty("notification.sms.path");

        this.applicationPayHost = environment.getRequiredProperty("application.pay.host");
        this.applicationPayLink = environment.getRequiredProperty("application.pay.link");

        this.urlShortnerHost = environment.getRequiredProperty("url.shortner.host");
        this.urlShortnerPath = environment.getRequiredProperty("url.shortner.path");

        this.stateLevelTenantLength = Integer.parseInt(environment.getRequiredProperty("state.level.tenantid.length"));
        this.isEnvironmentCentralInstance = Boolean.parseBoolean(environment.getRequiredProperty("is.environment.central.instance"));
    }
}
