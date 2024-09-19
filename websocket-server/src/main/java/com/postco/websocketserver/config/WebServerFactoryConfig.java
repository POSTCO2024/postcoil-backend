package com.postco.websocketserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;


@Configuration
public class WebServerFactoryConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Value("${server.min-port:8080}")
    private Integer minPort;
    @Value("${server.max-port:9080}")
    private Integer maxPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        int port = findAvailablePort(minPort, maxPort);
        factory.setPort(port);
        System.setProperty("server.port", String.valueOf(port));
    }

    private int findAvailablePort(int minPort, int maxPort) {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort(); // 사용 가능한 포트를 반환
        } catch (IOException e) {
            throw new IllegalStateException("사용 가능한 포트를 찾을 수 없습니다.", e);
        }
    }
}
