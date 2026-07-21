package com.zbkj.service.service;

import com.zbkj.common.request.ExtractConfigRequest;
import com.zbkj.common.response.ExtractConfigResponse;

/**
 * 提现设置
 */
public interface ExtractConfigService {

    ExtractConfigResponse getConfig();

    Boolean saveConfig(ExtractConfigRequest request);
}
