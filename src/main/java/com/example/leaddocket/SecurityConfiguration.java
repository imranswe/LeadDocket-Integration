package com.example.leaddocket;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
public class SecurityConfiguration {
 @Bean SecurityFilterChain security(HttpSecurity h) throws Exception {
  // Webhook controllers enforce provider-specific authentication before inserting any event.
  return h.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
    .requestMatchers("/actuator/health","/webhooks/leaddocket").permitAll().anyRequest().hasRole("ADMIN"))
   .httpBasic(Customizer.withDefaults()).build();
 }
 @Bean UserDetailsService users(@Value("${app.admin.username}") String username,@Value("${app.admin.password}") String password) {
  if(password.length()<16)throw new IllegalStateException("APP_ADMIN_PASSWORD needs at least 16 characters");
  return new org.springframework.security.provisioning.InMemoryUserDetailsManager(
   User.withUsername(username).password("{bcrypt}"+new BCryptPasswordEncoder().encode(password)).roles("ADMIN").build());
 }
}
