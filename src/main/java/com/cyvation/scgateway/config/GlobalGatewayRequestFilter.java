/**
 * projectName: scgateway-autodiscover
 * fileName: GlobalGatewayFilter.java
 * packageName: com.cyvation.scgateway.config
 * date: 2020-03-18 11:49
 * copyright(c) 2017-2020 同方赛威讯信息技术公司
 */
package com.cyvation.scgateway.config;

import com.cyvation.scgateway.core.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: GlobalGatewayFilter
 * @packageName: com.cyvation.scgateway.config
 * @description: 全局网关请求过滤器配置，当请求服务接口的时候，都会进入此拦截，用作认证拦截
 * @data: 2020-03-18 11:49
 **/
@Configuration
public class GlobalGatewayRequestFilter implements GlobalFilter,Ordered {

    @Autowired
    GatewayConfig gatewayConfig;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //进行认证拦截

        //过滤URL，解决单机版的时候请求swagger带上服务名称
        ServerHttpRequestDecorator serverHttpRequestDecorator = requestDecorator(exchange);
        return chain.filter(exchange.mutate().request(serverHttpRequestDecorator).build());
    }

    //重写Request,过滤swagger-ui的url
    private ServerHttpRequestDecorator requestDecorator(ServerWebExchange exchange) {
        ServerHttpRequestDecorator serverHttpRequestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public URI getURI() {
                URI  uri = super.getURI();
                URI base = null;
                try {
                    //
                    String url = uri.getPath();
                    if (url.indexOf("v2/api-docs") != -1 && Constant.SysEnv.ALONG.equalsIgnoreCase(gatewayConfig.getProfilesActive())){
                        url = url.substring(url.indexOf("/") + 1);
                        url = url.substring(url.indexOf("/") + 1);
                    }
                    base = new URI(uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/" + url);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return base;
            }
        };
        return serverHttpRequestDecorator;
    }

}