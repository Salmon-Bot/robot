package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.entity.LimitDelteConfigEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.service.LimitDelteConfigService;
import com.lucky.game.robot.vo.LimitBetaConfigVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:50
 **/
@Component
public class LimitDelteConfigBiz {

    @Autowired
    private LimitDelteConfigService limitDelteConfigService;

    /**
     * 所有有效的配置
     */
    public List<LimitDelteConfigEntity> findByUserIdAndMarketType(String userId, String marketType) {
        List<LimitDelteConfigEntity> list = limitDelteConfigService.findByUserIdAndMarketType(userId, marketType);
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()));
        return list;
    }

    public LimitDelteConfigEntity save(LimitDelteConfigEntity entity) {
        return limitDelteConfigService.save(entity);
    }


    public LimitBetaConfigVo info(String oid) {
        LimitDelteConfigEntity entity = limitDelteConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        LimitBetaConfigVo vo = new LimitBetaConfigVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public LimitDelteConfigEntity findById(String oid) {
        return limitDelteConfigService.findById(oid);
    }


    public List<LimitDelteConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType) {

        List<LimitDelteConfigEntity> list = limitDelteConfigService.findByUserIdAndSymbolAndMarketType(userId, symbol, marketType);
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()));
        return list;
    }
}
