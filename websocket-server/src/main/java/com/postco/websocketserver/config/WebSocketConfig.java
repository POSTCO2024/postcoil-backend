package com.postco.websocketserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${broker.relay.host}")
    private String brokerRelayHost;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 모든 구독 데이터(나가는 메시지/응답)는 클라이언트가 구독할 수 있는 URL로 데이터를 보냅니다.
        // 이 URL은 '/topic'으로 시작합니다. 예를 들어, @SendTo("/outgoing")가 있는 메서드는
        // 클라이언트가 "/topic/outgoing"을 구독하여 웹소켓 서버로부터 메시지를 받을 수 있게 합니다.
        registry.enableStompBrokerRelay("/queue", "/topic")
                .setRelayHost(brokerRelayHost);

        // 모든 목적지(MVC/REST 용어로 요청 매핑)는 '/app'로 시작하는 접두사를 가진 목적지로부터
        // 요청을 받아들입니다. 예를 들어, @MessageMapping("/hello")는 클라이언트가
        // "/app/hello"로 메시지를 보낼 때 해당 메시지를 받습니다.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 이 설정은 애플리케이션이 웹소켓 연결을 제공할 엔드포인트 또는 URL을 구성합니다.
        // 모든 클라이언트는 이 URL을 사용하여 이 웹소켓 서버에 연결을 열 것입니다.
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("*");
        registry.addEndpoint("/sockjs-websocket")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
