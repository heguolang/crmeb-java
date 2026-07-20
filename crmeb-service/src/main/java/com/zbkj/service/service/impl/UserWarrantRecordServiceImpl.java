package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.WarrantRecordConstants;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AdminIntegralSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserWarrantRecordResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.UserWarrantRecordDao;
import com.zbkj.service.service.UserService;
import com.zbkj.service.service.UserWarrantRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户权证记录 Service 实现
 */
@Service
public class UserWarrantRecordServiceImpl extends ServiceImpl<UserWarrantRecordDao, UserWarrantRecord> implements UserWarrantRecordService {

    @Resource
    private UserWarrantRecordDao dao;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<UserWarrantRecordResponse> findAdminList(AdminIntegralSearchRequest request, PageParamRequest pageParamRequest) {
        Page<UserWarrantRecordResponse> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserWarrantRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserWarrantRecord::getStatus, WarrantRecordConstants.STATUS_COMPLETE);
        if (ObjectUtil.isNotNull(request.getUid())) {
            lqw.eq(UserWarrantRecord::getUid, request.getUid());
        }
        if (StrUtil.isNotBlank(request.getKeywords())) {
            List<Integer> idList = userService.findIdListLikeName(request.getKeywords());
            if (CollUtil.isNotEmpty(idList)) {
                lqw.in(UserWarrantRecord::getUid, idList);
            } else {
                return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
            }
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            lqw.between(UserWarrantRecord::getUpdateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        lqw.orderByDesc(UserWarrantRecord::getUpdateTime);
        List<UserWarrantRecord> list = dao.selectList(lqw);
        if (CollUtil.isEmpty(list)) {
            return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        }
        List<UserWarrantRecordResponse> responseList = list.stream().map(record -> {
            UserWarrantRecordResponse response = new UserWarrantRecordResponse();
            BeanUtils.copyProperties(record, response);
            User user = userService.getById(record.getUid());
            if (ObjectUtil.isNotNull(user)) {
                response.setNickname(user.getNickname());
            }
            return response;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    @Override
    public List<UserWarrantRecord> findUserRecordList(Integer uid, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserWarrantRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserWarrantRecord::getUid, uid);
        lqw.eq(UserWarrantRecord::getStatus, WarrantRecordConstants.STATUS_COMPLETE);
        lqw.orderByDesc(UserWarrantRecord::getUpdateTime);
        return dao.selectList(lqw);
    }
}
