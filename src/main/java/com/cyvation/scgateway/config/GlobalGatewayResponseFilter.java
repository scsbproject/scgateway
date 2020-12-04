package com.cyvation.scgateway.config;

import com.cyvation.scgateway.core.Constant;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultClientResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * 全局网关响应数据过滤器配置
 */
@Configuration
@Slf4j
public class GlobalGatewayResponseFilter implements GlobalFilter,Ordered {

    @Autowired
    GatewayConfig gatewayConfig;

    @Override
    public int getOrder() {
        //WRITE_RESPONSE_FILTER 之前执行
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponseDecorator serverHttpResponseDecorator = responseDecorator(exchange);
        return chain.filter(exchange.mutate().response(serverHttpResponseDecorator).build());
    }

    /**
     * 响应数据过滤器
     * @param exchange
     * @return
     */
    private ServerHttpResponseDecorator responseDecorator(ServerWebExchange exchange){

        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR).toString();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, originalResponseContentType);
                ResponseAdapter responseAdapter = new ResponseAdapter(body, httpHeaders);
                String requestUrl = exchange.getRequest().getURI().toString();//请求的url
                DefaultClientResponse clientResponse = new DefaultClientResponse(responseAdapter, ExchangeStrategies.withDefaults());
                Mono<String> rawBody = clientResponse.bodyToMono(String.class).map(s -> s);
                BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(rawBody, String.class);
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, exchange.getResponse().getHeaders());
                return bodyInserter.insert(outputMessage, new BodyInserterContext())
                        .then(Mono.defer(() -> {
                            Flux<DataBuffer> messageBody = outputMessage.getBody();
                            Flux<DataBuffer> flux = messageBody.map(buffer -> {
                                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
                                DataBufferUtils.release(buffer);
                                // 将响应信息转化为字符串
                                String responseStr = charBuffer.toString();
                                if (StringUtils.isNotBlank(responseStr)
                                    && Constant.SysEnv.NACOS.equalsIgnoreCase(gatewayConfig.getProfilesActive())
                                    && requestUrl.indexOf("v2/api-docs") != -1) {
                                    try {
                                        responseStr = filterSwaggerResponseBody(responseStr,requestUrl);
                                    } catch (Exception e) {
                                        log.error("响应结果处理异常{}", responseStr,e);
                                    }
                                }
                                return getDelegate().bufferFactory().wrap(responseStr.getBytes(StandardCharsets.UTF_8));

                            });
                            HttpHeaders headers = getDelegate().getHeaders();
                            // 修改响应包的大小，不修改会因为包大小不同被浏览器丢掉
                            flux = flux.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
                            return getDelegate().writeWith(flux);
                        }));
            }

            /**
             * 过滤swagger的响应内容，解决使用注册中心的时候，swagger生成的接口path没有带服务名称
             * @param responseStr
             * @return
             */
            private String filterSwaggerResponseBody(String responseStr,String requestUrl)throws Exception{

                //解析服务名称
                String serverName = requestUrl.substring(requestUrl.indexOf("/") + 2);
                serverName = serverName.substring(serverName.indexOf("/") + 1);
                serverName = serverName.substring(0,serverName.indexOf("/"));
                //解析JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String,Object> jsonMap = (Map<String,Object>)objectMapper.readValue(responseStr,Map.class);
                Map<String,Object> paths = (Map<String,Object>)jsonMap.get("paths");
                Map<String,Object> newMap = new HashMap<>();
                for (String key : paths.keySet()) {
                    newMap.put("/" + serverName + key, paths.get(key));
                }
                jsonMap.put("paths",newMap);
                return objectMapper.writeValueAsString(jsonMap);
            }
        };
        return responseDecorator;
    }


    private class ResponseAdapter implements ClientHttpResponse {

        private final Flux<DataBuffer> flux;
        private final HttpHeaders headers;

        @SuppressWarnings("unchecked")
        private ResponseAdapter(Publisher<? extends DataBuffer> body, HttpHeaders headers) {
            this.headers = headers;
            if (body instanceof Flux) {
                flux = (Flux) body;
            } else {
                flux = ((Mono) body).flux();
            }
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return flux;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public HttpStatus getStatusCode() {
            return null;
        }

        @Override
        public int getRawStatusCode() {
            return 0;
        }

        @Override
        public MultiValueMap<String, ResponseCookie> getCookies() {
            return null;
        }
    }
}
