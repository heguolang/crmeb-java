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
import com.zbkj.common.model.finance.UserWarrantExchange;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.model.user.UserIntegralRecord;
import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.request.*;
import com.zbkj.common.response.UserWarrantRecordFrontResponse;
import com.zbkj.common.response.VoucherWarrantConfigResponse;
import com.zbkj.common.response.VoucherWarrantUserResponse;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.service.service.*;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private UserWarrantExchangeService userWarrantExchangeService;

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
            throw new CrmebException("信用值兑换CCEA比例未正确配置");
        }
        if (useIntegral < ratio.intValue()) {
            throw new CrmebException(StrUtil.format("兑换信用值至少为{}", ratio.intValue()));
        }
        // 只兑换整份：向下取整
        int exchangeTimes = useIntegral / ratio.intValue();
        BigDecimal realIntegral = BigDecimal.valueOf(exchangeTimes).multiply(ratio);
        BigDecimal voucherAmount = BigDecimal.valueOf(exchangeTimes);
        if (realIntegral.compareTo(BigDecimal.ZERO) <= 0 || voucherAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("兑换结果为0，请调整信用值");
        }
        if (nullToZero(user.getIntegral()).compareTo(realIntegral) < 0) {
            throw new CrmebException("信用值不足");
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
            throw new CrmebException("CCEA兑换余额比例未正确配置");
        }
        if (nullToZero(user.getConsumeVoucher()).compareTo(useVoucher) < 0) {
            throw new CrmebException("CCEA不足");
        }
        // 整份兑换：向下取整
        BigDecimal times = useVoucher.divide(ratio, 0, RoundingMode.DOWN);
        if (times.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException(StrUtil.format("兑换CCEA至少为{}", ratio));
        }
        BigDecimal realVoucher = times.multiply(ratio);
        BigDecimal balanceAmount = times;

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (nullToZero(fresh.getConsumeVoucher()).compareTo(realVoucher) < 0) {
                throw new CrmebException("CCEA不足");
            }
            Boolean subOk = userService.operationVoucher(fresh.getUid(), realVoucher, nullToZero(fresh.getConsumeVoucher()), "sub");
            if (!subOk) {
                throw new CrmebException("扣减CCEA失败");
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
            voucherRecord.setMark(StrUtil.format("CCEA{}兑换余额{}", realVoucher, balanceAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);

            UserBill userBill = new UserBill();
            userBill.setUid(fresh.getUid());
            userBill.setLinkId("0");
            userBill.setPm(1);
            userBill.setTitle("CCEA兑换余额");
            userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
            userBill.setType("voucher_exchange");
            userBill.setNumber(balanceAmount);
            userBill.setBalance(fresh.getNowMoney().add(balanceAmount));
            userBill.setMark(StrUtil.format("CCEA兑换增加余额{}", balanceAmount));
            userBill.setStatus(1);
            userBill.setCreateTime(now);
            userBillService.save(userBill);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("CCEA兑换余额失败");
        }
        return true;
    }

    @Override
    public Boolean exchangeWarrant(ExchangeWarrantRequest request) {
        checkSwitchOn();
        String payType = StrUtil.trim(request.getPayType()).toLowerCase();
        if (!"integral".equals(payType) && !"voucher".equals(payType)) {
            throw new CrmebException("兑换方式仅支持信用值或CCEA");
        }
        String address = normalizeWarrantAddress(request.getAddress());
        if ("integral".equals(payType)) {
            return exchangeWarrantByIntegral(request.getAmount(), address);
        }
        return exchangeWarrantByVoucher(request.getAmount(), address);
    }

    private Boolean exchangeWarrantByIntegral(BigDecimal amount, String address) {
        User user = userService.getInfoException();
        int ratio = getIntConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL, 100);
        if (ratio <= 0) {
            throw new CrmebException("信用值兑CEA比例未正确配置");
        }
        int useIntegral = amount.setScale(0, RoundingMode.DOWN).intValue();
        if (useIntegral <= 0) {
            throw new CrmebException("请输入有效信用值");
        }
        // 按比例精确兑换，支持到 0.001：如 1000 积分 = 1 权证，则 1 积分 = 0.001 权证
        BigDecimal warrantAmount = BigDecimal.valueOf(useIntegral)
                .divide(BigDecimal.valueOf(ratio), 3, RoundingMode.DOWN);
        if (warrantAmount.compareTo(new BigDecimal("0.001")) < 0) {
            throw new CrmebException(StrUtil.format("兑换后CEA不足0.001，至少需要{}信用值",
                    BigDecimal.valueOf(ratio).multiply(new BigDecimal("0.001")).setScale(0, RoundingMode.UP).intValue()));
        }
        BigDecimal useIntegralDecimal = BigDecimal.valueOf(useIntegral);
        if (nullToZero(user.getIntegral()).compareTo(useIntegralDecimal) < 0) {
            throw new CrmebException("信用值不足");
        }

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (nullToZero(fresh.getIntegral()).compareTo(useIntegralDecimal) < 0) {
                throw new CrmebException("信用值不足");
            }
            if (!userService.operationIntegral(fresh.getUid(), useIntegralDecimal, nullToZero(fresh.getIntegral()), "sub")) {
                throw new CrmebException("扣减信用值失败");
            }
            if (!userService.operationWarrant(fresh.getUid(), warrantAmount, nullToZero(fresh.getWarrant()), "add")) {
                throw new CrmebException("增加CEA失败");
            }
            saveWarrantAddress(fresh.getUid(), address);

            Date now = CrmebDateUtil.nowDateTime();
            UserIntegralRecord integralRecord = new UserIntegralRecord();
            integralRecord.setUid(fresh.getUid());
            integralRecord.setLinkId("0");
            integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_SYSTEM);
            integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB);
            integralRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_INTEGRAL);
            integralRecord.setIntegral(useIntegralDecimal);
            integralRecord.setBalance(nullToZero(fresh.getIntegral()).subtract(useIntegralDecimal));
            integralRecord.setMark(StrUtil.format("信用值{}兑换CEA{}", useIntegral, warrantAmount));
            integralRecord.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
            integralRecord.setCreateTime(now);
            integralRecord.setUpdateTime(now);
            userIntegralRecordService.save(integralRecord);

            UserWarrantRecord warrantRecord = new UserWarrantRecord();
            warrantRecord.setUid(fresh.getUid());
            warrantRecord.setLinkType(WarrantRecordConstants.LINK_TYPE_EXCHANGE);
            warrantRecord.setType(WarrantRecordConstants.TYPE_ADD);
            warrantRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_INTEGRAL);
            warrantRecord.setWarrant(warrantAmount);
            warrantRecord.setBalance(nullToZero(fresh.getWarrant()).add(warrantAmount));
            warrantRecord.setMark(StrUtil.format("消耗信用值{}兑换CEA{}，地址{}", useIntegral, warrantAmount, address));
            warrantRecord.setStatus(WarrantRecordConstants.STATUS_COMPLETE);
            warrantRecord.setCreateTime(now);
            warrantRecord.setUpdateTime(now);
            Integer applyId = userWarrantExchangeService.createApply(fresh.getUid(), "integral",
                    BigDecimal.valueOf(useIntegral), warrantAmount, address);
            warrantRecord.setLinkId(String.valueOf(applyId));
            userWarrantRecordService.save(warrantRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("信用值兑换CEA失败");
        }
        return true;
    }

    private Boolean exchangeWarrantByVoucher(BigDecimal amount, String address) {
        User user = userService.getInfoException();
        BigDecimal ratio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER, "5");
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("CCEA兑CEA比例未正确配置");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrmebException("请输入有效CCEA数量");
        }
        if (nullToZero(user.getConsumeVoucher()).compareTo(amount) < 0) {
            throw new CrmebException("CCEA不足");
        }
        BigDecimal realVoucher = amount.setScale(2, RoundingMode.DOWN);
        // 按比例精确兑换，支持到 0.001
        BigDecimal warrantAmount = realVoucher.divide(ratio, 3, RoundingMode.DOWN);
        if (warrantAmount.compareTo(new BigDecimal("0.001")) < 0) {
            throw new CrmebException(StrUtil.format("兑换后CEA不足0.001，至少需要{}CCEA",
                    ratio.multiply(new BigDecimal("0.001")).stripTrailingZeros().toPlainString()));
        }

        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (nullToZero(fresh.getConsumeVoucher()).compareTo(realVoucher) < 0) {
                throw new CrmebException("CCEA不足");
            }
            if (!userService.operationVoucher(fresh.getUid(), realVoucher, nullToZero(fresh.getConsumeVoucher()), "sub")) {
                throw new CrmebException("扣减CCEA失败");
            }
            if (!userService.operationWarrant(fresh.getUid(), warrantAmount, nullToZero(fresh.getWarrant()), "add")) {
                throw new CrmebException("增加CEA失败");
            }
            saveWarrantAddress(fresh.getUid(), address);

            Date now = CrmebDateUtil.nowDateTime();
            UserVoucherRecord voucherRecord = new UserVoucherRecord();
            voucherRecord.setUid(fresh.getUid());
            voucherRecord.setLinkId("0");
            voucherRecord.setLinkType(VoucherRecordConstants.LINK_TYPE_TO_WARRANT);
            voucherRecord.setType(VoucherRecordConstants.TYPE_SUB);
            voucherRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_VOUCHER);
            voucherRecord.setVoucher(realVoucher);
            voucherRecord.setBalance(nullToZero(fresh.getConsumeVoucher()).subtract(realVoucher));
            voucherRecord.setMark(StrUtil.format("CCEA{}兑换CEA{}", realVoucher, warrantAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);

            UserWarrantRecord warrantRecord = new UserWarrantRecord();
            warrantRecord.setUid(fresh.getUid());
            warrantRecord.setLinkType(WarrantRecordConstants.LINK_TYPE_EXCHANGE);
            warrantRecord.setType(WarrantRecordConstants.TYPE_ADD);
            warrantRecord.setTitle(WarrantRecordConstants.TITLE_EXCHANGE_VOUCHER);
            warrantRecord.setWarrant(warrantAmount);
            warrantRecord.setBalance(nullToZero(fresh.getWarrant()).add(warrantAmount));
            warrantRecord.setMark(StrUtil.format("消耗CCEA{}兑换CEA{}，地址{}", realVoucher, warrantAmount, address));
            warrantRecord.setStatus(WarrantRecordConstants.STATUS_COMPLETE);
            warrantRecord.setCreateTime(now);
            warrantRecord.setUpdateTime(now);
            Integer applyId = userWarrantExchangeService.createApply(fresh.getUid(), "voucher",
                    realVoucher, warrantAmount, address);
            warrantRecord.setLinkId(String.valueOf(applyId));
            userWarrantRecordService.save(warrantRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("CCEA兑换CEA失败");
        }
        return true;
    }

    @Override
    public List<UserVoucherRecord> getVoucherRecordList(PageParamRequest pageParamRequest) {
        User user = userService.getInfoException();
        return userVoucherRecordService.findUserRecordList(user.getUid(), pageParamRequest);
    }

    @Override
    public List<UserWarrantRecordFrontResponse> getWarrantRecordList(PageParamRequest pageParamRequest) {
        User user = userService.getInfoException();
        List<UserWarrantRecord> recordList = userWarrantRecordService.findUserRecordList(user.getUid(), pageParamRequest);
        return fillProcessStatus(user.getUid(), recordList);
    }

    /**
     * 同步后台权证兑换审核状态到用户端流水
     */
    private List<UserWarrantRecordFrontResponse> fillProcessStatus(Integer uid, List<UserWarrantRecord> recordList) {
        if (ObjectUtil.isNull(recordList) || recordList.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserWarrantExchange> exchangeList = userWarrantExchangeService.list(Wrappers.<UserWarrantExchange>lambdaQuery()
                .eq(UserWarrantExchange::getUid, uid)
                .orderByDesc(UserWarrantExchange::getCreateTime));
        Map<Integer, UserWarrantExchange> exchangeById = exchangeList.stream()
                .filter(item -> ObjectUtil.isNotNull(item.getId()))
                .collect(Collectors.toMap(UserWarrantExchange::getId, item -> item, (a, b) -> a));

        return recordList.stream().map(record -> {
            UserWarrantRecordFrontResponse response = new UserWarrantRecordFrontResponse();
            BeanUtils.copyProperties(record, response);
            if (!WarrantRecordConstants.LINK_TYPE_EXCHANGE.equals(record.getLinkType())) {
                // 后台直接操作等无需审核
                response.setProcessStatus(null);
                response.setProcessStatusText("");
                return response;
            }
            UserWarrantExchange matched = null;
            if (StrUtil.isNotBlank(record.getLinkId()) && !"0".equals(record.getLinkId()) && record.getLinkId().matches("\\d+")) {
                matched = exchangeById.get(Integer.valueOf(record.getLinkId()));
            }
            // 兼容历史数据：按金额+时间就近匹配
            if (ObjectUtil.isNull(matched) && ObjectUtil.isNotNull(record.getWarrant()) && ObjectUtil.isNotNull(record.getCreateTime())) {
                matched = exchangeList.stream()
                        .filter(item -> ObjectUtil.isNotNull(item.getWarrantAmount())
                                && item.getWarrantAmount().compareTo(record.getWarrant()) == 0
                                && ObjectUtil.isNotNull(item.getCreateTime())
                                && Math.abs(item.getCreateTime().getTime() - record.getCreateTime().getTime()) <= 120000L)
                        .findFirst()
                        .orElse(null);
            }
            if (ObjectUtil.isNotNull(matched)) {
                Integer processStatus = matched.getStatus();
                response.setProcessStatus(processStatus);
                response.setProcessStatusText(Objects.equals(processStatus, UserWarrantExchangeServiceImpl.STATUS_DONE) ? "已处理" : "待处理");
            } else {
                response.setProcessStatus(UserWarrantExchangeServiceImpl.STATUS_PENDING);
                response.setProcessStatusText("待处理");
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public void dailyReleaseIntegralToVoucher() {
        if (!isReleaseSwitchOn()) {
            logger.info("积分释放开关已关闭，跳过每日释放");
            return;
        }
        BigDecimal releasePercent = getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO, "1");
        BigDecimal exchangeRatio = getDecimalConfig(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_EXCHANGE_RATIO, "1");
        if (releasePercent.compareTo(BigDecimal.ZERO) <= 0 || exchangeRatio.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("每日释放百分比或释放兑换比例配置无效，跳过");
            return;
        }

        BigDecimal minRelease = new BigDecimal("0.01");
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
                    BigDecimal currentIntegral = nullToZero(user.getIntegral());
                    // 释放积分 = 当前积分 × 日释放百分比 ÷ 100
                    BigDecimal releaseIntegral = currentIntegral
                            .multiply(releasePercent)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
                    if (releaseIntegral.compareTo(minRelease) < 0) {
                        continue;
                    }
                    // 消费券 = 释放积分 ÷ 释放兑换比例（独立于主动兑换比例）
                    BigDecimal voucherAmount = releaseIntegral.divide(exchangeRatio, 2, RoundingMode.DOWN);
                    if (voucherAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    User fresh = userService.getById(user.getUid());
                    if (ObjectUtil.isNull(fresh) || nullToZero(fresh.getIntegral()).compareTo(releaseIntegral) < 0) {
                        continue;
                    }
                    // 二次幂等，避免并发重复释放
                    if (userVoucherRecordService.existsTodayByUidAndLinkType(fresh.getUid(), VoucherRecordConstants.LINK_TYPE_DAILY_RELEASE)) {
                        continue;
                    }
                    doIntegralToVoucher(fresh, releaseIntegral, voucherAmount,
                            VoucherRecordConstants.LINK_TYPE_DAILY_RELEASE,
                            VoucherRecordConstants.TITLE_DAILY_RELEASE,
                            StrUtil.format("每日释放比例{}%，释放兑换{}信用值=1CCEA，释放信用值{}",
                                    releasePercent, exchangeRatio, releaseIntegral));
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
        response.setIntegralDailyReleaseRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO), "1"));
        response.setIntegralDailyReleaseExchangeRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_EXCHANGE_RATIO), "1"));
        response.setVoucherToBalanceRatio(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO), "10"));
        response.setWarrantNeedVoucher(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_VOUCHER), "5"));
        response.setWarrantNeedIntegral(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_WARRANT_NEED_INTEGRAL), "100"));
        response.setVoucherWarrantSwitch(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_WARRANT_SWITCH), "1"));
        response.setIntegralDailyReleaseSwitch(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_SWITCH), "1"));
        return response;
    }

    @Override
    public Boolean saveConfig(VoucherWarrantConfigRequest request) {
        validatePositiveNumber(request.getIntegralToVoucherRatio(), "信用值兑换CCEA比例");
        validatePercent(request.getIntegralDailyReleaseRatio(), "每日释放百分比");
        validatePositiveNumber(request.getIntegralDailyReleaseExchangeRatio(), "每日释放兑换比例");
        validatePositiveNumber(request.getVoucherToBalanceRatio(), "CCEA兑换余额比例");
        validatePositiveNumber(request.getWarrantNeedVoucher(), "CCEA兑1CEA所需数量");
        validatePositiveNumber(request.getWarrantNeedIntegral(), "信用值兑1CEA所需数量");

        if (StrUtil.isNotBlank(request.getIntegralToVoucherRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO, request.getIntegralToVoucherRatio().trim());
        }
        if (StrUtil.isNotBlank(request.getIntegralDailyReleaseRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO, request.getIntegralDailyReleaseRatio().trim());
        }
        if (StrUtil.isNotBlank(request.getIntegralDailyReleaseExchangeRatio())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_EXCHANGE_RATIO, request.getIntegralDailyReleaseExchangeRatio().trim());
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
        if (StrUtil.isNotBlank(request.getIntegralDailyReleaseSwitch())) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_SWITCH, request.getIntegralDailyReleaseSwitch().trim());
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
                    throw new CrmebException("CCEA扣减后不能小于0");
                }
                if (!userService.operationVoucher(fresh.getUid(), request.getVoucherValue(), nullToZero(fresh.getConsumeVoucher()), opType)) {
                    throw new CrmebException("CCEA操作失败");
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
                record.setMark(StrUtil.format("后台{}CCEA{}", request.getVoucherType() == 1 ? "增加" : "减少", request.getVoucherValue()));
                record.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
                record.setCreateTime(now);
                record.setUpdateTime(now);
                userVoucherRecordService.save(record);
            }
            if (ObjectUtil.isNotNull(request.getWarrantValue()) && request.getWarrantValue().compareTo(BigDecimal.ZERO) > 0) {
                User fresh = userService.getById(request.getUid());
                String opType = request.getWarrantType() == 1 ? "add" : "sub";
                if ("sub".equals(opType) && nullToZero(fresh.getWarrant()).compareTo(request.getWarrantValue()) < 0) {
                    throw new CrmebException("CEA扣减后不能小于0");
                }
                if (!userService.operationWarrant(fresh.getUid(), request.getWarrantValue(), nullToZero(fresh.getWarrant()), opType)) {
                    throw new CrmebException("CEA操作失败");
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
                record.setMark(StrUtil.format("后台{}CEA{}", request.getWarrantType() == 1 ? "增加" : "减少", request.getWarrantValue()));
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
        String address = normalizeWarrantAddress(request.getAddress());
        User user = userService.getInfoException();
        return saveWarrantAddress(user.getUid(), address);
    }

    private String normalizeWarrantAddress(String raw) {
        String address = StrUtil.trim(raw);
        if (StrUtil.isBlank(address)) {
            throw new CrmebException("地址不能为空");
        }
        if (address.length() > 255) {
            throw new CrmebException("地址长度不能超过255");
        }
        return address;
    }

    private Boolean saveWarrantAddress(Integer uid, String address) {
        User update = new User();
        update.setUid(uid);
        update.setWarrantAddress(address);
        update.setWarrantAddressTime(CrmebDateUtil.nowDateTime());
        update.setUpdateTime(CrmebDateUtil.nowDateTime());
        return userService.updateById(update);
    }

    private Boolean doIntegralToVoucher(User user, BigDecimal realIntegral, BigDecimal voucherAmount,
                                        String linkType, String title, String markSuffix) {
        Boolean execute = transactionTemplate.execute(e -> {
            User fresh = userService.getById(user.getUid());
            if (ObjectUtil.isNull(fresh) || nullToZero(fresh.getIntegral()).compareTo(realIntegral) < 0) {
                throw new CrmebException("信用值不足");
            }
            if (!userService.operationIntegral(fresh.getUid(), realIntegral, nullToZero(fresh.getIntegral()), "sub")) {
                throw new CrmebException("扣减信用值失败");
            }
            if (!userService.operationVoucher(fresh.getUid(), voucherAmount, nullToZero(fresh.getConsumeVoucher()), "add")) {
                throw new CrmebException("增加CCEA失败");
            }

            Date now = CrmebDateUtil.nowDateTime();
            UserIntegralRecord integralRecord = new UserIntegralRecord();
            integralRecord.setUid(fresh.getUid());
            integralRecord.setLinkId("0");
            integralRecord.setLinkType(IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_SYSTEM);
            integralRecord.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB);
            integralRecord.setTitle(title);
            integralRecord.setIntegral(realIntegral);
            integralRecord.setBalance(nullToZero(fresh.getIntegral()).subtract(realIntegral));
            integralRecord.setMark(StrUtil.format("{}，扣减信用值{}", markSuffix, realIntegral));
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
            voucherRecord.setMark(StrUtil.format("{}，增加CCEA{}", markSuffix, voucherAmount));
            voucherRecord.setStatus(VoucherRecordConstants.STATUS_COMPLETE);
            voucherRecord.setCreateTime(now);
            voucherRecord.setUpdateTime(now);
            userVoucherRecordService.save(voucherRecord);
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("信用值兑换CCEA失败");
        }
        return true;
    }

    private void checkSwitchOn() {
        if (!isSwitchOn()) {
            throw new CrmebException("兑换功能未开启");
        }
    }

    private boolean isSwitchOn() {
        String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_VOUCHER_WARRANT_SWITCH);
        return !"0".equals(value);
    }

    private boolean isReleaseSwitchOn() {
        String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_DAILY_RELEASE_SWITCH);
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
