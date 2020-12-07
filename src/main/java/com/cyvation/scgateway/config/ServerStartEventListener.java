/**
 * projectName: scgateway-autodiscover
 * fileName: ServerStartedListener.java
 * packageName: com.cyvation.scgateway.config
 * date: 2020-12-07 10:23
 * copyright(c) 2017-2020 同方赛威讯信息技术公司
 */
package com.cyvation.scgateway.config;

import com.moon.util.network.NetWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @version: V1.0
 * @author: 代浩然
 * @className: ServerStartedListener
 * @packageName: com.cyvation.scgateway.config
 * @description: 服务启动事件监听器
 * @data: 2020-12-07 10:23
 **/
@Component
public class ServerStartEventListener implements ApplicationListener<ApplicationReadyEvent> {


    @Value("${server.port}")
    private String port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Environment environment = applicationReadyEvent.getApplicationContext().getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        int localPort = Integer.parseInt(port);
        String profile = StringUtils.arrayToCommaDelimitedString(environment.getActiveProfiles());
        if (ClassUtils.isPresent("springfox.documentation.spring.web.plugins.Docket", null)) {
            String runInfo = String.format("-本地 API文档地址:http://localhost:%s/doc.html\n-局域网 API文档地址:http://%s:%s/doc.html\n服务[%s]启动完成，当前使用的端口:[%s]，环境变量:[%s]", localPort, NetWork.getHostIp(), localPort, appName, localPort, profile);
            System.out.println(runInfo);
        }
    }
}