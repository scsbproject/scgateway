//package com.cyvation.scgateway.task;
//
//import com.cyvation.scgateway.SCGatewayApplication;
//import com.cyvation.scgateway.pojo.GatewayFilterDefinition;
//import com.cyvation.scgateway.pojo.GatewayPredicateDefinition;
//import com.cyvation.scgateway.pojo.GatewayRouteDefinition;
//import com.cyvation.scgateway.service.DynamicRouteServiceImpl;
//import com.cyvation.scgateway.util.RouteUtil;
//import com.cyvation.scgateway.util.SpringContextUtil;
//import org.quartz.Job;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
//import org.springframework.cloud.gateway.filter.FilterDefinition;
//import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * 自动发现ereukaz注册中心的服务，如果发现新的服务那么自动加入到gateway中，不用重启网关，
// * 新版本getaway已经实现了，不需要
// */
//public class AutoDiscoverEreukaServiceTask implements Job {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(AutoDiscoverEreukaServiceTask.class);
//
//    /**
//     * 定时任务执行，每隔一段时间扫描注册中心的服务
//     * @param jobExecutionContext
//     * @throws JobExecutionException
//     */
//    @Override
//    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        LOGGER.info("-------------------------------------------------------------------------");
//        LOGGER.info("开始寻找服务");
//        DynamicRouteServiceImpl dynamicRouteService = SCGatewayApplication.configurableApplicationContext.getBean(DynamicRouteServiceImpl.class);
//        List<GatewayRouteDefinition> gatewayRouteDefinitions = new ArrayList<>();
//        DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator = (DiscoveryClientRouteDefinitionLocator) SpringContextUtil.getBean(DiscoveryClientRouteDefinitionLocator.class);
//        try {
//            discoveryClientRouteDefinitionLocator.getRouteDefinitions().subscribe(routeDefinition -> {
//                GatewayRouteDefinition gatewayRouteDefinition = new GatewayRouteDefinition();
//                gatewayRouteDefinition.setUri(routeDefinition.getUri().toString());
//                gatewayRouteDefinition.setId(routeDefinition.getId());
//                gatewayRouteDefinition.setOrder(routeDefinition.getOrder());
//                if (routeDefinition.getPredicates() != null && routeDefinition.getPredicates().size() > 0){
//                    gatewayRouteDefinition.setPredicates(new ArrayList<>());
//                    for (PredicateDefinition predicateDefinition:routeDefinition.getPredicates()) {
//                        GatewayPredicateDefinition gatewayPredicateDefinition = new GatewayPredicateDefinition();
//                        gatewayPredicateDefinition.setArgs(predicateDefinition.getArgs());
//                        gatewayPredicateDefinition.setName(predicateDefinition.getName());
//                        gatewayRouteDefinition.getPredicates().add(gatewayPredicateDefinition);
//                    }
//                }
//
//                if (routeDefinition.getFilters() != null && routeDefinition.getFilters().size() > 0){
//                    gatewayRouteDefinition.setFilters(new ArrayList<>());
//                    for (FilterDefinition filterDefinition:routeDefinition.getFilters()) {
//                        GatewayFilterDefinition gatewayFilterDefinition = new GatewayFilterDefinition();
//                        gatewayFilterDefinition.setArgs(filterDefinition.getArgs());
//                        gatewayFilterDefinition.setName(filterDefinition.getName());
//                        gatewayRouteDefinition.getFilters().add(gatewayFilterDefinition);
//                    }
//                }
//                gatewayRouteDefinitions.add(gatewayRouteDefinition);
//
//            });
//            List<RouteDefinition> currentRoutes = dynamicRouteService.getCurrentRoutes();
//            //然后查找现有的路由与注册中心的差异，判断哪些是需要删除，哪些是需要新增
//            //删除路由
//            List<RouteDefinition> needDeleteRoutes = getNeedDeleteRoutes(gatewayRouteDefinitions,currentRoutes);
//
//            for (RouteDefinition routeDefinition:needDeleteRoutes) {
//                dynamicRouteService.delete(routeDefinition.getId());
//            }
//
//            LOGGER.info("删除：[" + needDeleteRoutes.size() + "]条路由。");
//            //添加路由
//            List<GatewayRouteDefinition> needAddRoutes = getNeedAddRoutes(gatewayRouteDefinitions,currentRoutes);
//            for (GatewayRouteDefinition gatewayRouteDefinition:needAddRoutes) {
//                dynamicRouteService.add(RouteUtil.assembleRouteDefinition(gatewayRouteDefinition));
//            }
//            //
//            LOGGER.info("添加：[" + needAddRoutes.size() + "]条路由。");
//        }catch (Exception e){
//            LOGGER.error("查找服务失败",e);
//        }
//        LOGGER.info("寻找服务完成");
//        LOGGER.info("-------------------------------------------------------------------------");
//
//    }
//
//    /**
//     * 获取需要删除的路由
//     * @param ereukaRoutes 注册中心的路由
//     * @param currentRoutes 当前网关中的路由
//     * @return
//     */
//    private List<RouteDefinition> getNeedDeleteRoutes(List<GatewayRouteDefinition> ereukaRoutes,List<RouteDefinition> currentRoutes){
//        List<RouteDefinition> needDeleteRoutes = new ArrayList<>();
//        for (RouteDefinition routeDefinition:currentRoutes) {
//            //查找在注册中心是否存在
//            List<GatewayRouteDefinition> existRoute = ereukaRoutes.stream().filter(item -> item.getId().equals(routeDefinition.getId())).collect(Collectors.toList());
//            if (existRoute == null || existRoute.size() == 0){
//                needDeleteRoutes.add(routeDefinition);
//            }
//        }
//        return needDeleteRoutes;
//    }
//
//    /**
//     * 获取需要添加的路由
//     * @param ereukaRoutes
//     * @param currentRoutes
//     * @return
//     */
//    private List<GatewayRouteDefinition> getNeedAddRoutes(List<GatewayRouteDefinition> ereukaRoutes,List<RouteDefinition> currentRoutes){
//        List<GatewayRouteDefinition> needAddRoutes = new ArrayList<>();
//        for (GatewayRouteDefinition gatewayRouteDefinition:ereukaRoutes) {
//            //判断在当前官网中的路由是否能找到，如果不能找到，那么添加
//            List<RouteDefinition> existRoute = currentRoutes.stream().filter(item -> item.getId().equals(gatewayRouteDefinition.getId())).collect(Collectors.toList());
//            if (existRoute == null || existRoute.size() == 0){
//                needAddRoutes.add(gatewayRouteDefinition);
//            }
//        }
//        return needAddRoutes;
//    }
//}
