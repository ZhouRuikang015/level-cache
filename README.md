# Level-cache

level-cache-spring-boot-starter 是一个基于springboot的集成redis与ehcache的二级缓存快速启动器。使用原有轮子 spring cache、redis cache、ehcache 构建，仅需一键引入少量配置即可使用。

level-cache-dubbo-demo 是一个简单的 dubbo 使用 demo 供参考，当然 spring cloud 亦可使用。

## 前言

​		写这个项目的缘由来源于上一份工作中，公司各项目在使用缓存时仅使用 spring cache 的常规操作，即要么只使用 ehcache 作为本地缓存、要么只使用 redis 作为分布式缓存 ，详细使用方式可参考这篇文章：[Spring Boot 缓存 Cache 入门](https://www.iocoder.cn/Spring-Boot/Cache/)。

​		However,  本地缓存指在应用中内置的缓存组件，其优势在于与应用本身在同一个进程内部，请求缓存快速，无过多的网络开销。在单应用不需要集群支持或者集群情况下各节点无需互相通知的场景下使用本地缓存较合适。然而，许多中到大型应用通常会做负载均衡，同一个应用往往存在多个应用实例部署。在这种情况下，极大可能会出现多个应用实例对应多个缓存的情况，从而导致数据不一致性，




## 项目介绍



## 使用

##### 1. 打包 maven 至私人仓库

因为暂时还没有上传中央仓库，需要自己打包一下。

```shell
cd level-cache-spring-boot-starter

mvn clean install
```

##### 2. 引入 level-cache-spring-boot-starter 依赖

```xml
<dependency>
  <groupId>cn.zhouruikang</groupId>
  <artifactId>level-cache-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

##### 3. 配置 ehcache.xml

像缓存只使用 ehcache 本地缓存的一般项目配置即可，因为**我原本的设想初衷是在不改动原只使用ehcache的工作项目的情况下升级为二级缓存**。以下是简单的 ehcache 配置示例。

`resources/ehcache.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd">

    <!-- users 缓存 -->
    <!-- name：缓存名 -->
    <!-- maxElementsInMemory：最大缓存 key 数量 -->
    <!-- timeToLiveSeconds：缓存过期时长，单位：秒 -->
    <!-- memoryStoreEvictionPolicy：缓存淘汰策略 -->
    <cache name="users"
           maxElementsInMemory="1000"
           timeToLiveSeconds="60"
           memoryStoreEvictionPolicy="LRU"/>

    <cache name="abcs"
           maxElementsInMemory="1000"
           timeToLiveSeconds="50"
           memoryStoreEvictionPolicy="LRU"/>

</ehcache>
```

##### 4. 配置 redis 缓存

这里在`resources/application.yaml`

```yaml
levelcache:
	# redis 频道名与 messageList 名标识相关，若注释不配置默认使用 spring.application.name。
  namespace: zrk 
  
  # redis 各缓存默认的 expire 时间为 50s。
  defaultExpire: 50 
  
  # 配置 redis 各缓存的 expire，单位为 s，若不配置则为 defaultExpire。
  # 例如这里的 users 缓存下各个 key 为 80s， abcs 则为 50s。
  cachesExpire:
    users: 80 
    
  # 主动刷新时间，当 redis 缓存key命中，若此时 expire 在 20s 以下则主动刷新至配置的缓存时间。
  preLoadTime: 20 

# 以下省略 
# redis 常规的连接配置
```

##### 5. 使用 @EnableCaching 开启缓存

```java
@EnableCaching
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```



## 一点感想

说来也比较好笑，开发完成后



