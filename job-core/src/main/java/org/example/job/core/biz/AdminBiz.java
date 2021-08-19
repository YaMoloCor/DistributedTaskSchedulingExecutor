package org.example.job.core.biz;

import org.example.job.core.biz.model.HandleCallbackParam;
import org.example.job.core.biz.model.RegistryParam;
import org.example.job.core.biz.model.ReturnT;

import java.util.List;

public interface AdminBiz {
    /**
     * 回调
     * @param callbackParamList
     * @return
     */
    ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);

    /**
     * 执行器注册
     * @param registryParam
     * @return
     */
    ReturnT<String> registry(RegistryParam registryParam);

    /**
     * 移除执行器
     * @param registryParam
     * @return
     */
    ReturnT<String> registryRemove(RegistryParam registryParam);
}
