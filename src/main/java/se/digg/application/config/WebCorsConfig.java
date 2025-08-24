/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC CORS mappings for REST endpoints.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer
{
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("http://localhost:5173", "http://localhost:8081")
			.allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
			.allowedHeaders("*")
			.exposedHeaders("Location")
			.allowCredentials(true);
	}
}