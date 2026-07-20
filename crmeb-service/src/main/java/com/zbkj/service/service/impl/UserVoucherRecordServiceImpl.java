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
import com.zbkj.common.constants.VoucherRecordConstants;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AdminIntegralSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserVoucherRecordResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.UserVoucherRecordDao;
import com.zbkj.service.service.UserService;
import com.zbkj.service.service.UserVoucherRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户消费券记录 Service 实现
 */
@Service
public class UserVoucherRecordServiceImpl extends ServiceImpl<UserVoucherRecordDao, UserVoucherRecord> implements UserVoucherRecordService {

    @Resource
    private UserVoucherRecordDao dao;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<UserVoucherRecordResponse> findAdminList(AdminIntegralSearchRequest request, PageParamRequest pageParamRequest) {
        Page<UserVoucherRecordResponse> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserVoucherRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserVoucherRecord::getStatus, VoucherRecordConstants.STATUS_COMPLETE);
        if (ObjectUtil.isNotNull(request.getUid())) {
            lqw.eq(UserVoucherRecord::getUid, request.getUid());
        }
        if (StrUtil.isNotBlank(request.getKeywords())) {
            List<Integer> idList = userService.findIdListLikeName(request.getKeywords());
            if (CollUtil.isNotEmpty(idList)) {
                lqw.in(UserVoucherRecord::getUid, idList);
            } else {
                return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
            }
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            lqw.between(UserVoucherRecord::getUpdateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        lqw.orderByDesc(UserVoucherRecord::getUpdateTime);
        List<UserVoucherRecord> list = dao.selectList(lqw);
        if (CollUtil.isEmpty(list)) {
            return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        }
        List<UserVoucherRecordResponse> responseList = list.stream().map(record -> {
            UserVoucherRecordResponse response = new UserVoucherRecordResponse();
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
    public List<UserVoucherRecord> findUserRecordList(Integer uid, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserVoucherRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserVoucherRecord::getUid, uid);
        lqw.eq(UserVoucherRecord::getStatus, VoucherRecordConstants.STATUS_COMPLETE);
        lqw.orderByDesc(UserVoucherRecord::getUpdateTime);
        return dao.selectList(lqw);
    }

    @Override
    public Boolean existsTodayByUidAndLinkType(Integer uid, String linkType) {
        String today = cn.hutool.core.date.DateUtil.today();
        String start = today + " 00:00:00";
        String end = today + " 23:59:59";
        LambdaQueryWrapper<UserVoucherRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserVoucherRecord::getUid, uid);
        lqw.eq(UserVoucherRecord::getLinkType, linkType);
        lqw.between(UserVoucherRecord::getCreateTime, start, end);
        lqw.last(" limit 1");
        return dao.selectCount(lqw) > 0;
    }
}
