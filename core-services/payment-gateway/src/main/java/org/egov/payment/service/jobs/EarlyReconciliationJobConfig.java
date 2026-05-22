package org.egov.payment.service.jobs;

import org.egov.payment.config.AppProperties;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * Scheduled to run at a frequent configurable interval.
 */
@Configuration
public class EarlyReconciliationJobConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    @Primary
    JobDetailFactoryBean earlyReconciliationJobs() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(EarlyReconciliationJob.class);
        jobDetailFactory.setGroup("status-update");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    @Autowired
    CronTriggerFactoryBean earlyReconciliationTrigger(JobDetail earlyReconciliationJob) {
        int runEvery = appProperties.getEarlyReconcileJobRunInterval();
        int runEveryHours = runEvery / 60;
        int runEveryMinutes = runEvery % 60;

        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(earlyReconciliationJob);
        cronTriggerFactoryBean.setCronExpression("0 " + runEveryHours + "/" + runEveryMinutes + " * * * ?");
        cronTriggerFactoryBean.setGroup("status-update");
        return cronTriggerFactoryBean;
    }
}
