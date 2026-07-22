package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserTeamLevel;
import com.zbkj.common.response.UserTeamLevelRecordResponse;
import com.zbkj.common.response.UserTeamLevelUserResponse;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * 用户团队等级 Service
 */
public interface UserTeamLevelService extends IService<UserTeamLevel> {

    /**
     * 支付成功时统计自购/团队金额并触发团队等级升级
     */
    Boolean processTeamLevelOnOrderPaid(StoreOrder storeOrder);

    /**
     * 订单完成时统计自购/团队金额并触发团队等级升级
     */
    Boolean processTeamLevelOnOrderComplete(StoreOrder storeOrder);

    /**
     * 订单退款时回滚自购/团队金额；等级采用只升不降，退款不会自动清空/降低团队等级
     */
    Boolean rollbackTeamLevelOnRefund(StoreOrder storeOrder);

    /**
     * 根据用户当前统计数据匹配应达到的团队等级
     */
    SystemTeamLevel resolveMatchedTeamLevel(User user);

    /**
     * 批量根据 uid 触发等级同步（内部使用）
     */
    Boolean syncTeamLevels(List<Integer> uids);

    /**
     * 管理员手动修改用户团队等级
     */
    Boolean adminUpdateTeamLevel(Integer uid, Integer teamLevelId);

    /**
     * 团队关联用户分页
     */
    PageInfo<UserTeamLevelUserResponse> getTeamUserPage(String keywords, Integer teamLevelId, PageParamRequest pageParamRequest);

    /**
     * 团队等级变更记录分页
     */
    PageInfo<UserTeamLevelRecordResponse> getTeamRecordPage(String keywords, Integer teamLevelId, Integer status, PageParamRequest pageParamRequest);
}

