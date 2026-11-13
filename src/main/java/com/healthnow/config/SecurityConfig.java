
package com.healthnow.config;

import com.healthnow.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarioDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/static.css",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                .requestMatchers("/doctor/**")
                .hasRole("DOCTOR")

                .requestMatchers("/paciente/**")
                .hasRole("PACIENTE")

                .requestMatchers("/especialidad/**")
                .hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")

                .requestMatchers(
                    "/cita/agendar",
                    "/cita/guardar",
                    "/cita/editar/**",
                    "/cita/actualizar/**",
                    "/cita/cancelar/**"
                ).hasRole("PACIENTE")

                .requestMatchers("/cita/listado")
                .hasAnyRole("PACIENTE", "DOCTOR")

                .requestMatchers("/cita/**")
                .hasAnyRole("PACIENTE", "DOCTOR")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")

                .successHandler((request, response, authentication) -> {
                    response.sendRedirect(
                        request.getContextPath() + "/"
                    );
                })

                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}

