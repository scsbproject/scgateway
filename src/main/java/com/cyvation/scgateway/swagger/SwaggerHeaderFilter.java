/**
 * projectName: scgateway-autodiscover
 * fileName: SwaggerHeaderFilter.java
 * packageName: com.cyvation.scgateway.swagger
 * date: 2020-03-18 11:12
 * copyright(c) 2017-2020 同方赛威讯信息技术公司
 */
package com.cyvation.scgateway.swagger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: SwaggerHeaderFilter
 * @packageName: com.cyvation.scgateway.swagger
 * @description:  只是在Finchley.RELEASE版本需要实现，SR2版本无需实现。
 *                解决使用 Swagger 的 try it out 功能发现路径是路由切割后的路径比如：
 *               Swagger 文档中的路径为：
 *      主机名：端口：映射路径 少了一个 服务路由前缀，是因为展示 handler 经过了 StripPrefixGatewayFilterFactory 这个过滤器的处理，原有的 路由前缀被过滤掉了！解决思路是在Gateway里加一个过滤器来添加这个header。
 *                需要在添加每一个路由的时候，将该过滤器配置进去
 * @data: 2020-03-18 11:12
 **/
@Component
public class SwaggerHeaderFilter extends AbstractGatewayFilterFactory {
    private static final String HEADER_NAME = "X-Forwarded-Prefix";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (!StringUtils.endsWithIgnoreCase(path, SwaggerProvider.API_URI)) {
                return chain.filter(exchange);
            }
            String basePath = path.substring(0, path.lastIndexOf(SwaggerProvider.API_URI));
            ServerHttpRequest newRequest = request.mutate().header(HEADER_NAME, basePath).build();
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
            return chain.filter(newExchange);
        };
    }
}