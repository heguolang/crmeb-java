-- 模拟订单229同款场景：买家3471 → 翟敏3347(V4) → 简孟辉3308(V4)，验证团队极差/平级
-- 说明：临时把黄兴3003团等级置0以还原当时链路，脚本结束会恢复
SET NAMES utf8mb4;
SET @sim_order_no = CONCAT('orderSIM', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
SET @buyer_uid = 3471;
SET @product_id = 13;
SET @attr_value_id = 1319;
SET @pay_price = 398.00;
SET @huang_uid = 3003;

SELECT team_level INTO @huang_team_level_bak FROM eb_user WHERE uid = @huang_uid;
UPDATE eb_user SET team_level = 0, update_time = NOW() WHERE uid = @huang_uid;

INSERT INTO eb_store_order (
  order_id, uid, real_name, user_phone, user_address,
  freight_price, total_num, total_price, total_postage, pay_price, pay_postage,
  deduction_price, coupon_id, coupon_price, paid, pay_time, pay_type,
  status, refund_status, refund_price, gain_integral, use_integral, back_integral,
  mark, is_del, mer_id, is_mer_check, combination_id, pink_id, cost,
  seckill_id, bargain_id, verify_code, store_id, shipping_type, clerk_id,
  is_channel, is_remind, is_system_del, bargain_user_id, type,
  pro_total_price, before_pay_price, is_alter_price, create_time, update_time
) VALUES (
  @sim_order_no, @buyer_uid, '模拟唐小', '13800000000', '模拟地址-团队奖验证',
  0.00, 1, @pay_price, 0.00, @pay_price, 0.00,
  0.00, 0, 0.00, 1, NOW(), 'yue',
  0, 0, 0.00, 796, 0, 0,
  '本地模拟：验证翟敏/简孟辉团队奖平级奖', 0, 0, 0, 0, 0, 0.00,
  0, 0, '', 0, 1, 0,
  0, 0, 0, 0, 0,
  @pay_price, 0.00, 0, NOW(), NOW()
);

SET @oid = LAST_INSERT_ID();

INSERT INTO eb_store_order_info (
  order_id, product_id, info, `unique`, order_no, product_name, attr_value_id,
  image, sku, price, pay_num, weight, volume, give_integral, is_reply, is_sub,
  vip_price, product_type, create_time, update_time
) VALUES (
  @oid, @product_id,
  CONCAT('{"image":"","cost":0.00,"productId":', @product_id, ',"attrValueId":', @attr_value_id,
         ',"weight":0.00,"giveIntegral":796,"isSub":false,"productName":"明眸清润护眼液","volume":0.00,"payNum":1,"price":',
         @pay_price, ',"vipPrice":', @pay_price, ',"tempId":3,"sku":"默认","productType":0}'),
  MD5(CONCAT(@sim_order_no, @attr_value_id)),
  @sim_order_no, '明眸清润护眼液', @attr_value_id,
  '', '默认', @pay_price, 1, 0.00, 0.00, 796, 0, 0,
  @pay_price, 0, NOW(), NOW()
);

INSERT INTO eb_store_order_status (oid, change_type, change_message, create_time)
VALUES (@oid, 'pay_success', '本地模拟支付成功', NOW());

-- ========== 按 TeamBrokerageServiceImpl 逻辑发团队奖 ==========
DROP TEMPORARY TABLE IF EXISTS tmp_team_awards;
CREATE TEMPORARY TABLE tmp_team_awards (
  uid INT NOT NULL,
  title VARCHAR(64) NOT NULL,
  price DECIMAL(8,2) NOT NULL,
  brokerage_level INT NOT NULL,
  mark VARCHAR(512) NOT NULL
);

SET @acc_rate = 0;
SET @cur_uid = (SELECT spread_uid FROM eb_user WHERE uid = @buyer_uid);

-- 逐级向上（最多20层，足够）
SET @i = 0;
WHILE @i < 20 AND @cur_uid IS NOT NULL AND @cur_uid > 0 DO
  SET @team_level_id = (SELECT IFNULL(team_level, 0) FROM eb_user WHERE uid = @cur_uid);
  SET @next_uid = (SELECT spread_uid FROM eb_user WHERE uid = @cur_uid);

  IF @team_level_id > 0 THEN
    SET @my_rate = (
      SELECT IFNULL(team_brokerage_rate, 0) FROM eb_system_team_level_config
      WHERE team_level_id = @team_level_id AND is_del = 0 LIMIT 1
    );
    SET @peer_rate = (
      SELECT IFNULL(peer_award_rate, 0) FROM eb_system_team_level_config
      WHERE team_level_id = @team_level_id AND is_del = 0 LIMIT 1
    );
    SET @level_name = (
      SELECT IFNULL(name, '') FROM eb_system_team_level WHERE id = @team_level_id LIMIT 1
    );
    SET @is_team_product = (
      SELECT IFNULL(is_team_brokerage, 1) FROM eb_store_product WHERE id = @product_id
    );

    IF @is_team_product = 1 AND @my_rate IS NOT NULL THEN
      IF @my_rate > @acc_rate THEN
        SET @diff = @my_rate - @acc_rate;
        SET @award = FLOOR((@pay_price - 0) * (@diff / 100) * 100) / 100;
        IF @award > 0 THEN
          INSERT INTO tmp_team_awards VALUES (
            @cur_uid, '获得团队极差奖', @award, 10,
            CONCAT('获得团队极差奖，团等级【', @level_name, '】极差', TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM CAST(@diff AS CHAR))), '%，分佣', @award)
          );
        END IF;
        SET @acc_rate = @my_rate;
      ELSEIF @my_rate = @acc_rate AND @peer_rate > 0 THEN
        SET @award = FLOOR((@pay_price - 0) * (@peer_rate / 100) * 100) / 100;
        IF @award > 0 THEN
          INSERT INTO tmp_team_awards VALUES (
            @cur_uid, '获得团队平级奖', @award, 11,
            CONCAT('获得团队平级奖，团等级【', @level_name, '】平级奖', @peer_rate, '%，分佣', @award)
          );
        END IF;
      END IF;
    END IF;
  END IF;

  SET @cur_uid = @next_uid;
  SET @i = @i + 1;
END WHILE;

-- 一级推广佣金（联创30%）
SET @l1_uid = (SELECT spread_uid FROM eb_user WHERE uid = @buyer_uid);
SET @l1_level = (SELECT `level` FROM eb_user WHERE uid = @l1_uid);
SET @l1_rate = (
  SELECT IFNULL(brokerage_rate_one, 0) FROM eb_system_user_level_brokerage
  WHERE level_id = @l1_level AND is_del = 0 LIMIT 1
);
SET @l1_name = (SELECT IFNULL(name, '') FROM eb_system_user_level WHERE id = @l1_level LIMIT 1);
SET @l1_award = FLOOR((@pay_price - 0) * (@l1_rate / 100) * 100) / 100;

-- 写入佣金并加余额
INSERT INTO eb_user_brokerage_record (
  uid, link_id, link_type, type, title, price, balance, mark, status,
  frozen_time, thaw_time, create_time, update_time, brokerage_level
)
SELECT
  a.uid, @sim_order_no, 'order', 1, a.title, a.price,
  IFNULL(u.brokerage_price, 0) + a.price,
  a.mark, 3, 0, 0, NOW(), NOW(), a.brokerage_level
FROM tmp_team_awards a
JOIN eb_user u ON u.uid = a.uid;

UPDATE eb_user u
JOIN tmp_team_awards a ON a.uid = u.uid
SET u.brokerage_price = IFNULL(u.brokerage_price, 0) + a.price, u.update_time = NOW();

INSERT INTO eb_user_brokerage_record (
  uid, link_id, link_type, type, title, price, balance, mark, status,
  frozen_time, thaw_time, create_time, update_time, brokerage_level
)
SELECT
  @l1_uid, @sim_order_no, 'order', 1, '获得推广佣金', @l1_award,
  IFNULL(u.brokerage_price, 0) + @l1_award,
  CONCAT('获得推广佣金，您的等级', @l1_name, '，1级返佣', @l1_rate, '%，分佣', @l1_award),
  3, 0, 0, NOW(), NOW(), 1
FROM eb_user u WHERE u.uid = @l1_uid;

UPDATE eb_user SET brokerage_price = IFNULL(brokerage_price, 0) + @l1_award, update_time = NOW()
WHERE uid = @l1_uid;

-- 恢复黄兴团等级
UPDATE eb_user SET team_level = @huang_team_level_bak, update_time = NOW() WHERE uid = @huang_uid;

SELECT 'SIM_ORDER' AS tag, @sim_order_no AS order_no, @oid AS oid;
SELECT 'TEAM_EXPECTED' AS tag, uid, title, price, brokerage_level, mark FROM tmp_team_awards;
SELECT 'L1_EXPECTED' AS tag, @l1_uid AS uid, @l1_award AS price, @l1_rate AS rate;
SELECT 'RESULT' AS tag, id, uid, title, price, status, brokerage_level, mark, create_time
FROM eb_user_brokerage_record WHERE link_id = @sim_order_no ORDER BY id;
