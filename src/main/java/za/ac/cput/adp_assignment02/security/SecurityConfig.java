package za.ac.cput.adp_assignment02.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/*
 * SecurityConfig.java
 * Spring Security set-up, following the Blackboard "Security" notes (Chapter 9.6 / 9.7),
 * written as modern Java configuration instead of applicationContext-security.xml:
 *
 *  Notes (XML)                                  Here (Java config)
 *  -------------------------------------------  ----------------------------------------------------
 *  <form-login login-page="/login.jsp"/>         formLogin() posting to /auth/login (email + password)
 *  <intercept-url pattern=... access="ROLE_.."/> authorizeHttpRequests(...) rules below
 *  <authentication-manager> + jdbc-user-service  EmployeeUserDetailsService (users = employees)
 *  <password-encoder hash="md5"/>                BCryptPasswordEncoder (MD5 is no longer safe)
 *  <global-method-security pre-post-annotations> @EnableMethodSecurity + @PreAuthorize on controllers
 *  _spring_security_remember_me checkbox         rememberMe() ("Keep me signed in" on the login page)
 *  session-fixation-protection="migrateSession"  sessionManagement().sessionFixation().migrateSession()
 *  accessDenied.jsp                              JSON 401 / 403 messages (JsonSecurityHandlers)
 *
 * Roles:  ADMIN    - everything (add/edit people, stock, suppliers, approve leave, attendance register)
 *         EMPLOYEE - sell (New Sale), view stock/customers/orders, add a customer from a sale,
 *                    request leave and clock in/out for THEMSELVES only
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JsonSecurityHandlers handlers,
                                                   EmployeeUserDetailsService userDetailsService,
                                                   @Value("${app.security.remember-me-key:change-this-remember-me-key}") String rememberMeKey) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // the React app itself, the health check and the login form are public
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/error", "/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // admin-only areas
                        .requestMatchers("/employees/**").hasRole("ADMIN")
                        .requestMatchers("/leave/all", "/leave/*/approve", "/leave/*/decline").hasRole("ADMIN")
                        .requestMatchers("/attendance/all").hasRole("ADMIN")
                        .requestMatchers("/notifications/run-checks").hasRole("ADMIN")

                        // employees can make sales and add a new customer while making a sale
                        .requestMatchers(HttpMethod.POST, "/orders/checkout", "/customers/create").hasAnyRole("ADMIN", "EMPLOYEE")

                        // everyone logged in can LOOK at stock, customers, suppliers and orders ...
                        .requestMatchers(HttpMethod.GET, "/products/**", "/suppliers/**", "/customers/**", "/orders/**", "/order-items/**")
                            .hasAnyRole("ADMIN", "EMPLOYEE")
                        // ... but only admins can change them
                        .requestMatchers("/products/**", "/suppliers/**", "/customers/**", "/orders/**", "/order-items/**").hasRole("ADMIN")

                        // leave, attendance, notifications, own profile: any logged-in user
                        // (controllers make sure employees only touch their own records)
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/")                       // the React app shows the login screen
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(handlers)
                        .failureHandler(handlers)
                        .permitAll())
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                        .userDetailsService(userDetailsService))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200)))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession()))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(handlers)
                        .accessDeniedHandler(handlers))
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
