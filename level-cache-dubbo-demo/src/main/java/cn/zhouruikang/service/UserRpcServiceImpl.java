package cn.zhouruikang.service;

import cn.zhouruikang.dto.UserDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@CacheConfig
public class UserRpcServiceImpl implements  UserRpcService {
    private static final Logger logger = LoggerFactory.getLogger(UserRpcServiceImpl.class);

    @Value("${dubbo.protocol.port}")
    Integer port;

    @Cacheable(cacheNames = "users", key = "#id", sync = true)
    public UserDTO get(Integer id) {
        logger.debug("尚无缓存");

        if (id == 3) return null; //做缓存value为null的测试

        return new UserDTO().setId(id).setName(port + "zrk");
    }


    @CachePut(cacheNames = "users", key = "#userDTO.id")
    public UserDTO update(UserDTO userDTO) {
        return userDTO;
    }

    @CacheEvict(cacheNames = "users", key = "#id")
    public boolean evict(Integer id) {
        return true;
    }


}
