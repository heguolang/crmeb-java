package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.finance.UserWarrantExchange;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserWarrantExchangeSearchRequest;
import com.zbkj.common.response.UserWarrantExchangeResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.UserWarrantExchangeDao;
import com.zbkj.service.service.UserService;
import com.zbkj.service.service.UserWarrantExchangeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权证兑换申请实现
 */
@Service
public class UserWarrantExchangeServiceImpl extends ServiceImpl<UserWarrantExchangeDao, UserWarrantExchange> implements UserWarrantExchangeService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_DONE = 1;

    @Resource
    private UserWarrantExchangeDao dao;

    @Autowired
    private UserService userService;

    @Override
    public Boolean createApply(Integer uid, String payType, BigDecimal payAmount, BigDecimal warrantAmount, String address) {
        Date now = CrmebDateUtil.nowDateTime();
        UserWarrantExchange apply = new UserWarrantExchange();
        apply.setUid(uid);
        apply.setPayType(payType);
        apply.setPayAmount(payAmount);
        apply.setWarrantAmount(warrantAmount);
        apply.setAddress(StrUtil.blankToDefault(address, ""));
        apply.setStatus(STATUS_PENDING);
        apply.setMark("");
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        return save(apply);
    }

    @Override
    public PageInfo<UserWarrantExchangeResponse> getAdminList(UserWarrantExchangeSearchRequest request, PageParamRequest pageParamRequest) {
        Page<UserWarrantExchange> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserWarrantExchange> wrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(request.getUid())) {
            wrapper.eq(UserWarrantExchange::getUid, request.getUid());
        }
        if (ObjectUtil.isNotNull(request.getStatus())) {
            wrapper.eq(UserWarrantExchange::getStatus, request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getPayType())) {
            wrapper.eq(UserWarrantExchange::getPayType, request.getPayType().trim());
        }
        if (StringUtils.isNotBlank(request.getKeywords())) {
            String kw = request.getKeywords().trim();
            List<User> users = userService.list(new LambdaQueryWrapper<User>()
                    .like(User::getNickname, kw)
                    .select(User::getUid));
            Set<Integer> nicknameUids = users.stream().map(User::getUid).collect(Collectors.toSet());
            wrapper.and(w -> {
                w.like(UserWarrantExchange::getAddress, kw);
                if (CollUtil.isNotEmpty(nicknameUids)) {
                    w.or().in(UserWarrantExchange::getUid, nicknameUids);
                }
                if (kw.matches("\\d+")) {
                    w.or().eq(UserWarrantExchange::getUid, Integer.valueOf(kw));
                }
            });
        }
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            wrapper.between(UserWarrantExchange::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        wrapper.orderByDesc(UserWarrantExchange::getCreateTime, UserWarrantExchange::getId);
        List<UserWarrantExchange> list = dao.selectList(wrapper);

        Set<Integer> uidSet = list.stream().map(UserWarrantExchange::getUid).collect(Collectors.toSet());
        HashMap<Integer, User> userMap = CollUtil.isEmpty(uidSet)
                ? new HashMap<>()
                : userService.getMapListInUid(new ArrayList<>(uidSet));

        List<UserWarrantExchangeResponse> responseList = list.stream().map(item -> {
            UserWarrantExchangeResponse response = new UserWarrantExchangeResponse();
            BeanUtils.copyProperties(item, response);
            User user = userMap.get(item.getUid());
            response.setNickname(ObjectUtil.isNotNull(user) ? user.getNickname() : "");
            return response;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    @Override
    public Boolean updateStatus(Integer id, Integer status) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            throw new CrmebException("记录不存在");
        }
        if (!Integer.valueOf(STATUS_PENDING).equals(status) && !Integer.valueOf(STATUS_DONE).equals(status)) {
            throw new CrmebException("状态仅支持待处理或已处理");
        }
        UserWarrantExchange exist = getById(id);
        if (ObjectUtil.isNull(exist)) {
            throw new CrmebException("记录不存在");
        }
        UserWarrantExchange update = new UserWarrantExchange();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(CrmebDateUtil.nowDateTime());
        return updateById(update);
    }
}
