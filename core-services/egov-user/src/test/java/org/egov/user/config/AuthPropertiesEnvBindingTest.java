package org.egov.user.config;

import org.junit.Test;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthPropertiesEnvBindingTest {

    private AuthProperties bind(Map<String, Object> env) throws Exception {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(new SystemEnvironmentPropertySource("systemEnvironment", env));
        sources.addLast(new ResourcePropertySource(new ClassPathResource("application.properties")));
        AuthProperties target = new AuthProperties();
        PropertiesConfigurationFactory<AuthProperties> factory = new PropertiesConfigurationFactory<>(target);
        factory.setPropertySources(sources);
        factory.setTargetName("auth");
        factory.bindPropertiesToTarget();
        return target;
    }

    @Test
    public void details_id_token_check_defaults_to_false() throws Exception {
        assertFalse(bind(new HashMap<>()).getOidc().isDetailsIdTokenCheckEnabled());
    }

    @Test
    public void details_id_token_check_binds_from_env_var() throws Exception {
        Map<String, Object> env = new HashMap<>();
        env.put("AUTH_OIDC_DETAILS_ID_TOKEN_CHECK_ENABLED", "true");
        assertTrue(bind(env).getOidc().isDetailsIdTokenCheckEnabled());
    }
}
