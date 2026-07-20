package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.request.AdminIntegralSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserWarrantRecordResponse;

import java.util.List;

/**
 * 用户权证记录 Service
 */
public interface UserWarrantRecordService extends IService<UserWarrantRecord> {

    PageInfo<UserWarrantRecordResponse> findAdminList(AdminIntegralSearchRequest request, PageParamRequest pageParamRequest);

    List<UserWarrantRecord> findUserRecordList(Integer uid, PageParamRequest pageParamRequest);
}
