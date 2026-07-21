package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.finance.UserMoneyTransfer;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserMoneyTransferRequest;
import com.zbkj.common.request.UserMoneyTransferSearchRequest;
import com.zbkj.common.response.UserMoneyTransferCheckResponse;
import com.zbkj.common.response.UserMoneyTransferResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.UserMoneyTransferDao;
import com.zbkj.service.service.UserBillService;
import com.zbkj.service.service.UserMoneyTransferService;
import com.zbkj.service.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户余额互转实现
 */
@Service
public class UserMoneyTransferServiceImpl extends ServiceImpl<UserMoneyTransferDao, UserMoneyTransfer> implements UserMoneyTransferService {

    @Resource
    private UserMoneyTransferDao dao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public UserMoneyTransferCheckResponse checkReceiver(Integer toUid) {
        if (ObjectUtil.isNull(toUid) || toUid <= 0) {
            throw new CrmebException("请输入正确的收款用户ID");
        }
        Integer fromUid = userService.getUserIdException();
        if (fromUid.equals(toUid)) {
            throw new CrmebException("不能转账给自己");
        }
        User receiver = userService.getById(toUid);
        if (ObjectUtil.isNull(receiver)) {
            throw new CrmebException("收款用户不存在");
        }
        if (Boolean.FALSE.equals(receiver.getStatus())) {
            throw new CrmebException("收款用户状态异常");
        }
        UserMoneyTransferCheckResponse response = new UserMoneyTransferCheckResponse();
        response.setUid(receiver.getUid());
        response.setNickname(maskNickname(receiver.getNickname()));
        return response;
    }

    @Override
    public Boolean transfer(UserMoneyTransferRequest request) {
        if (ObjectUtil.isNull(request.getToUid()) || request.getToUid() <= 0) {
            throw new CrmebException("请输入正确的收款用户ID");
        }
        BigDecimal amount = request.getAmount();
        if (ObjectUtil.isNull(amount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("转账金额必须大于0");
        }
        amount = amount.setScale(2, RoundingMode.DOWN);
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new CrmebException("转账金额必须大于0");
        }

        User sender = userService.getInfoException();
        if (sender.getUid().equals(request.getToUid())) {
            throw new CrmebException("不能转账给自己");
        }
        User receiver = userService.getById(request.getToUid());
        if (ObjectUtil.isNull(receiver)) {
            throw new CrmebException("收款用户不存在");
        }
        if (Boolean.FALSE.equals(receiver.getStatus())) {
            throw new CrmebException("收款用户状态异常");
        }
        if (ObjectUtil.isNull(sender.getNowMoney()) || sender.getNowMoney().compareTo(amount) < 0) {
            throw new CrmebException("余额不足");
        }

        String mark = StrUtil.blankToDefault(StrUtil.trim(request.getMark()), "");
        if (mark.length() > 200) {
            throw new CrmebException("备注不能超过200字");
        }

        String transferNo = CrmebUtil.getOrderNo("MT");
        Date now = CrmebDateUtil.nowDateTime();
        BigDecimal fromBalance = sender.getNowMoney().subtract(amount);
        BigDecimal toBalance = nullToZero(receiver.getNowMoney()).add(amount);
        BigDecimal finalAmount = amount;

        UserBill outBill = buildBill(sender.getUid(), transferNo, 0, "余额转出",
                Constants.USER_BILL_TYPE_USER_TRANSFER_OUT, finalAmount, fromBalance,
                StrUtil.format("转账给用户ID{}，金额{}", receiver.getUid(), finalAmount));
        outBill.setCreateTime(now);
        outBill.setUpdateTime(now);

        UserBill inBill = buildBill(receiver.getUid(), transferNo, 1, "余额转入",
                Constants.USER_BILL_TYPE_USER_TRANSFER_IN, finalAmount, toBalance,
                StrUtil.format("收到用户ID{}转账，金额{}", sender.getUid(), finalAmount));
        inBill.setCreateTime(now);
        inBill.setUpdateTime(now);

        UserMoneyTransfer transfer = new UserMoneyTransfer();
        transfer.setTransferNo(transferNo);
        transfer.setFromUid(sender.getUid());
        transfer.setToUid(receiver.getUid());
        transfer.setAmount(finalAmount);
        transfer.setFromBalance(fromBalance);
        transfer.setToBalance(toBalance);
        transfer.setMark(mark);
        transfer.setStatus(1);
        transfer.setCreateTime(now);
        transfer.setUpdateTime(now);

        Boolean execute = transactionTemplate.execute(e -> {
            User freshSender = userService.getById(sender.getUid());
            User freshReceiver = userService.getById(receiver.getUid());
            if (ObjectUtil.isNull(freshSender) || ObjectUtil.isNull(freshReceiver)) {
                throw new CrmebException("用户数据异常");
            }
            if (nullToZero(freshSender.getNowMoney()).compareTo(finalAmount) < 0) {
                throw new CrmebException("余额不足");
            }
            if (!userService.operationNowMoney(freshSender.getUid(), finalAmount, freshSender.getNowMoney(), "sub")) {
                throw new CrmebException("扣减余额失败");
            }
            if (!userService.operationNowMoney(freshReceiver.getUid(), finalAmount, freshReceiver.getNowMoney(), "add")) {
                throw new CrmebException("增加余额失败");
            }

            BigDecimal realFromBalance = nullToZero(freshSender.getNowMoney()).subtract(finalAmount);
            BigDecimal realToBalance = nullToZero(freshReceiver.getNowMoney()).add(finalAmount);
            outBill.setBalance(realFromBalance);
            inBill.setBalance(realToBalance);
            transfer.setFromBalance(realFromBalance);
            transfer.setToBalance(realToBalance);

            userBillService.save(outBill);
            userBillService.save(inBill);
            save(transfer);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("转账失败");
        }
        return true;
    }

    @Override
    public PageInfo<UserMoneyTransferResponse> getAdminList(UserMoneyTransferSearchRequest request, PageParamRequest pageParamRequest) {
        Page<UserMoneyTransfer> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserMoneyTransfer> wrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(request.getFromUid())) {
            wrapper.eq(UserMoneyTransfer::getFromUid, request.getFromUid());
        }
        if (ObjectUtil.isNotNull(request.getToUid())) {
            wrapper.eq(UserMoneyTransfer::getToUid, request.getToUid());
        }
        if (ObjectUtil.isNotNull(request.getUid())) {
            wrapper.and(w -> w.eq(UserMoneyTransfer::getFromUid, request.getUid())
                    .or().eq(UserMoneyTransfer::getToUid, request.getUid()));
        }
        if (StringUtils.isNotBlank(request.getTransferNo())) {
            wrapper.eq(UserMoneyTransfer::getTransferNo, request.getTransferNo().trim());
        }
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = CrmebDateUtil.getDateLimit(request.getDateLimit());
            wrapper.between(UserMoneyTransfer::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        wrapper.orderByDesc(UserMoneyTransfer::getCreateTime, UserMoneyTransfer::getId);
        List<UserMoneyTransfer> list = dao.selectList(wrapper);

        Set<Integer> uidSet = new HashSet<>();
        list.forEach(item -> {
            uidSet.add(item.getFromUid());
            uidSet.add(item.getToUid());
        });
        HashMap<Integer, User> userMap = CollUtil.isEmpty(uidSet)
                ? new HashMap<>()
                : userService.getMapListInUid(new ArrayList<>(uidSet));

        List<UserMoneyTransferResponse> responseList = list.stream().map(item -> {
            UserMoneyTransferResponse response = new UserMoneyTransferResponse();
            BeanUtils.copyProperties(item, response);
            User fromUser = userMap.get(item.getFromUid());
            User toUser = userMap.get(item.getToUid());
            response.setFromNickname(ObjectUtil.isNotNull(fromUser) ? fromUser.getNickname() : "");
            response.setToNickname(ObjectUtil.isNotNull(toUser) ? toUser.getNickname() : "");
            return response;
        }).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    private UserBill buildBill(Integer uid, String linkId, int pm, String title, String type,
                               BigDecimal number, BigDecimal balance, String mark) {
        UserBill bill = new UserBill();
        bill.setUid(uid);
        bill.setLinkId(linkId);
        bill.setPm(pm);
        bill.setTitle(title);
        bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        bill.setType(type);
        bill.setNumber(number);
        bill.setBalance(balance);
        bill.setMark(mark);
        bill.setStatus(1);
        return bill;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return ObjectUtil.isNull(value) ? BigDecimal.ZERO : value;
    }

    private String maskNickname(String nickname) {
        if (StrUtil.isBlank(nickname)) {
            return "***";
        }
        String name = nickname.trim();
        if (name.length() == 1) {
            return name + "***";
        }
        return name.substring(0, 1) + "***";
    }
}
