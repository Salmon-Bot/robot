package com.lucky.game.robot.biz;

import com.lucky.game.robot.entity.KlineInfoEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.service.KlineInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author conan
 *         2018/5/31 14:28
 **/
@Component
public class KlineInfoBiz {

    @Autowired
    private KlineInfoService klineInfoService;


    /**
     * 异步保存行情数据
     */
    @Async("syncKlineInsert")
    public void syncKlineInsert(KlineInfoEntity entity) {
        KlineInfoEntity old = klineInfoService.findByKlineId(entity.getKlineId());
        if (old == null) {
            klineInfoService.insert(entity);
        }
    }

    /**
     * 获取指定行情的k线记录
     */
    public KlineInfoEntity findByKlineId(String klineId) {
        KlineInfoEntity klineInfoEntity = klineInfoService.findByKlineId(klineId);
        if (klineInfoEntity == null) {
            throw new BizException("未找到指定id的k线,klineId=" + klineId);
        }
        return klineInfoEntity;
    }
}
