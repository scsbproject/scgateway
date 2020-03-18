package com.cyvation.scgateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态路由服务
 */
@Service
public class DynamicRouteServiceImpl implements ApplicationEventPublisherAware {

    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    public DynamicRouteServiceImpl(RouteDefinitionWriter routeDefinitionWriter,RouteDefinitionLocator routeDefinitionLocator){
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    //增加路由
    public String add(RouteDefinition definition){
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    //更新路由
    public String update(RouteDefinition definition){
        try {
            this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
        } catch (Exception e) {
            return "update fail,not find route routeId: "+definition.getId();
        }
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return "success";
        } catch (Exception e) {
            return "update route fail";
        }
    }


    /**
     * 通过id获取路由信息
     * @param id
     * @return
     */
    public RouteDefinition getRoute(final String id){
        List<RouteDefinition> routeList = new ArrayList<>();
        routeDefinitionLocator.getRouteDefinitions().subscribe(routeDefinition -> {
            if (routeDefinition.getId().equals(id)){
                routeList.add(routeDefinition);
            }
        });
        if (routeList.size() > 0){
            return routeList.get(0);
        }else{
            return null;
        }
    }

    /**
     * 获取当前网关中的所有路由
     * @return
     */
    public List<RouteDefinition> getCurrentRoutes(){
        List<RouteDefinition> routeList = new ArrayList<>();
        routeDefinitionLocator.getRouteDefinitions().subscribe(routeDefinition -> {
            routeList.add(routeDefinition);
        });
        return routeList;
    }

    //删除路由
    public Mono<ResponseEntity<Object>> delete(String id) {
        return this.routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }

}
