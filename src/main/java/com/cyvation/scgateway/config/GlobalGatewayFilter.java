/**
 * projectName: scgateway-autodiscover
 * fileName: GlobalGatewayFilter.java
 * packageName: com.cyvation.scgateway.config
 * date: 2020-03-18 11:49
 * copyright(c) 2017-2020 同方赛威讯信息技术公司
 */
package com.cyvation.scgateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: GlobalGatewayFilter
 * @packageName: com.cyvation.scgateway.config
 * @description: 全局网关过滤器配置，当请求服务接口的时候，都会进入此拦截，用作认证拦截
 * @data: 2020-03-18 11:49
 **/
@Configuration
public class GlobalGatewayFilter implements GlobalFilter,Ordered {

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //进行认证拦截
        return chain.filter(exchange);
    }
}