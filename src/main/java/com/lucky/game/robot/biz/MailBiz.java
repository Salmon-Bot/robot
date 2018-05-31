package com.lucky.game.robot.biz;

import com.lucky.game.core.util.StrRedisUtil;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.mail.MailQQ;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/4/12 13:34
 **/
@Component
@Slf4j
public class MailBiz {

    @Autowired
    private RedisTemplate<String, String> redis;

    @Autowired
    private UserBiz userBiz;

    private static final String BLANCE_NOTIFY_KEY = "blance_notify_key_";

    public void transToEmailNotify(OrderEntity orderEntity,String userId){
        UserEntity userEntity = userBiz.findById(userId);
        transToEmail(orderEntity,userEntity);
    }

    /**
     * 搬砖单交易成功邮件通知
     */
    public void transToEmailNotify(OrderEntity orderEntity, UserEntity userEntity) {
        transToEmail(orderEntity,userEntity);
    }

    private void transToEmail(OrderEntity orderEntity, UserEntity userEntity){
        if (userEntity == null || StringUtils.isEmpty(userEntity.getNotifyEmail())) {
            log.info("email address is empty...");
            return;
        }
        String subject = orderEntity.getMarketType() + " " + orderEntity.getModel() + " model " + orderEntity.getSymbol() + " " + orderEntity.getType() + " success notify";
        String content = orderEntity.getSymbol() + " " + orderEntity.getType() + " success. price is " + orderEntity.getPrice().setScale(8, BigDecimal.ROUND_DOWN)
                + ",amount is " + orderEntity.getAmount().setScale(8, BigDecimal.ROUND_DOWN) + " and totalToUsdt is " + orderEntity.getTotalToUsdt().setScale(8, BigDecimal.ROUND_DOWN) + ".";
        if (orderEntity.getType().equals(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode())) {
            content = content + " and relating buyorderId=" + orderEntity.getBuyOrderId();
        }
        MailQQ.sendEmail(subject, content, userEntity.getNotifyEmail());
    }

    /**
     * 余额不足提醒
     */
    public void balanceToEmailNotify(String userId, String baseQuote, String marketType) {
        UserEntity userEntity = userBiz.findById(userId);
        if (userEntity == null || StringUtils.isEmpty(userEntity.getNotifyEmail())) {
            log.info("email address is empty...");
            return;
        }
        if (StrRedisUtil.get(redis, BLANCE_NOTIFY_KEY + marketType + baseQuote) == null) {
            log.info("发送余额不足提醒,userId={},baseQuote={},marketType={}", userEntity.getOid(), baseQuote, marketType);
            String subject = marketType + " " + baseQuote + " balance not enough";
            String content = marketType + " " + baseQuote + " balance not enough,please deposit.";
            MailQQ.sendEmail(subject, content, userEntity.getNotifyEmail());
            StrRedisUtil.setEx(redis, BLANCE_NOTIFY_KEY + marketType + baseQuote, 7200, baseQuote);
        }
    }
}
