package cn.zhouruikang.dto;

import java.io.Serializable;

/**
 * 用户信息 DTO
 */
public class UserDTO implements Serializable {

    /**
     * 用户编号
     */
    private Integer id;
    /**
     * 昵称
     */
    private String name;


    public Integer getId() {
        return id;
    }

    public UserDTO setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserDTO setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
