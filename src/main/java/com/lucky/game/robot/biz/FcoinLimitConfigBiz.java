package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.fcoin.FcoinLimitConfigDto;
import com.lucky.game.robot.entity.FcoinLimitConfigEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.service.FcoinLimitConfigService;
import com.lucky.game.robot.vo.FcoinLimitConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * conan
 * 2018/7/3 下午5:27
 **/
@Component
@Slf4j
public class FcoinLimitConfigBiz {

    @Autowired
    private FcoinLimitConfigService fcoinLimitConfigService;


    public FcoinLimitConfigEntity save(FcoinLimitConfigEntity entity) {
        return fcoinLimitConfigService.save(entity);
    }


    public List<FcoinLimitConfigEntity> findAllInUse() {
        List<FcoinLimitConfigEntity> list = fcoinLimitConfigService.findAll();
        list.removeIf(entity -> DictEnum.STATUS_STOP.getCode().equals(entity.getStatus()) || DictEnum.IS_DELETE_YES.getCode().equals(entity.getIsDelete()));
        return list;
    }

    public List<FcoinLimitConfigVo> findAllList(String userId) {
        List<FcoinLimitConfigEntity> list = fcoinLimitConfigService.findByUserId(userId);
        List<FcoinLimitConfigVo> resultList = new ArrayList<>();
        for (FcoinLimitConfigEntity entity : list) {
            FcoinLimitConfigVo vo = new FcoinLimitConfigVo();
            if (DictEnum.IS_DELETE_NO.getCode().equals(entity.getIsDelete())) {
                BeanUtils.copyProperties(entity, vo);
                resultList.add(vo);
            }
        }
        return resultList;
    }

    public FcoinLimitConfigVo info(String oid) {
        FcoinLimitConfigEntity entity = fcoinLimitConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        FcoinLimitConfigVo vo = new FcoinLimitConfigVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public FcoinLimitConfigEntity findById(String oid) {
        return fcoinLimitConfigService.findById(oid);
    }

    public void save(FcoinLimitConfigDto dto, String userId) {
        FcoinLimitConfigEntity entity = new FcoinLimitConfigEntity();
        if (StringUtils.isNotEmpty(dto.getOid())) {
            entity = fcoinLimitConfigService.findById(dto.getOid());
        }else{
            entity.setUserId(userId);
            entity.setMarketType(DictEnum.MARKET_TYPE_FCOIN.getCode());
        }
        BeanUtils.copyProperties(dto,entity);
        this.save(entity);
    }


    public void delete(String oid) {

        FcoinLimitConfigEntity entity = fcoinLimitConfigService.findById(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setIsDelete(DictEnum.IS_DELETE_YES.getCode());
        this.save(entity);
    }


    public void updateStatus(FcoinLimitConfigDto dto) {
        FcoinLimitConfigEntity entity = fcoinLimitConfigService.findById(dto.getOid());
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        entity.setStatus(dto.getStatus());
        this.save(entity);
    }

}
