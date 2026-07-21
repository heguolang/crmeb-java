package com.zbkj.common.utils;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.SysConfigConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 提现手续费计算
 */
public class ExtractFeeUtil {

    private ExtractFeeUtil() {
    }

    /**
     * 计算手续费
     *
     * @param extractPrice 提现金额
     * @param feeType      fixed / percent
     * @param feeValueStr  固定元或百分比数字
     */
    public static BigDecimal calcFee(BigDecimal extractPrice, String feeType, String feeValueStr) {
        if (extractPrice == null || extractPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal feeValue = parseNonNegative(feeValueStr);
        String type = normalizeFeeType(feeType);
        if (SysConfigConstants.EXTRACT_FEE_TYPE_PERCENT.equals(type)) {
            return extractPrice.multiply(feeValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return feeValue.setScale(2, RoundingMode.HALF_UP);
    }

    public static String normalizeFeeType(String feeType) {
        if (StrUtil.isBlank(feeType)) {
            return SysConfigConstants.EXTRACT_FEE_TYPE_FIXED;
        }
        String t = feeType.trim().toLowerCase();
        if (SysConfigConstants.EXTRACT_FEE_TYPE_PERCENT.equals(t)) {
            return SysConfigConstants.EXTRACT_FEE_TYPE_PERCENT;
        }
        return SysConfigConstants.EXTRACT_FEE_TYPE_FIXED;
    }

    public static String normalizeSource(String source) {
        if (StrUtil.isBlank(source)) {
            return SysConfigConstants.EXTRACT_SOURCE_BROKERAGE;
        }
        String s = source.trim().toLowerCase();
        if (SysConfigConstants.EXTRACT_SOURCE_BALANCE.equals(s)) {
            return SysConfigConstants.EXTRACT_SOURCE_BALANCE;
        }
        return SysConfigConstants.EXTRACT_SOURCE_BROKERAGE;
    }

    public static BigDecimal parseNonNegative(String value) {
        if (StrUtil.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal v = new BigDecimal(value.trim());
            return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 校验提现金额是否符合倍数；multiple &lt;= 0 表示不限制
     *
     * @return null 表示通过，否则返回错误文案
     */
    public static String checkMultiple(BigDecimal extractPrice, String multipleStr) {
        BigDecimal multiple = parseNonNegative(multipleStr);
        if (multiple.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (extractPrice == null || extractPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return StrUtil.format("提现金额必须是{}的倍数", multiple.stripTrailingZeros().toPlainString());
        }
        // remainder == 0
        if (extractPrice.remainder(multiple).compareTo(BigDecimal.ZERO) != 0) {
            return StrUtil.format("提现金额必须是{}的倍数，如 {}、{}、{}…",
                    multiple.stripTrailingZeros().toPlainString(),
                    multiple.stripTrailingZeros().toPlainString(),
                    multiple.multiply(new BigDecimal("2")).stripTrailingZeros().toPlainString(),
                    multiple.multiply(new BigDecimal("3")).stripTrailingZeros().toPlainString());
        }
        return null;
    }

    /**
     * 规范化提现支持方式，默认三种全开
     */
    public static String normalizeExtractTypes(String config) {
        if (StrUtil.isBlank(config)) {
            return "bank,weixin,alipay";
        }
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String part : config.split(",")) {
            if (StrUtil.isBlank(part)) {
                continue;
            }
            String t = part.trim().toLowerCase();
            if (SysConfigConstants.EXTRACT_TYPE_BANK.equals(t)
                    || SysConfigConstants.EXTRACT_TYPE_WEIXIN.equals(t)
                    || SysConfigConstants.EXTRACT_TYPE_ALIPAY.equals(t)) {
                set.add(t);
            }
        }
        if (set.isEmpty()) {
            return "bank,weixin,alipay";
        }
        return String.join(",", set);
    }

    public static boolean isExtractTypeEnabled(String config, String extractType) {
        if (StrUtil.isBlank(extractType)) {
            return false;
        }
        String normalized = normalizeExtractTypes(config);
        String type = extractType.trim().toLowerCase();
        for (String part : normalized.split(",")) {
            if (type.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }
}
