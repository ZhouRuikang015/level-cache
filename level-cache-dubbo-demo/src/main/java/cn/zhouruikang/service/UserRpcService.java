package cn.zhouruikang.service;


import cn.zhouruikang.dto.UserDTO;

public interface UserRpcService {

    UserDTO get(Integer id);

    UserDTO update(UserDTO userDTO);

    boolean evict(Integer id);
}
