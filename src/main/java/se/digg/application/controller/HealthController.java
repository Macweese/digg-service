/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Health check API")
public class HealthController
{
	@Operation(summary = "Health check", description = "Check if the service is running")
	@GetMapping
	public Map<String, Object> health()
	{
		Map<String, Object> health = new HashMap<>();
		health.put("status", "UP");
		health.put("timestamp", LocalDateTime.now());
		health.put("service", "User Service");
		health.put("version", "1.0.0");
		return health;
	}
}