package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.request.AdminIntegralSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserVoucherRecordResponse;

import java.util.List;

/**
 * 用户消费券记录 Service
 */
public interface UserVoucherRecordService extends IService<UserVoucherRecord> {

    PageInfo<UserVoucherRecordResponse> findAdminList(AdminIntegralSearchRequest request, PageParamRequest pageParamRequest);

    List<UserVoucherRecord> findUserRecordList(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 是否已存在指定类型的当日流水（用于每日释放幂等）
     */
    Boolean existsTodayByUidAndLinkType(Integer uid, String linkType);
}
