package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.IntegralRecordConstants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.constants.VoucherRecordConstants;
import com.zbkj.common.constants.WarrantRecordConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.model.user.UserIntegralRecord;
import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.request.*;
import com.zbkj.common.response.VoucherWarrantConfigResponse;
import com.zbkj.common.response.VoucherWarrantUserResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.service.service.*;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * 消费券与权证兑换服务实现
 */
@Service
public class VoucherWarrantServiceImpl implements VoucherWarrantService {

    private static final Logger logger = LoggerFactory.getLogger(VoucherWarrantServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UserVoucherRecordService userVoucherRecordService;

    @Autowired
    private UserWarrantRecordService userWarrantRecordService;

    @Autowired
    private UserIntegralRecordService userIntegralRecordService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public VoucherWarrantUserResponse getUserAsset() {
        User user = userService.getInfoException();
        VoucherWarrantUserResponse response = new VoucherWarrantUserResponse();
        response.setIntegral(user.getIntegral());
        response.setConsumeVoucher(nullToZero(user.getConsumeVoucher()));
        response.setWarrant(nullToZero(user.getWarrant()));
        response.setWarrantAddress(user.getWarrantAddress());
        response.setWarrantAddressTime(user.getWarrantAddressTime());
        response.setNowMoney(user.getNowMoney());
        response.setIntegralToVoucherRatio(getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO, "100"));
        response.setVoucherToBalanceRatio(getDecimalConfig(SysConfigConstants.CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO, "10"));
        response.setWarrantNeedVoucher(getDecimalConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER, "5"));
        response.setWarrantNeedIntegral(getIntConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL, 100));
        response.setSwitchOn(isSwitchOn());
        return response;
    }

    @Override
    public Boolean integralToVoucher(IntegralToVoucherRequest request) {
        checkSwitchOn();
        User user = userService.getInfoException();
        int useIntegral = request.getIntegral();
        BigDecimal ratio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO, "100");
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("积分兑换消费券比例未正确配置");
        }
        if (useIntegral < ratio.intValue()) {
            throw new CrmebException(StrUtil.format("兑换积分数至少为{}", ratio.intValue()));
        }
        // 只兑换整份：向下取整
        int exchangeTimes = useIntegral / ratio.intValue();
        int realIntegral = exchangeTimes * ratio.intValue();
        BigDecimal voucherAmount = BigDecimal.valueOf(exchangeTimes);
        if (realIntegral <= 0 || voucherAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("兑换结果为0，请调整积分数");
        }
        if (user.getIntegral() < realIntegral) {
            throw new CrmebException("积分不足");
        }
        return doIntegralToVoucher(user, realIntegral, voucherAmount, VoucherRecordConstants.LINK_TYPE_EXCHANGE,
                VoucherRecordConstants.TITLE_EXCHANGE, "用户自主兑换");
    }

    @Override
    public Boolean voucherToBalance(VoucherToBalanceRequest request) {
        checkSwitchOn();
        User user = userService.getInfoException();
        BigDecimal useVoucher = request.getVoucher();
        BigDecimal ratio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO, "10");
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("消费券兑换余额比例未正确配置");
        }
        if (nullToZero(user.getConsumeVoucher()).compareTo(useVoucher) < 0) {
            throw new CrmebException("消费券不足");
        }
        // 整份兑换：向下取整
        BigDecimal times = useVoucher.divide(ratio, 0, RoundingMode.DOWN);
        if (times.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException(StrUtil.format("兑换消费券至少为{}", ratio));
        }
        BigDecimal realVoucher = times.multiply(ratio);
        BigDecimal balanceAmount = times;

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (nullToZero(fresh.getConsumeVoucher()).compareTo(realVoucher) < 0) {
                throw new CrmebException("消费券不足");
            }
            Boolean subOk = userService.operationVoucher(fresh.getUid(), realVoucher, nullToZero(fresh.getConsumeVoucher()), "sub");
            if (!subOk) {
                throw new CrmebException("扣减消费券失败");
            }
            Boolean addOk = userService.operationNowMoney(fresh.getUid(), balanceAmount, fresh.getNowMoney(), "add");
            if (!addOk) {
                throw new CrmebException("增加余额失败");
            }

            Date now = CrmebDateUtil.nowDateTime();
            UserVoucherRecord voucherRecord = new UserVoucherRecord();
            voucherRecord.setUid(fresh.getUid());
            voucherRecord.setLinkId("0");
            voucherRecord.setLinkType(VoucherRecordConstants.LINK_TYPE_TO_BALANCE);
            voucherRecord.setType(VoucherRecordConstants.TYPE_SUB);
            voucherRecord.setTitle(VoucherRecordConstants.TITLE_TO_BALANCE);
            voucherRecord.setVoucher(realVoucher);
            voucherRecord.setBalance(nullToZero(fresh.getConsumeVoucher()).subtract(realVoucher));
            voucherRecord.setMark(StrUtil.format("消费券{}兑换余额{}", realVoucher, balanceAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);

            UserBill userBill = new UserBill();
            userBill.setUid(fresh.getUid());
            userBill.setLinkId("0");
            userBill.setPm(1);
            userBill.setTitle("消费券兑换余额");
            userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
            userBill.setType("voucher_exchange");
            userBill.setNumber(balanceAmount);
            userBill.setBalance(fresh.getNowMoney().add(balanceAmount));
            userBill.setMark(StrUtil.format("消费券兑换增加余额{}", balanceAmount));
            userBill.setStatus(1);
            userBill.setCreateTime(now);
            userBillService.save(userBill);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("消费券兑换余额失败");
        }
        return true;
    }

    @Override
    public Boolean exchangeWarrant(ExchangeWarrantRequest request) {
        checkSwitchOn();
        String payType = StrUtil.trim(request.getPayType()).toLowerCase();
        if (!"integral".equals(payType) && !"voucher".equals(payType)) {
            throw new CrmebException("兑换方式仅支持积分或消费券");
        }
        if ("integral".equals(payType)) {
            return exchangeWarrantByIntegral(request.getAmount());
        }
        return exchangeWarrantByVoucher(request.getAmount());
    }

    private Boolean exchangeWarrantByIntegral(BigDecimal amount) {
        User user = userService.getInfoException();
        int ratio = getIntConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL, 100);
        if (ratio <= 0) {
            throw new CrmebException("积分兑权证比例未正确配置");
        }
        int useIntegral = amount.setScale(0, RoundingMode.DOWN).intValue();
        if (useIntegral < ratio) {
            throw new CrmebException(StrUtil.format("兑换积分数至少为{}", ratio));
        }
        int times = useIntegral / ratio;
        int realIntegral = times * ratio;
        BigDecimal warrantAmount = BigDecimal.valueOf(times);
        if (user.getIntegral() < realIntegral) {
            throw new CrmebException("积分不足");
        }

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (fresh.getIntegral() < realIntegral) {
                throw new CrmebException("积分不足");
            }
            if (!userService.operationIntegral(fresh.getUid(), realIntegral, fresh.getIntegral(), "sub")) {
                throw new CrmebException("扣减积分失败");
            }
            if (!userService.operationWarrant(fresh.getUid(), warrantAmount, nullToZero(fresh.getWarrant()), "add")) {
                throw new CrmebException("增加权证失败");
            }

            Date now = CrmebDateUtil.nowDateTime();
            UserIntegralRecord integralRecord = new UserIntegralRecord();
            integralRecord.setUid(fresh.getUid());
            integralRecord.setLinkId("0");
            integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_SYSTEM);
            integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB);
            integralRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_INTEGRAL);
            integralRecord.setIntegral(realIntegral);
            integralRecord.setBalance(fresh.getIntegral() - realIntegral);
            integralRecord.setMark(StrUtil.format("积分{}兑换权证{}", realIntegral, warrantAmount));
            integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
            integralRecord.setCreateTime(now);
            integralRecord.setUpdateTime(now);
            userIntegralRecordService.save(integralRecord);

            UserWarrantRecord warrantRecord = new UserWarrantRecord();
            warrantRecord.setUid(fresh.getUid());
            warrantRecord.setLinkId("0");
            warrantRecord.setLinkType(WarrantRecordConstants.LINK_TYPE_EXCHANGE);
            warrantRecord.setType(WarrantRecordConstants.TYPE_ADD);
            warrantRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_INTEGRAL);
            warrantRecord.setWarrant(warrantAmount);
            warrantRecord.setBalance(nullToZero(fresh.getWarrant()).add(warrantAmount));
            warrantRecord.setMark(StrUtil.format("消耗积分{}兑换权证{}", realIntegral, warrantAmount));
            warrantRecord.setStatus(WarrantRecordConstants.STATUS_COMPLETE);
            warrantRecord.setCreateTime(now);
            warrantRecord.setUpdateTime(now);
            userWarrantRecordService.save(warrantRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("积分兑换权证失败");
        }
        return true;
    }

    private Boolean exchangeWarrantByVoucher(BigDecimal amount) {
        User user = userService.getInfoException();
        BigDecimal ratio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER, "5");
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("消费券兑权证比例未正确配置");
        }
        if (nullToZero(user.getConsumeVoucher()).compareTo(amount) < 0) {
            throw new CrmebException("消费券不足");
        }
        BigDecimal times = amount.divide(ratio, 0, RoundingMode.DOWN);
        if (times.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException(StrUtil.format("兑换消费券至少为{}", ratio));
        }
        BigDecimal realVoucher = times.multiply(ratio);
        BigDecimal warrantAmount = times;

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (nullToZero(fresh.getConsumeVoucher()).compareTo(realVoucher) < 0) {
                throw new CrmebException("消费券不足");
            }
            if (!userService.operationVoucher(fresh.getUid(), realVoucher, nullToZero(fresh.getConsumeVoucher()), "sub")) {
                throw new CrmebException("扣减消费券失败");
            }
            if (!userService.operationWarrant(fresh.getUid(), warrantAmount, nullToZero(fresh.getWarrant()), "add")) {
                throw new CrmebException("增加权证失败");
            }

            Date now = CrmebDateUtil.nowDateTime();
            UserVoucherRecord voucherRecord = new UserVoucherRecord();
            voucherRecord.setUid(fresh.getUid());
            voucherRecord.setLinkId("0");
            voucherRecord.setLinkType(VoucherRecordConstants.LINK_TYPE_TO_WARRANT);
            voucherRecord.setType(VoucherRecordConstants.TYPE_SUB);
            voucherRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_VOUCHER);
            voucherRecord.setVoucher(realVoucher);
            voucherRecord.setBalance(nullToZero(fresh.getConsumeVoucher()).subtract(realVoucher));
            voucherRecord.setMark(StrUtil.format("消费券{}兑换权证{}", realVoucher, warrantAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);

            UserWarrantRecord warrantRecord = new UserWarrantRecord();
            warrantRecord.setUid(fresh.getUid());
            warrantRecord.setLinkId("0");
            warrantRecord.setLinkType(WarrantRecordConstants.LINK_TYPE_EXCHANGE);
            warrantRecord.setType(WarrantRecordConstants.TYPE_ADD);
            warrantRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_VOUCHER);
            warrantRecord.setWarrant(warrantAmount);
            warrantRecord.setBalance(nullToZero(fresh.getWarrant()).add(warrantAmount));
            warrantRecord.setMark(StrUtil.format("消耗消费券{}兑换权证{}", realVoucher, warrantAmount));
            warrantRecord.setStatus(WarrantRecordConstants.STATUS_COMPLETE);
            warrantRecord.setCreateTime(now);
            warrantRecord.setUpdateTime(now);
            userWarrantRecordService.save(warrantRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("消费券兑换权证失败");
        }
        return true;
    }

    @Override
    public List<UserVoucherRecord> getVoucherRecordList(PageParamRequest pageParamRequest) {
        User user = userService.getInfoException();
        return userVoucherRecordService.findUserRecordList(user.getUid(), pageParamRequest);
    }

    @Override
    public List<UserWarrantRecord> getWarrantRecordList(PageParamRequest pageParamRequest) {
        User user = userService.getInfoException();
        return userWarrantRecordService.findUserRecordList(user.getUid(), pageParamRequest);
    }

    @Override
    public void dailyReleaseIntegralToVoucher() {
        if (!isSwitchOn()) {
            logger.info("消费券权证功能已关闭，跳过每日释放");
            return;
        }
        BigDecimal releasePercent = getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO, "10");
        BigDecimal ratio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO, "100");
        if (releasePercent.compareTo(BigDecimal.ZERO) <= 0 || ratio.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("每日释放比例或兑换比例配置无效，跳过");
            return;
        }

        int ratioInt = ratio.intValue();
        int pageSize = 200;
        int pageNo = 1;
        while (true) {
            PageHelper.startPage(pageNo, pageSize, false);
            LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
            lqw.gt(User::getIntegral, 0);
            lqw.select(User::getUid, User::getIntegral, User::getConsumeVoucher);
            lqw.orderByAsc(User::getUid);
            List<User> userList = userService.list(lqw);
            if (ObjectUtil.isNull(userList) || userList.isEmpty()) {
                break;
            }
            for (User user : userList) {
                try {
                    if (userVoucherRecordService.existsTodayByUidAndLinkType(user.getUid(), VoucherRecordConstants.LINK_TYPE_DAILY_RELEASE)) {
                        continue;
                    }
                    int releaseIntegral = BigDecimal.valueOf(user.getIntegral())
                            .multiply(releasePercent)
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                            .intValue();
                    if (releaseIntegral < ratioInt) {
                        continue;
                    }
                    int exchangeTimes = releaseIntegral / ratioInt;
                    int realIntegral = exchangeTimes * ratioInt;
                    BigDecimal voucherAmount = BigDecimal.valueOf(exchangeTimes);
                    if (realIntegral <= 0) {
                        continue;
                    }
                    User fresh = userService.getById(user.getUid());
                    if (ObjectUtil.isNull(fresh) || fresh.getIntegral() < realIntegral) {
                        continue;
                    }
                    // 二次幂等，避免并发重复释放
                    if (userVoucherRecordService.existsTodayByUidAndLinkType(fresh.getUid(), VoucherRecordConstants.LINK_TYPE_DAILY_RELEASE)) {
                        continue;
                    }
                    doIntegralToVoucher(fresh, realIntegral, voucherAmount,
                            VoucherRecordConstants.LINK_TYPE_DAILY_RELEASE,
                            VoucherRecordConstants.TITLE_DAILY_RELEASE,
                            StrUtil.format("每日释放比例{}%，释放积分{}", releasePercent, realIntegral));
                } catch (Exception ex) {
                    logger.error("每日积分释放失败 uid={}", user.getUid(), ex);
                }
            }
            if (userList.size() < pageSize) {
                break;
            }
            pageNo++;
        }
    }

    @Override
    public VoucherWarrantConfigResponse getConfig() {
        VoucherWarrantConfigResponse response = new VoucherWarrantConfigResponse();
        response.setIntegralToVoucherRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO), "100"));
        response.setIntegralDailyReleaseRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO), "10"));
        response.setVoucherToBalanceRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO), "10"));
        response.setWarrantNeedVoucher(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER), "5"));
        response.setWarrantNeedIntegral(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL), "100"));
        response.setVoucherWarrantSwitch(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_WARRANT_SWITCH), "1"));
        return response;
    }

    @Override
    public Boolean saveConfig(VoucherWarrantConfigRequest request) {
        validatePositiveNumber(request.getIntegralToVoucherRatio(), "积分兑换消费券比例");
        validatePercent(request.getIntegralDailyReleaseRatio(), "每日释放百分比");
        validatePositiveNumber(request.getVoucherToBalanceRatio(), "消费券兑换余额比例");
        validatePositiveNumber(request.getWarrantNeedVoucher(), "消费券兑1权证所需数量");
        validatePositiveNumber(request.getWarrantNeedIntegral(), "积分兑1权证所需数量");

        if (StrUtil.isNotBlank(request.getIntegralToVoucherRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO, request.getIntegralToVoucherRatio().trim());
        }
        if (StrUtil.isNotBlank(request.getIntegralDailyReleaseRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO, request.getIntegralDailyReleaseRatio().trim());
        }
        if (StrUtil.isNotBlank(request.getVoucherToBalanceRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO, request.getVoucherToBalanceRatio().trim());
        }
        if (StrUtil.isNotBlank(request.getWarrantNeedVoucher())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER, request.getWarrantNeedVoucher().trim());
        }
        if (StrUtil.isNotBlank(request.getWarrantNeedIntegral())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL, request.getWarrantNeedIntegral().trim());
        }
        if (StrUtil.isNotBlank(request.getVoucherWarrantSwitch())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_VOUCHER_WARRANT_SWITCH, request.getVoucherWarrantSwitch().trim());
        }
        return true;
    }

    @Override
    public Boolean adminOperate(UserOperateVoucherWarrantRequest request) {
        if ((ObjectUtil.isNull(request.getVoucherValue()) || request.getVoucherValue().compareTo(BigDecimal.ZERO) <= 0)
                && (ObjectUtil.isNull(request.getWarrantValue()) || request.getWarrantValue().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new CrmebException("至少输入一个变动值");
        }
        User user = userService.getById(request.getUid());
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("用户不存在");
        }
        Boolean execute = transactionTemplate.execute(e -> {
            Date now = CrmebDateUtil.nowDateTime();
            if (ObjectUtil.isNotNull(request.getVoucherValue()) && request.getVoucherValue().compareTo(BigDecimal.ZERO) > 0) {
                User fresh = userService.getById(request.getUid());
                String opType = request.getVoucherType() == 1 ? "add" : "sub";
                if ("sub".equals(opType) && nullToZero(fresh.getConsumeVoucher()).compareTo(request.getVoucherValue()) < 0) {
                    throw new CrmebException("消费券扣减后不能小于0");
                }
                if (!userService.operationVoucher(fresh.getUid(), request.getVoucherValue(), nullToZero(fresh.getConsumeVoucher()), opType)) {
                    throw new CrmebException("消费券操作失败");
                }
                UserVoucherRecord record = new UserVoucherRecord();
                record.setUid(fresh.getUid());
                record.setLinkId("0");
                record.setLinkType(VoucherRecordConstants.LINK_TYPE_SYSTEM);
                record.setType(request.getVoucherType() == 1 ? VoucherRecordConstants.TYPE_ADD : VoucherRecordConstants.TYPE_SUB);
                record.setTitle(VoucherRecordConstants.TITLE_SYSTEM);
                record.setVoucher(request.getVoucherValue());
                record.setBalance(request.getVoucherType() == 1
                        ? nullToZero(fresh.getConsumeVoucher()).add(request.getVoucherValue())
                        : nullToZero(fresh.getConsumeVoucher()).subtract(request.getVoucherValue()));
                record.setMark(StrUtil.format("后台{}消费券{}", request.getVoucherType() == 1 ? "增加" : "减少", request.getVoucherValue()));
                record.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
                record.setCreateTime(now);
                record.setUpdateTime(now);
                userVoucherRecordService.save(record);
            }
            if (ObjectUtil.isNotNull(request.getWarrantValue()) && request.getWarrantValue().compareTo(BigDecimal.ZERO) > 0) {
                User fresh = userService.getById(request.getUid());
                String opType = request.getWarrantType() == 1 ? "add" : "sub";
                if ("sub".equals(opType) && nullToZero(fresh.getWarrant()).compareTo(request.getWarrantValue()) < 0) {
                    throw new CrmebException("权证扣减后不能小于0");
                }
                if (!userService.operationWarrant(fresh.getUid(), request.getWarrantValue(), nullToZero(fresh.getWarrant()), opType)) {
                    throw new CrmebException("权证操作失败");
                }
                UserWarrantRecord record = new UserWarrantRecord();
                record.setUid(fresh.getUid());
                record.setLinkId("0");
                record.setLinkType(WarrantRecordConstants.LINK_TYPE_SYSTEM);
                record.setType(request.getWarrantType() == 1 ? WarrantRecordConstants.TYPE_ADD : WarrantRecordConstants.TYPE_SUB);
                record.setTitle(WarrantRecordConstants.TITLE_SYSTEM);
                record.setWarrant(request.getWarrantValue());
                record.setBalance(request.getWarrantType() == 1
                        ? nullToZero(fresh.getWarrant()).add(request.getWarrantValue())
                        : nullToZero(fresh.getWarrant()).subtract(request.getWarrantValue()));
                record.setMark(StrUtil.format("后台{}权证{}", request.getWarrantType() == 1 ? "增加" : "减少", request.getWarrantValue()));
                record.setStatus(WarrantRecordConstants.STATUS_COMPLETE);
                record.setCreateTime(now);
                record.setUpdateTime(now);
                userWarrantRecordService.save(record);
            }
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("操作失败");
        }
        return true;
    }

    @Override
    public Boolean bindWarrantAddress(WarrantAddressRequest request) {
        checkSwitchOn();
        String address = StrUtil.trim(request.getAddress());
        if (StrUtil.isBlank(address)) {
            throw new CrmebException("地址不能为空");
        }
        if (address.length() > 255) {
            throw new CrmebException("地址长度不能超过255");
        }
        User user = userService.getInfoException();
        User update = new User();
        update.setUid(user.getUid());
        update.setWarrantAddress(address);
        update.setWarrantAddressTime(CrmebDateUtil.nowDateTime());
        update.setUpdateTime(CrmebDateUtil.nowDateTime());
        return userService.updateById(update);
    }

    private Boolean doIntegralToVoucher(User user, int realIntegral, BigDecimal voucherAmount,
                                        String linkType, String title, String markSuffix) {
        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (ObjectUtil.isNull(fresh) || fresh.getIntegral() < realIntegral) {
                throw new CrmebException("积分不足");
            }
            if (!userService.operationIntegral(fresh.getUid(), realIntegral, fresh.getIntegral(), "sub")) {
                throw new CrmebException("扣减积分失败");
            }
            if (!userService.operationVoucher(fresh.getUid(), voucherAmount, nullToZero(fresh.getConsumeVoucher()), "add")) {
                throw new CrmebException("增加消费券失败");
            }

            Date now = CrmebDateUtil.nowDateTime();
            UserIntegralRecord integralRecord = new UserIntegralRecord();
            integralRecord.setUid(fresh.getUid());
            integralRecord.setLinkId("0");
            integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_SYSTEM);
            integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB);
            integralRecord.setTitle(title);
            integralRecord.setIntegral(realIntegral);
            integralRecord.setBalance(fresh.getIntegral() - realIntegral);
            integralRecord.setMark(StrUtil.format("{}，扣减积分{}", markSuffix, realIntegral));
            integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
            integralRecord.setCreateTime(now);
            integralRecord.setUpdateTime(now);
            userIntegralRecordService.save(integralRecord);

            UserVoucherRecord voucherRecord = new UserVoucherRecord();
            voucherRecord.setUid(fresh.getUid());
            voucherRecord.setLinkId("0");
            voucherRecord.setLinkType(linkType);
            voucherRecord.setType(VoucherRecordConstants.TYPE_ADD);
            voucherRecord.setTitle(title);
            voucherRecord.setVoucher(voucherAmount);
            voucherRecord.setBalance(nullToZero(fresh.getConsumeVoucher()).add(voucherAmount));
            voucherRecord.setMark(StrUtil.format("{}，增加消费券{}", markSuffix, voucherAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("积分兑换消费券失败");
        }
        return true;
    }

    private void checkSwitchOn() {
        if (!isSwitchOn()) {
            throw new CrmebException("消费券权证功能未开启");
        }
    }

    private boolean isSwitchOn() {
        String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_WARRANT_SWITCH);
        return !"0".equals(value);
    }

    private BigDecimal getDecimalConfig(String key, String defaultValue) {
        String value = systemConfigService.getValueByKey(key);
        if (StrUtil.isBlank(value)) {
            value = defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return new BigDecimal(defaultValue);
        }
    }

    private int getIntConfig(String key, int defaultValue) {
        String value = systemConfigService.getValueByKey(key);
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return ObjectUtil.isNull(value) ? BigDecimal.ZERO : value;
    }

    private String defaultStr(String value, String defaultValue) {
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    private void validatePositiveNumber(String value, String label) {
        if (StrUtil.isBlank(value)) {
            return;
        }
        try {
            BigDecimal number = new BigDecimal(value.trim());
            if (number.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CrmebException(label + "必须大于0");
            }
        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            throw new CrmebException(label + "必须是有效数字");
        }
    }

    private void validatePercent(String value, String label) {
        if (StrUtil.isBlank(value)) {
            return;
        }
        try {
            BigDecimal number = new BigDecimal(value.trim());
            if (number.compareTo(BigDecimal.ZERO) <= 0 || number.compareTo(new BigDecimal("100")) > 0) {
                throw new CrmebException(label + "需在0到100之间（不含0）");
            }
        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            throw new CrmebException(label + "必须是有效数字");
        }
    }
}
