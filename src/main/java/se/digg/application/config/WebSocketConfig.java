/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{

	private static final String WS_ENDPOINT = "/ws";

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry)
	{
		registry
			.addEndpoint(WS_ENDPOINT)
			.setAllowedOriginPatterns(
				"http://localhost:8080",
				"http://localhost:8081",
				"http://localhost:4173",
				"http://localhost:5174",
				"http://localhost:5173"
			)
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry)
	{
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableSimpleBroker("/topic", "/queue");
	}
}