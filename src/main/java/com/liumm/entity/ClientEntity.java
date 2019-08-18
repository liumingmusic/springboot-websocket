package com.liumm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.websocket.Session;
import java.io.Serializable;

/**
 * @Author NieZhiLiang
 * @Email nzlsgg@163.com
 * @Date 2019/3/1 上午9:08
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientEntity implements Serializable {

    private String userName;

    private Session session;

}
