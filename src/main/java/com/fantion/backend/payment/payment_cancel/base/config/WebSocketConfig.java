package com.fantion.backend.payment.payment_cancel.base.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	public void configureMessageBroker(MessageBrokerRegistry config) {
		/*
		Subscribe
		[/chat/message/ + {roomSeq}]
		[/chat/notify/ + {roomSeq}]
		 */
		config.enableSimpleBroker("/sub");

		/*
		Publish
		[/pub/chat/message/{roomSeq}]
		[/pub/chat/notify/{roomSeq}]
		 */
		config.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/fantion").setAllowedOriginPatterns("*");
		registry.addEndpoint("/fantion").setAllowedOriginPatterns("*").withSockJS();
	}
}
