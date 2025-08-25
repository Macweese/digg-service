/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
	@Autowired
	private Environment env;

	/**
	 * Configures the security filter chain for the application.
	 * This method defines access rules, CSRF protection, CORS settings,
	 * form login, and exception handling for the application's HTTP requests.
	 *
	 * @param http the HttpSecurity object to configure
	 * @return a SecurityFilterChain instance
	 * @throws Exception if an error occurs during configuration
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		http
			.cors(Customizer.withDefaults())
			// Keep CSRF enabled, but ignore for H2 console
			.csrf(csrf ->
				{
					csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
					// Only disable CSRF for H2 console in non-production environments
					if (Arrays.asList(env).contains("dev") || Arrays.asList(env.getActiveProfiles()).contains("test"))
					{
						csrf.ignoringRequestMatchers(PathRequest.toH2Console());
					}
				}
			).authorizeHttpRequests(auth -> auth
				// Allow pre-flight CORS requests
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// Allow public access to certain endpoints (Swagger, Actuator, WebSockets, API, etc)
				.requestMatchers("/ws/**", "/digg/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				// Require authentication for H2 console
				.requestMatchers("/h2-console/**").access((authentication, ctx) ->
				{
					boolean isDevOrTest = Arrays.asList(env.getActiveProfiles()).contains("dev")
						|| Arrays.asList(env.getActiveProfiles()).contains("test");
					return new AuthorizationDecision(isDevOrTest);
				})
				// All other requests are permitted without authentication
				.anyRequest().permitAll()
			)
			// Configure form login for authenticated access
			.formLogin(Customizer.withDefaults())
			// Configure basic authentication for a simple login prompt (optional, formLogin is usually preferred)
			// .httpBasic(Customizer.withDefaults())
			// Exception handling for access denied (403 Forbidden)
			.exceptionHandling(ex -> ex
				// send 403 instead of forwarding to a page (which produced 404)
				.accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
			)
			// Write CSRF cookie on every request
			.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

		// H2 console needs frames - only in dev/test
		if (Arrays.asList(env.getActiveProfiles()).contains("dev") || Arrays.asList(env.getActiveProfiles()).contains("test"))
		{
			http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
		}
		return http.build();
	}

	/**
	 * Configures the Cross-Origin Resource Sharing (CORS) settings for the application.
	 * This bean defines which origins, methods, and headers are allowed for cross-origin requests.
	 *
	 * @return a CorsConfigurationSource instance
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource()
	{
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of("http://localhost:8081", "http://localhost:5173", "http://localhost:5174", "http://localhost:4173"));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setExposedHeaders(List.of("Location"));
		cfg.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}

	/**
	 * Configure in-memory user for demon purposes.
	 * In a production environment, would typically use a database
	 * or an external identity provider for user management.
	 *
	 * @param passwordEncoder the password encoder to use for encoding passwords
	 * @return a UserDetailsService
	 */
	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder)
	{
		// User for H2 console access
		// Replace "admin" and "password" with secure credentials in production
		UserDetails user = User.builder()
			.username("admin")
			.password(passwordEncoder.encode("password"))
			.roles("ADMIN")
			.build();
		return new InMemoryUserDetailsManager(user);
	}

	/**
	 * Provides a password encoder for encoding user passwords.
	 * BCrypt is recommended for strong password hashing.
	 *
	 * @return a PasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
}