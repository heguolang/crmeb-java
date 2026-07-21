package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.finance.UserWarrantExchange;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserWarrantExchangeSearchRequest;
import com.zbkj.common.response.UserWarrantExchangeResponse;

import java.math.BigDecimal;

/**
 * 权证兑换申请
 */
public interface UserWarrantExchangeService extends IService<UserWarrantExchange> {

    /**
     * 用户兑换成功后创建待处理申请
     */
    Boolean createApply(Integer uid, String payType, BigDecimal payAmount, BigDecimal warrantAmount, String address);

    /**
     * 后台分页列表
     */
    PageInfo<UserWarrantExchangeResponse> getAdminList(UserWarrantExchangeSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 更新处理状态：0=待处理 1=已处理
     */
    Boolean updateStatus(Integer id, Integer status);
}
