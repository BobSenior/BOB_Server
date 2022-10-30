package com.bob_senior.bob_server.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ChatConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*").withSockJS();
        //websocket연결 endpoint = */ws/chat
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/queue","/topic");
        registry.setApplicationDestinationPrefixes("/app");
        //msg보낼때는 /app으로 -> 변경가능
        //구독신청 : topic, queue

    }

}
