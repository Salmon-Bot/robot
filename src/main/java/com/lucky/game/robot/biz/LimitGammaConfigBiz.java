package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.client.LimitBetaConfigDto;
import com.lucky.game.robot.dto.client.LimitGammaConfigDto;
import com.lucky.game.robot.entity.LimitGammaConfigEntity;
import com.lucky.game.robot.service.LimitGammaConfigService;
import com.lucky.game.robot.vo.LimitBetaConfigVo;
import com.lucky.game.robot.vo.huobi.LimitGammaConfigVo;
import com.lucky.game.robot.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:50
 **/
@Component
public class LimitGammaConfigBiz {

    @Autowired
    private LimitGammaConfigService limitGammaConfigService;

    /**
     * 所有有效的使用中配置
     */
    public List<LimitGammaConfigEntity> findByUserIdAndMarketTypeInUse(String userId, String marketType) {
        List<LimitGammaConfigEntity> list = limitGammaConfigService.findByUserIdAndMarketType(userId, marketType);
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete())
                || DictEnum.IS_USER_NO.getCode().equals(entity.getIsUse()));
        return list;
    }

    /**
     * 所有有效的配置
     */
    public List<LimitGammaConfigEntity> findByUserIdAndMarketType(String userId, String marketType) {
        List<LimitGammaConfigEntity> list = limitGammaConfigService.findByUserIdAndMarketType(userId, marketType);
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()));
        return list;
    }

    public LimitGammaConfigEntity save(LimitGammaConfigEntity entity) {
        return limitGammaConfigService.save(entity);
    }

    public List<LimitGammaConfigVo> findAllList(String userId) {
        List<LimitGammaConfigEntity> list = limitGammaConfigService.findByUserId(userId);
        List<LimitGammaConfigVo> resultList = new ArrayList<>();
        for (LimitGammaConfigEntity entity : list) {
            LimitGammaConfigVo vo = new LimitGammaConfigVo();
            if (DictEnum.IS_DELETE_NO.getCode().equals(entity.getIsDelete())) {
                BeanUtils.copyProperties(entity, vo);
                resultList.add(vo);
            }
        }
        return resultList;
    }

    public LimitBetaConfigVo info(String oid) {
        LimitGammaConfigEntity entity = limitGammaConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        LimitBetaConfigVo vo = new LimitBetaConfigVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public LimitGammaConfigEntity findById(String oid) {
        return limitGammaConfigService.findById(oid);
    }

    public void save(LimitGammaConfigDto dto, String userId) {
        LimitGammaConfigEntity entity = new LimitGammaConfigEntity();
        entity.setUserId(userId);
        if (StringUtils.isNotEmpty(dto.getOid())) {
            entity = limitGammaConfigService.findById(dto.getOid());
        }
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
    }

    public void delete(String oid) {

        LimitGammaConfigEntity entity = limitGammaConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setIsDelete(DictEnum.IS_DELETE_YES.getCode());
        this.save(entity);
    }

    public void updateStatus(LimitBetaConfigDto dto) {
        LimitGammaConfigEntity entity = limitGammaConfigService.findById(dto.getOid());
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setStatus(dto.getStatus());
        this.save(entity);
    }

    public List<LimitGammaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType) {

        List<LimitGammaConfigEntity> list = limitGammaConfigService.findByUserIdAndSymbolAndMarketType(userId, symbol, marketType);
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()));
        return list;
    }
}
