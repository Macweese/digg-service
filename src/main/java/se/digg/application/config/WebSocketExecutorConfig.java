/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketExecutorConfig implements WebSocketMessageBrokerConfigurer
{
	public ThreadPoolTaskExecutor clientInboundChannelExecutor()
	{
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setThreadNamePrefix("ws-in-");
		ex.setCorePoolSize(2);
		ex.setMaxPoolSize(8);
		ex.setQueueCapacity(1000);
		ex.setWaitForTasksToCompleteOnShutdown(true);
		ex.setAwaitTerminationSeconds(5);
		return ex;
	}

	public ThreadPoolTaskExecutor clientOutboundChannelExecutor()
	{
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setThreadNamePrefix("ws-out-");
		ex.setCorePoolSize(2);
		ex.setMaxPoolSize(8);
		ex.setQueueCapacity(1000);
		ex.setWaitForTasksToCompleteOnShutdown(true);
		ex.setAwaitTerminationSeconds(5);
		return ex;
	}
}