package com.cyvation.scgateway.api;

import com.cyvation.scgateway.pojo.GatewayRouteDefinition;
import com.cyvation.scgateway.service.DynamicRouteServiceImpl;
import com.cyvation.scgateway.util.RouteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 说明：路由控制器，向外部应用提供路由管理的接口，可以对路由进行增删改查
 */
@RestController
@RequestMapping("/route")
public class RouteController {

    private DynamicRouteServiceImpl dynamicRouteService;

    @Autowired
    public RouteController(DynamicRouteServiceImpl dynamicRouteService){
        this.dynamicRouteService = dynamicRouteService;
    }

    //增加路由
    @PostMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gwdefinition){
        try {
            RouteDefinition definition = RouteUtil.assembleRouteDefinition(gwdefinition);
            return this.dynamicRouteService.add(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "succss";
    }

    //删除路由
    @DeleteMapping("/routes/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
        return this.dynamicRouteService.delete(id);
    }

    //更新路由
    @PostMapping("/update")
    public String update(@RequestBody GatewayRouteDefinition gwdefinition){
        RouteDefinition definition = RouteUtil.assembleRouteDefinition(gwdefinition);
        return this.dynamicRouteService.update(definition);
    }

    //获取路由信息
    @PostMapping("/getRouteInfo")
    public RouteDefinition getRouteInfo(String id){
        return this.dynamicRouteService.getRoute(id);
    }
}
