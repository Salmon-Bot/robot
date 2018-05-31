package com.lucky.game.robot.biz;

import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.core.util.Digests;
import com.lucky.game.core.util.PwdUtil;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.client.ModifyUserInfoDto;
import com.lucky.game.robot.service.UserService;
import com.lucky.game.robot.vo.UserVo;
import com.lucky.game.robot.dto.client.UserRegisterDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.vo.LoginVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/22 10:15
 **/
@Component
public class UserBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountBiz accountBiz;

    /**
     * hb and zb api all set
     *
     * @return
     */
    public List<UserEntity> findAllByNormal() {
        List<UserEntity> userEntityList = userService.findAllByStatus(DictEnum.USER_STATUS_NORMAL.getCode());
        List<UserEntity> newList = new ArrayList<>();
        for (UserEntity userEntity : userEntityList) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(userEntity.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            if (accountEntity != null && StringUtils.isNotEmpty(accountEntity.getApiKey()) && StringUtils.isNotEmpty(accountEntity.getApiSecret())) {
                accountEntity = accountBiz.getByUserIdAndType(userEntity.getOid(), DictEnum.MARKET_TYPE_ZB.getCode());
                if (accountEntity != null && StringUtils.isNotEmpty(accountEntity.getApiKey()) && StringUtils.isNotEmpty(accountEntity.getApiSecret())) {
                    newList.add(userEntity);
                }
                newList.add(userEntity);
            }
        }
        return newList;
    }

    public List<UserEntity> findAllHbByNormal() {
        List<UserEntity> userEntityList = userService.findAllByStatus(DictEnum.USER_STATUS_NORMAL.getCode());
        List<UserEntity> newList = new ArrayList<>();
        for (UserEntity userEntity : userEntityList) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(userEntity.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            if (accountEntity != null && StringUtils.isNotEmpty(accountEntity.getApiKey()) && StringUtils.isNotEmpty(accountEntity.getApiSecret())) {
                newList.add(userEntity);
            }
        }
        return newList;
    }

    public List<UserEntity> findAllZbByNormal() {
        List<UserEntity> userEntityList = userService.findAllByStatus(DictEnum.USER_STATUS_NORMAL.getCode());
        List<UserEntity> newList = new ArrayList<>();
        for (UserEntity userEntity : userEntityList) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(userEntity.getOid(), DictEnum.MARKET_TYPE_ZB.getCode());
            if (accountEntity != null && StringUtils.isNotEmpty(accountEntity.getApiKey()) && StringUtils.isNotEmpty(accountEntity.getApiSecret())) {
                newList.add(userEntity);
            }
        }
        return newList;
    }

    public ResponseData getUserInfo(String userId) {
        UserEntity user = userService.findOne(userId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        AccountEntity hbAccount = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_HB.getCode());
        if (hbAccount != null) {
            userVo.setHbApiKey(hbAccount.getApiKey());
            userVo.setHbApiSecret(hbAccount.getApiSecret());
        }
        AccountEntity zbAccount = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
        if (zbAccount != null) {
            userVo.setZbApiKey(zbAccount.getApiKey());
            userVo.setZbApiSecret(zbAccount.getApiSecret());
        }
        return ResponseData.success(userVo);
    }

    public ResponseData modify(ModifyUserInfoDto dto, String userId) {
        UserEntity user = userService.findOne(userId);
        if(StringUtils.isNotEmpty(dto.getNotifyEmail())){
                user.setNotifyEmail(dto.getNotifyEmail());
        }
        if(StringUtils.isNotEmpty(dto.getNotifyPhone())){
            user.setNotifyPhone(dto.getNotifyPhone());
        }
        if (!StringUtils.isEmpty(dto.getPassword())) {
            user.setSalt(Digests.genSalt());
            user.setPassword(PwdUtil.encryptPassword(dto.getPassword(), user.getSalt()));
        }
        if (StringUtils.isNotEmpty(dto.getHbApiKey()) && StringUtils.isNotEmpty(dto.getHbApiSecret())) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_HB.getCode());
            if (accountEntity == null) {
                accountEntity = new AccountEntity();
                accountEntity.setApiKey(dto.getHbApiKey());
                accountEntity.setApiSecret(dto.getHbApiSecret());
                accountEntity.setStatus(DictEnum.USER_STATUS_NORMAL.getCode());
                accountEntity.setType(DictEnum.MARKET_TYPE_HB.getCode());
                accountEntity.setUserId(userId);
                accountBiz.save(accountEntity);
            }

        }
        if (StringUtils.isNotEmpty(dto.getZbApiKey()) && StringUtils.isNotEmpty(dto.getZbApiSecret())) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
            if (accountEntity == null) {
                accountEntity = new AccountEntity();
                accountEntity.setApiKey(dto.getZbApiKey());
                accountEntity.setApiSecret(dto.getZbApiSecret());
                accountEntity.setStatus(DictEnum.USER_STATUS_NORMAL.getCode());
                accountEntity.setType(DictEnum.MARKET_TYPE_ZB.getCode());
                accountEntity.setUserId(userId);
                accountBiz.save(accountEntity);
            }

        }
        userService.save(user);
        return ResponseData.success();
    }

    public UserEntity findById(String userId) {
        return userService.findOne(userId);
    }

    public LoginVo login(String phone, String password) {
        UserEntity user = userService.findByPhone(phone);
        if (user == null) {
            throw new BizException(ErrorEnum.USER_NOT_FOUND);
        }
        boolean result = PwdUtil.checkPassword(password, user.getPassword(), user.getSalt());
        if (!result) {
            throw new BizException(ErrorEnum.USER_PWD_FAIL);
        }
        LoginVo vo = new LoginVo();
        vo.setUserId(user.getOid());
        vo.setPhone(user.getPhone());

        return vo;
    }

    public UserEntity register(UserRegisterDto dto) {
        UserEntity user = new UserEntity();
        user.setPhone(dto.getPhone());
        user.setSalt(Digests.genSalt());
        user.setPassword(PwdUtil.encryptPassword(dto.getUserPwd(), user.getSalt()));
        user.setStatus(DictEnum.USER_STATUS_NORMAL.getCode());
        return userService.save(user);
    }
}

