package org.digit.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This library must not contribute a Jackson bean.
 *
 * <p>Boot declares {@code @Bean @Primary @ConditionalOnMissingBean JsonMapper}. Now that this
 * library is also on Jackson 3, a mapper bean of its own would be the same type — and if this
 * auto-configuration were processed first, Boot's condition would suppress the application's own
 * primary mapper and every consumer's responses would silently be serialized by this library's
 * rules. Auto-configuration order is not something a library may rely on, so the library owns a
 * mapper internally (via DigitJson) and publishes none.
 *
 * <p>This is the one regression here that is invisible inside this repository and severe outside it.
 */
class MapperBeanIsolationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class, HeaderPropagationAutoConfiguration.class));

    @Test
    void leavesTheApplicationsPrimaryJsonMapperAlone() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(JsonMapper.class);
            assertThat(context.getBean(JsonMapper.class))
                    .isSameAs(context.getBean("jacksonJsonMapper", JsonMapper.class));
        });
    }

    @Test
    void clientsAreStillWiredWithoutAMapperBean() {
        // The clients must resolve even though nothing publishes an ObjectMapper for them.
        runner.run(context -> assertThat(context)
                .hasSingleBean(org.digit.services.billing.BillingClient.class)
                .hasSingleBean(org.digit.services.employee.EmployeeClient.class));
    }
}
