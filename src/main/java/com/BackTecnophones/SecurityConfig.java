package com.BackTecnophones;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	      .csrf(csrf -> csrf.disable())
	      .authorizeHttpRequests(auth -> auth
	          .requestMatchers("/verificar", "/logout").permitAll() // Publicos
	          .anyRequest().permitAll()
//	          .anyRequest().authenticated() -> Para produccion
	      );
	    return http.build();
	  }
	
	@Bean
	  public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration cfg = new CorsConfiguration();
	    cfg.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:5174", "https://tecnophones00.web.app")); //5174 Por Romero -> Despues Sacarlo y poner el de produccion
	    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
	    cfg.setAllowedHeaders(List.of("*"));
	    cfg.setAllowCredentials(true);
	    cfg.setMaxAge(3600L);
	
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", cfg);
	    return source;
	  }
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(12);
	}
}
