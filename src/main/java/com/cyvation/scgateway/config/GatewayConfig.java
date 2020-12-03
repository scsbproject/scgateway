/**
 * projectName: scgateway-autodiscover
 * fileName: GatewayConfig.java
 * packageName: com.cyvation.scgateway.config
 * date: 2020-03-18 14:22
 * copyright(c) 2017-2020 同方赛威讯信息技术公司
 */
package com.cyvation.scgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: GatewayConfig
 * @packageName: com.cyvation.scgateway.config
 * @description: 网关配置
 * @data: 2020-03-18 14:22
 **/
@Configuration
public class GatewayConfig {

    @Value("${spring.profiles.active}")
    private String profilesActive;

    /**
     * 如果使用了注册中心（如：Eureka），进行控制则需要增加如下配置
     */
    @Bean
    public RouteDefinitionLocator discoveryClientRouteDefinitionLocator(DiscoveryClient discoveryClient,
                                                                        DiscoveryLocatorProperties properties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }

    public String getProfilesActive() {
        return profilesActive;
    }

    public void setProfilesActive(String profilesActive) {
        this.profilesActive = profilesActive;
    }
}