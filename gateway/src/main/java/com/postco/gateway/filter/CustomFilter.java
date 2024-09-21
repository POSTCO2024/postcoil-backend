package com.postco.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        /*
        exchange : 서비스 요청/응답값을 담고있는 변수로, 요청/응답값을 출력하거나 변환할 때 사용한다.
        요청값은 (exchange, chain) -> 구문 이후에 얻을 수 있으며,
        서비스로부터 리턴받은 응답값은 Mono.fromRunnable(()-> 구문 이후부터 얻을 수 있다.
         */
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom PRE filter : request id -> {}", request.getId());

            // Custom Post Filter
            //비동기 방식으로 지원할 때 단일값 Mono 타입을 전달.
            return chain.filter(exchange).then(Mono.fromRunnable(()-> {
                log.info("Custom POST filter : response code -> {}", response.getStatusCode());
            }));
        };
    }

    /*
    config : application.yml에 선언한 각 filter의 args(인자값) 사용을 위한 클래스
     */
    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
