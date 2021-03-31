# Level-cache

简介

level-cache-spring-boot-starter 是一个基于springboot的集成redis与ehcache的 **二级缓存** 快速启动器。使用原有轮子 spring cache、redis cache、ehcache 构建，仅需一键引入少量配置即可使用。

level-cache-dubbo-demo 是一个简单的 dubbo 使用 demo 供参考，当然 spring cloud 亦可使用。

## 前言

写这个项目的缘由来源于上一份工作中，公司各项目中使用缓存时仅使用 spring cache 的常规操作，即要么只使用 ehcache 作为本地缓存、要么只使用 redis 作为分布式缓存 ，详细使用方式可参考这篇文章：[Spring Boot 缓存 Cache 入门](https://www.iocoder.cn/Spring-Boot/Cache/)。

However,  **本地缓存** 由于与应用本身在同一个进程内部，无分布式缓存过多的网络开销，请求缓存快速。因此在单应用不需要集群支持或者集群情况下各节点无需互相通知的场景下使用较合适。但微服务场景下为保证可用性，同一个应用往往存在多个应用实例部署负载均衡，因此极大可能会导致多个应用实例对应缓存数据不一致性。**集中式缓存** 与本地应用隔离，多个应用可直接共享缓存，极少出现数据不一致情况。但存在过多的网络IO延迟和对象序列化造成的开销，且会给网络带宽造成极大压力。

**若项目仅使用一种缓存，**在缓存使用选择上，对于不变对象或较小规模的、可预见次数的访问本地缓存是一个理想解决方案，性能上它优于分布式缓存，**但当出现某一应用实例访问修改缓存时，需重启其他应用实例以清空自身原缓存，从而维护缓存数据一致性**（之前公司这么操作，真的非常really **不优雅**！！！）。对于可变对象、较大且不可预见规模、要求强一致性而的访问，则最好采用分布式缓存。

显然，**二级缓存** 是更好的选择～


## 优势

- 基于原有轮子spring cache、redis cache、ehcache 改造，易操作，学习成本少，bug 风险低。
- 支持缓存的自动刷新（当二级缓存命中并发现将要过期时，会主动刷新缓存）
- 允许存空值解决缓存穿透问题
- 项目代码结构清晰简洁，易于自由二次开发，例如自定义序列化方式等

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

## 思路来源及一点感想

除了仔细

说来也比较好笑，开发完成后



