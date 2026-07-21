package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.finance.UserMoneyTransfer;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserMoneyTransferRequest;
import com.zbkj.common.request.UserMoneyTransferSearchRequest;
import com.zbkj.common.response.UserMoneyTransferCheckResponse;
import com.zbkj.common.response.UserMoneyTransferResponse;

/**
 * 用户余额互转
 */
public interface UserMoneyTransferService extends IService<UserMoneyTransfer> {

    /**
     * 校验收款用户
     */
    UserMoneyTransferCheckResponse checkReceiver(Integer toUid);

    /**
     * 发起余额转账
     */
    Boolean transfer(UserMoneyTransferRequest request);

    /**
     * 后台转账记录分页
     */
    PageInfo<UserMoneyTransferResponse> getAdminList(UserMoneyTransferSearchRequest request, PageParamRequest pageParamRequest);
}
