package com.cyvation.scgateway.swagger;

import com.cyvation.scgateway.config.GatewayConfig;
import com.cyvation.scgateway.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: SwaggerProvider
 * @packageName: com.cyvation.scgateway.swagger
 * @description:
 * @data: 2020-03-18 11:12
 **/
@Component
@Primary
public class SwaggerProvider implements SwaggerResourcesProvider {
    public static final String API_URI = "/v2/api-docs";

    //private final Discover---------yClientRouteDefinitionLocator routeLocator;

    private final RouteDefinitionLocator routeLocator;

    public SwaggerProvider(RouteDefinitionLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {

        DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator = (DiscoveryClientRouteDefinitionLocator) SpringContextUtil.getBean(DiscoveryClientRouteDefinitionLocator.class);

        List<SwaggerResource> resources = new ArrayList<>();
        //从DiscoveryClientRouteDefinitionLocator 中取出routes，构造成swaggerResource
        routeLocator.getRouteDefinitions().subscribe(routeDefinition -> {
            String serverId = routeDefinition.getId();
            String location = "";
            if (routeDefinition.getPredicates().get(0).getArgs().get("pattern") != null) {
                //通过接口动态添加的路由，key为pattern
                location = routeDefinition.getPredicates().get(0).getArgs().get("pattern").replace("/**", API_URI);
            }else{
                location = routeDefinition.getPredicates().get(0).getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI);
            }
            resources.add(swaggerResource(serverId,location));
        });
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}