package org.egov.user.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//	@Autowired
//	@Qualifier("customAuthProvider")
//	private AuthenticationProvider customAuthProvider;
//
//	@Autowired
//	@Qualifier("preAuthProvider")
//	private AuthenticationProvider preAuthProvider;
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//		auth.authenticationProvider(customAuthProvider).authenticationProvider(preAuthProvider);
//	}

    /**
     * Configures HTTP security settings for the application.
     * Currently disables CSRF protection. This should be reviewed for production use
     * as CSRF protection is important for web applications.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
    }
}