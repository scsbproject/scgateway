package com.cyvation.scgateway;

//import com.cyvation.scgateway.config.QuartzScheduler;
import com.cyvation.scgateway.config.ServerStartEventListener;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @desction 使用SpingClound Gateway实现网关
 * @version 1.0.0.1
 */
@SpringBootApplication
//@EnableDiscoveryClient
public class SCGatewayApplication {

    private static Logger LOGGER = LoggerFactory.getLogger(SCGatewayApplication.class);

    public static ConfigurableApplicationContext configurableApplicationContext;

    /**
     * 方法入口
     * @param args
     */
    public static void main(String[] args){
        configurableApplicationContext = SpringApplication.run(SCGatewayApplication.class,args);
        configurableApplicationContext.addApplicationListener(new ServerStartEventListener());
//        configurableApplicationContext.publishEvent(new ServerStartEventListener());  //发布消息
//        configurableApplicationContext.close();
//        startTask(configurableApplicationContext);
    }

//
//    //启动任务
//    private static void startTask(ConfigurableApplicationContext configurableApplicationContext){
//        QuartzScheduler quartzScheduler = configurableApplicationContext.getBeanFactory().getBean(QuartzScheduler.class);
//        try {
//            //启动任务
//            quartzScheduler.startAutoScanServiceJob();
//        }catch (Exception e){
//            LOGGER.error("启动任务失败",e);
//        }
//    }

}
