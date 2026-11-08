package cl.duoc.gamerhub.security;

import cl.duoc.gamerhub.repository.AccountRepository;
import cl.duoc.gamerhub.client.BackendClient;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean UserDetailsService userDetailsService(AccountRepository accounts){
        return email -> accounts.findByEmail(email.strip().toLowerCase(java.util.Locale.ROOT))
            .map(a -> User.withUsername(a.getEmail()).password(a.getPassword()).roles("USER").build())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtTokens tokens, UserDetailsService users) throws Exception {
        http.securityMatcher("/api/**")
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .requestCache(cache -> cache.disable())
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) ->
                response.setStatus(401)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/eventos").permitAll()
                .anyRequest().hasRole("USER"))
            .addFilterBefore(new JwtAuthenticationFilter(tokens, users), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, BackendClient backend) throws Exception {
        // Todo lo que no se declara público requiere una sesión autenticada.
        http.authenticationManager(backend::authenticate)
            .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; " +
                "style-src 'self' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "object-src 'none'; " +
                "base-uri 'self'; " +
                "frame-ancestors 'none'; " +
                "form-action 'self'"
            )))
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/","/home","/recetas","/registro","/eventos","/login","/css/**","/error").permitAll()
            .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/eventos").permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
        // CSRF se mantiene activo, incluido el cierre de sesión mediante POST.
        return http.build();
    }
}
