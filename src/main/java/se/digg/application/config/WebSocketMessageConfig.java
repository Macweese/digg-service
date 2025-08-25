/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageConfig implements WebSocketMessageBrokerConfigurer
{

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry)
	{
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns(
				"http://localhost:8080",
				"http://localhost:8081",
				"http://localhost:5174",
				"http://localhost:5173",
				"http://localhost:4173")
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry)
	{
		// Client sends to /app/... -> @MessageMapping handlers
		registry.setApplicationDestinationPrefixes("/app");
		// Clients subscribe to /topic/... for broadcasts
		registry.enableSimpleBroker("/topic");
		// Optional: per-user queues (enable if you use /user/queue/...)
		// registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration)
	{
		ThreadPoolTaskExecutor inbound = new ThreadPoolTaskExecutor();
		inbound.setThreadNamePrefix("ws-in-");
		inbound.setCorePoolSize(2);
		inbound.setMaxPoolSize(8);
		inbound.setQueueCapacity(1000);
		inbound.setKeepAliveSeconds(30);
		inbound.setWaitForTasksToCompleteOnShutdown(true);
		inbound.setAwaitTerminationSeconds(5);
		inbound.initialize();

		registration.taskExecutor(inbound);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration)
	{
		ThreadPoolTaskExecutor outbound = new ThreadPoolTaskExecutor();
		outbound.setThreadNamePrefix("ws-out-");
		outbound.setCorePoolSize(2);
		outbound.setMaxPoolSize(8);
		outbound.setQueueCapacity(1000);
		outbound.setKeepAliveSeconds(30);
		outbound.setWaitForTasksToCompleteOnShutdown(true);
		outbound.setAwaitTerminationSeconds(5);
		outbound.initialize();

		registration.taskExecutor(outbound);
	}
}