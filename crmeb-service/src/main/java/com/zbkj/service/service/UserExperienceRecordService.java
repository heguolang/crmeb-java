package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.model.user.UserExperienceRecord;

import java.util.List;

/**
 * 用户经验记录服务接口
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
public interface UserExperienceRecordService extends IService<UserExperienceRecord> {

    /**
     * 获取用户经验列表（移动端）
     * @param userId 用户id
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserExperienceRecord> getH5List(Integer userId, PageParamRequest pageParamRequest);

    /**
     * 通过订单编号获取记录
     * @param orderNo 订单编号
     * @param uid uid
     * @return UserExperienceRecord
     */
    UserExperienceRecord getByOrderNoAndUid(String orderNo, Integer uid);

    /**
     * 通过订单编号和关联类型获取记录
     */
    UserExperienceRecord getByOrderNoAndUidAndLinkType(String orderNo, Integer uid, String linkType);

    /**
     * 统计用户交易完成计单记录数
     */
    Integer countCompleteOrderByUid(Integer uid);

    /**
     * 统计用户已付款累计消费金额（经验值）
     */
    Integer sumPaidConsumptionByUid(Integer uid);

    /**
     * 统计用户交易完成累计消费金额（经验值）
     */
    Integer sumCompleteConsumptionByUid(Integer uid);
}
