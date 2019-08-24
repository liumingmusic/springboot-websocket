package com.liumm.utils;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * 描述: webscoket工具类
 *
 * @Author liumm
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 * @Date 2019-08-24 18:21
 */
public class WebSocketUtil {

    /**
     * 根据session回话获取ip地址
     *
     * @param session session
     * @return InetSocketAddress
     */
    public static InetSocketAddress getRemoteAddress(Session session) {
        if (session == null) {
            return null;
        }
        Async async = session.getAsyncRemote();
        //在Tomcat 8.0.x版本有效
        //InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async,"base#sos#socketWrapper#socket#sc#remoteAddress");
        //在Tomcat 8.5以上版本有效
        return (InetSocketAddress) getFieldInstance(async, "base#socketWrapper#socket#sc#remoteAddress");
    }

    /**
     * 获取对应字段
     *
     * @param obj       obj
     * @param fieldPath fieldPath
     * @return Object
     */
    private static Object getFieldInstance(Object obj, String fieldPath) {
        String fields[] = fieldPath.split("#");
        for (String field : fields) {
            obj = getField(obj, obj.getClass(), field);
            if (obj == null) {
                return null;
            }
        }

        return obj;
    }

    /**
     * 获取字段值
     *
     * @param obj       obj
     * @param clazz     clazz
     * @param fieldName fieldName
     * @return Object
     */
    private static Object getField(Object obj, Class<?> clazz, String fieldName) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field;
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ignored) {

            }
        }

        return null;
    }

}
