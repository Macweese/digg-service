/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig
{

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		http
			// CORS first, THEN disable CSRF for simplicity in this setup
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/actuator/health",
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/ws/**"
				).permitAll()
				.anyRequest().permitAll()
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource()
	{
		CorsConfiguration config = new CorsConfiguration();

		// Just allow all local origins
		config.addAllowedOriginPattern("https?://localhost:*");

		// Typical allowed methods/headers
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Location"));
		// If we want to take advantage of cookies/auth
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// Apply to all routes
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}