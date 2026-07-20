package com.zbkj.admin.task.voucher;

import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.service.service.VoucherWarrantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 每日积分强制释放到消费券
 */
@Component("IntegralDailyReleaseTask")
public class IntegralDailyReleaseTask {

    private static final Logger logger = LoggerFactory.getLogger(IntegralDailyReleaseTask.class);

    @Autowired
    private VoucherWarrantService voucherWarrantService;

    /**
     * cron : 0 0 1 * * ?
     */
    public void dailyRelease() {
        logger.info("---IntegralDailyReleaseTask------Execution Time - {}", CrmebDateUtil.nowDateTime());
        try {
            voucherWarrantService.dailyReleaseIntegralToVoucher();
        } catch (Exception e) {
            logger.error("IntegralDailyReleaseTask.dailyRelease | msg : {}", e.getMessage(), e);
        }
    }
}
