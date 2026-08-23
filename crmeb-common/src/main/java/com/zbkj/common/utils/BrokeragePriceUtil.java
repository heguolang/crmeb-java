package com.zbkj.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.vo.OrderInfoDetailVo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 分销/团队奖计佣基数：售价(或会员价) - 成本价
 */
public final class BrokeragePriceUtil {

    private BrokeragePriceUtil() {
    }

    /**
     * 单件计佣基数 = max(售价 - 成本, 0)
     * 售价优先取会员价，否则取原售价；成本优先取订单快照，其次规格成本，再次商品成本
     */
    public static BigDecimal resolveUnitBase(OrderInfoDetailVo info, StoreProduct product, StoreProductAttrValue attrValue) {
        if (ObjectUtil.isNull(info)) {
            return BigDecimal.ZERO;
        }
        BigDecimal sellPrice = ObjectUtil.isNotNull(info.getVipPrice()) ? info.getVipPrice() : info.getPrice();
        sellPrice = ObjectUtil.defaultIfNull(sellPrice, BigDecimal.ZERO);

        BigDecimal cost = info.getCost();
        if (ObjectUtil.isNull(cost) && ObjectUtil.isNotNull(attrValue)) {
            cost = attrValue.getCost();
        }
        if (ObjectUtil.isNull(cost) && ObjectUtil.isNotNull(product)) {
            cost = product.getCost();
        }
        cost = ObjectUtil.defaultIfNull(cost, BigDecimal.ZERO);

        BigDecimal base = sellPrice.subtract(cost);
        return base.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : base;
    }

    /**
     * 按比例计算本行商品佣金：(售价-成本) × 比例 × 数量
     */
    public static BigDecimal calcLineBrokerage(OrderInfoDetailVo info, StoreProduct product,
                                              StoreProductAttrValue attrValue, BigDecimal rateDecimal) {
        if (ObjectUtil.isNull(rateDecimal) || rateDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal unitBase = resolveUnitBase(info, product, attrValue);
        if (unitBase.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal brokeragePrice = unitBase.multiply(rateDecimal).setScale(2, RoundingMode.DOWN);
        int payNum = ObjectUtil.defaultIfNull(info.getPayNum(), 1);
        if (brokeragePrice.compareTo(BigDecimal.ZERO) > 0 && payNum > 1) {
            brokeragePrice = brokeragePrice.multiply(new BigDecimal(payNum));
        }
        return brokeragePrice;
    }
}
