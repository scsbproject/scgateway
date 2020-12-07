# 1. 说明
  网关项目使用springboot父版本为：2.0.6。
  1. 实现单机版本url转发，以及swagger调用。
  2. 实现nacos版本转发，以及swagger调用。
  3. 实现动态路由的配置，在不关闭网关服务的情况下，实现路由信息的增删改查
  
 # 2. 使用方式
 ## 2.1 单机版本
  将配置文件application.yml中的spring.profiles.active配置成standalone，然后去掉如下依赖：  
  ```
  <dependencyManagement>
          <dependencies>
              <!--alibaba 基于nacos服务注册发现-->
              <dependency>
                  <groupId>org.springframework.cloud</groupId>
                  <artifactId>spring-cloud-dependencies</artifactId>
                  <version>Finchley.SR1</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
              <dependency>
                  <groupId>org.springframework.cloud</groupId>
                  <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                  <version>0.2.2.RELEASE</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
          </dependencies>
  </dependencyManagement>
```
```
  <!--基于alibaba的nacos的服务注册与发现-->
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
              <exclusions>
                  <exclusion>
                      <groupId>com.alibaba.nacos</groupId>
                      <artifactId>nacos-client</artifactId>
                  </exclusion>
              </exclusions>
          </dependency>
  
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
              <exclusions>
                  <exclusion>
                      <groupId>com.alibaba.nacos</groupId>
                      <artifactId>nacos-client</artifactId>
                  </exclusion>
              </exclusions>
          </dependency>
  
          <dependency>
              <groupId>com.alibaba.nacos</groupId>
              <artifactId>nacos-client</artifactId>
              <version>1.1.3</version>
          </dependency>
```   
然后去掉启动类的注解EnableDiscoveryClient   
```
   /**
    * @desction 使用SpingClound Gateway实现网关
    * @version 1.0.0.1
    */
   @SpringBootApplication
   //@EnableDiscoveryClient
   public class SCGatewayApplication {
```  
然后在application-standalone.properties中配置路由规则，如下：
```
#单机版，无注册中心的配置，如果是单机版，被转发的目标服务的路径必须带上服务名称进行请求，也就是控制器必须带上服务名称
spring.cloud.gateway.routes[0].id=测试服务
spring.cloud.gateway.routes[0].predicates[0]=Path=/test-service/**
spring.cloud.gateway.routes[0].uri=http://192.168.7.153:8088
```
这里需要注意的是：单机版服务，目标服务的所有rest接口需要加服务名称：@RequestMapping("${spring.application.name}")  

启动完成成服务之后，访问地址：127.0.0.1:8080/doc.html

## 2.2 nacos版本使用
将application.properties的spring.profiles.active配置成nacos  
打开nacos相关依赖：  
```
<dependencyManagement>
        <dependencies>
            <!--alibaba 基于nacos服务注册发现-->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Finchley.SR1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>0.2.2.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
</dependencyManagement>
```
```
  <!--基于alibaba的nacos的服务注册与发现-->
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
              <exclusions>
                  <exclusion>
                      <groupId>com.alibaba.nacos</groupId>
                      <artifactId>nacos-client</artifactId>
                  </exclusion>
              </exclusions>
          </dependency>
  
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
              <exclusions>
                  <exclusion>
                      <groupId>com.alibaba.nacos</groupId>
                      <artifactId>nacos-client</artifactId>
                  </exclusion>
              </exclusions>
          </dependency>
  
          <dependency>
              <groupId>com.alibaba.nacos</groupId>
              <artifactId>nacos-client</artifactId>
              <version>1.1.3</version>
          </dependency>
```  
然后在启动类里面添加注解:EnableDiscoveryClient  
```
@SpringBootApplication
@EnableDiscoveryClient
public class SCGatewayApplication {
```  
然后在bootstrap.properties文件中配置nacos的地址信息  
```
spring.cloud.nacos.discovery.server-addr=192.168.2.137:7747

#spring.cloud.nacos.discovery.namespace=

spring.cloud.nacos.config.server-addr=192.168.2.137:7747
#spring.cloud.nacos.config.namespace=54ddbb5c-5861-49de-b77f-dd85c1035ef1
#spring.cloud.nacos.config.prefix=multityPlatformApp
#spring.cloud.nacos.config.file-extension=properties
#spring.cloud.nacos.config.group=DEFAULT_GROUP
```
然后启动服务，访问地址：127.0.0.1:8080/doc.html