package com.example.aipet.network.request;

import java.io.Serializable;

/**
 * 通用 API 请求基类
 * 
 * 统一所有 API 请求的接口和行为，便于工厂模式创建和泛型处理
 */
public abstract class BaseApiRequest implements Serializable {
    
    /**
     * 获取模型名称
     * @return 模型ID
     */
    public abstract String getModel();
    
    /**
     * 获取请求类型（用于日志和调试）
     * @return 请求类型标识
     */
    public abstract String getRequestType();
    
    /**
     * 获取用户消息内容
     * @return 用户消息内容
     */
    public abstract String getUserMessage();
    
    /**
     * 验证请求是否有效
     * @return true 如果请求有效
     */
    public boolean isValid() {
        return getModel() != null && !getModel().isEmpty() 
               && getUserMessage() != null && !getUserMessage().isEmpty();
    }
}
