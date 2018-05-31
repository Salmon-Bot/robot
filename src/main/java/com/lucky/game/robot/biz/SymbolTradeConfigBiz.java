package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.client.SymbolTradeConfigDto;
import com.lucky.game.robot.dto.client.TradeConfigStatusDto;
import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.service.SymbolTradeConfigService;
import com.lucky.game.robot.vo.SymbolTradeConfigVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/22 10:31
 **/
@Component
public class SymbolTradeConfigBiz {


    @Autowired
    private SymbolTradeConfigService symbolTradeConfigService;

    public SymbolTradeConfigEntity findByUserIdAndThresholdType(String userId, String thresholdType) {

        return symbolTradeConfigService.findByUserIdAndThresholdType(userId, thresholdType);
    }

    /**
     * 获取使用中未删除配置
     */
    public SymbolTradeConfigEntity findByUserIdAndThresholdTypeAndMarketType(String userId, String thresholdType, String marketType) {

        SymbolTradeConfigEntity entity = symbolTradeConfigService.findByUserIdAndThresholdTypeAndMarketType(userId, thresholdType, marketType);
        if (entity == null || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()) || DictEnum.STATUS_STOP.getCode().equals(entity.getStatus())) {
            return null;
        }
        return entity;
    }

    public SymbolTradeConfigEntity findById(String id) {
        return symbolTradeConfigService.findById(id);
    }

    public List<SymbolTradeConfigVo> findAllList(String userId) {
        List<SymbolTradeConfigVo> voList = new ArrayList<>();
        List<SymbolTradeConfigEntity> list = symbolTradeConfigService.findByUserId(userId);
        for (SymbolTradeConfigEntity entity : list) {
            if (DictEnum.IS_DELETE_NO.getCode().equals(entity.getIsDelete())) {
                SymbolTradeConfigVo vo = new SymbolTradeConfigVo();
                voList.add(vo);
                BeanUtils.copyProperties(entity, vo);
            }
        }
        return voList;
    }

    public SymbolTradeConfigVo info(String oid) {
        SymbolTradeConfigEntity entity = symbolTradeConfigService.findById(oid);
        SymbolTradeConfigVo vo = new SymbolTradeConfigVo();
        if (entity != null) {
            BeanUtils.copyProperties(entity, vo);
        }
        return vo;
    }

    public void save(SymbolTradeConfigDto dto, String userId) {
        SymbolTradeConfigEntity entity = new SymbolTradeConfigEntity();
        entity.setUserId(userId);
        if (StringUtils.isNotEmpty(dto.getOid())) {
            entity = symbolTradeConfigService.findById(dto.getOid());
        }
        BeanUtils.copyProperties(dto, entity);
        symbolTradeConfigService.save(entity);
    }


    public void delete(String oid) {

        SymbolTradeConfigEntity entity = symbolTradeConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setIsDelete(DictEnum.IS_DELETE_YES.getCode());
        symbolTradeConfigService.save(entity);
    }


    public void updateStatus(TradeConfigStatusDto dto) {
        SymbolTradeConfigEntity entity = symbolTradeConfigService.findById(dto.getOid());
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setStatus(dto.getStatus());
        symbolTradeConfigService.save(entity);
    }
}
