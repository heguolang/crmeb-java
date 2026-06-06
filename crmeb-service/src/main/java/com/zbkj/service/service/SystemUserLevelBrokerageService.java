package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.system.SystemUserLevelBrokerage;
import com.zbkj.common.request.SystemUserLevelBrokerageRequest;

import java.util.List;
import java.util.Map;

/**
 * 会员等级返佣配置 Service
 */
public interface SystemUserLevelBrokerageService extends IService<SystemUserLevelBrokerage> {

    /**
     * 获取全部等级返佣配置
     */
    List<SystemUserLevelBrokerage> getList();

    /**
     * 根据等级ID获取返佣配置
     */
    SystemUserLevelBrokerage getByLevelId(Integer levelId);

    /**
     * 批量获取返佣配置
     */
    Map<Integer, SystemUserLevelBrokerage> mapByLevelIds(List<Integer> levelIds);

    /**
     * 保存或更新等级返佣配置
     */
    Boolean saveOrUpdateByLevelId(Integer levelId, SystemUserLevelBrokerageRequest request);

    /**
     * 删除等级返佣配置
     */
    Boolean deleteByLevelId(Integer levelId);
}
