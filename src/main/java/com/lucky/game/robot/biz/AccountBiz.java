package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.huobi.response.*;
import com.lucky.game.robot.service.AccountService;
import com.lucky.game.robot.vo.BalanceVo;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.dto.zb.BaseZbDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.huobi.api.ApiClient;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbAccountDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/14 10:54
 **/
@Slf4j
@Component
public class AccountBiz {
    @Autowired
    private AccountService accountService;

    @Autowired
    private ZbApi zbApi;


    /**
     * 获取用户在不同市场主对的余额
     *
     * @return
     */
    public BalanceVo getUserBaseCurrencyBalance(String userId) {
        BalanceVo vo = new BalanceVo();
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setUserId(userId);
        setHuobiApiKey(dto);
        if (StringUtils.isNotEmpty(dto.getApiSecret())) {
            //hb账号余额
            Accounts accounts = this.getHuobiSpotAccounts(dto);
            //标记hb余额是否设置完成
            int hbFinish = 0;
            List<BalanceBean> balanceBeanList = this.getHuobiAccountBalance(dto, accounts);
            for (BalanceBean balanceBean : balanceBeanList) {
                //设置完成
                if (hbFinish >= 6) {
                    break;
                }
                if (DictEnum.HB_MARKET_BASE_USDT.getCode().equals(balanceBean.getCurrency()) && "trade".equals(balanceBean.getType())) {
                    vo.setHbUsdtTradeBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                } else if (DictEnum.HB_MARKET_BASE_USDT.getCode().equals(balanceBean.getCurrency()) && "frozen".equals(balanceBean.getType())) {
                    vo.setHbUsdtFrozenBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                } else if (DictEnum.HB_MARKET_BASE_BTC.getCode().equals(balanceBean.getCurrency()) && "trade".equals(balanceBean.getType())) {
                    vo.setHbBtcTradeBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                } else if (DictEnum.HB_MARKET_BASE_BTC.getCode().equals(balanceBean.getCurrency()) && "frozen".equals(balanceBean.getType())) {
                    vo.setHbBtcFrozenBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                } else if (DictEnum.HB_MARKET_BASE_ETH.getCode().equals(balanceBean.getCurrency()) && "trade".equals(balanceBean.getType())) {
                    vo.setHbEthTradeBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                } else if (DictEnum.HB_MARKET_BASE_ETH.getCode().equals(balanceBean.getCurrency()) && "frozen".equals(balanceBean.getType())) {
                    vo.setHbEthFrozenBalance(new BigDecimal(balanceBean.getBalance()));
                    hbFinish++;
                }
            }
        }
        BaseZbDto baseZbDto = new BaseZbDto();
        this.setZbApiKey(baseZbDto, userId);
        if (StringUtils.isNotEmpty(baseZbDto.getAccessKey())) {
            //标记zb余额是否设置完成
            int zbFinish = 0;
            List<ZbAccountDetailVo> zbAccountDetailVoList = zbApi.getAccountInfo(baseZbDto);
            for (ZbAccountDetailVo zbAccountVo : zbAccountDetailVoList) {
                if (zbFinish >= 3) {
                    break;
                }
                if (DictEnum.ZB_MARKET_BASE_USDT.getCode().equals(zbAccountVo.getKey())) {
                    vo.setZbUsdtTradeBalance(zbAccountVo.getAvailable());
                    vo.setZbUsdtFrozenBalance(zbAccountVo.getFreez());
                    zbFinish++;
                } else if (DictEnum.ZB_MARKET_BASE_BTC.getCode().equals(zbAccountVo.getKey())) {
                    vo.setZbBtcTradeBalance(zbAccountVo.getAvailable());
                    vo.setZbBtcFrozenBalance(zbAccountVo.getFreez());
                    zbFinish++;
                } else if (DictEnum.ZB_MARKET_BASE_QC.getCode().equals(zbAccountVo.getKey())) {
                    vo.setZbQcTradeBalance(zbAccountVo.getAvailable());
                    vo.setZbQcFrozenBalance(zbAccountVo.getFreez());
                    zbFinish++;
                }
            }
        }
        log.info("账号余额,vo={}", vo);
        return vo;
    }


    /**
     * 根据用户id和账号类型获取账号信息
     */
    public AccountEntity getByUserIdAndType(String userId, String type) {
        return accountService.findByUserIdAndType(userId, type);
    }

    public AccountEntity save(AccountEntity entity) {
        return accountService.save(entity);
    }

    public List<AccountEntity> findByType(String type) {
        List<AccountEntity> list = accountService.findByTypeAndStatus(type, DictEnum.USER_STATUS_NORMAL.getCode());
        List<AccountEntity> newList = new ArrayList<>();
        for (AccountEntity accountEntity : list) {
            if (StringUtils.isNotEmpty(accountEntity.getApiKey()) && StringUtils.isNotEmpty(accountEntity.getApiSecret())) {
                if (StringUtils.isEmpty(accountEntity.getAccountId()) && DictEnum.MARKET_TYPE_HB.getCode().equals(type)) {
                    HuobiBaseDto dto = new HuobiBaseDto();
                    dto.setApiKey(accountEntity.getApiKey());
                    dto.setApiSecret(accountEntity.getApiSecret());
                    Accounts accounts = this.getHuobiSpotAccounts(dto);
                    if (accounts != null) {
                        accountEntity.setAccountId(String.valueOf(accounts.getId()));
                        accountService.save(accountEntity);
                    }
                }
                newList.add(accountEntity);
            }
        }
        return newList;
    }

    /**
     * 获取火币账号信息
     */
    public Accounts getHuobiSpotAccounts(HuobiBaseDto dto) {

        setHuobiApiKey(dto);

        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        AccountsResponse<List<Accounts>> accounts = client.accounts();
        List<Accounts> list = accounts.getData();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        log.info("hb 账号不存在,dto={}",dto);
        return null;
    }

    /**
     * 获取火币账号所有余额
     */
    public List<BalanceBean> getHuobiAccountBalance(HuobiBaseDto dto, Accounts accounts) {
        setHuobiApiKey(dto);
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        BalanceResponse<Balance<List<BalanceBean>>> response = client.balance(String.valueOf(accounts.getId()));
        Balance<List<BalanceBean>> balance = response.getData();
        return balance.getList();
    }


    /**
     * 获取hb当前币种最大余额
     */
    public BigDecimal getHuobiQuoteBalance(String userId, String quote) {
        BigDecimal maxBalance = BigDecimal.ZERO;
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setUserId(userId);
        setHuobiApiKey(dto);
        Accounts accounts = this.getHuobiSpotAccounts(dto);
        List<BalanceBean> balanceBeanList = this.getHuobiAccountBalance(dto, accounts);
        for (BalanceBean balanceBean : balanceBeanList) {
            //获取当前quote余额
            if (quote.equals(balanceBean.getCurrency()) && "trade".equals(balanceBean.getType())) {
                maxBalance = new BigDecimal(balanceBean.getBalance());
                break;
            }
        }
        log.info("maxBalance={}", maxBalance);
        return maxBalance;
    }


    /**
     * 获取zb当前币种最大余额
     */
    public BigDecimal getZbBalance(String userId, String quote) {
        BigDecimal maxBalance = BigDecimal.ZERO;
        BaseZbDto baseZbDto = new BaseZbDto();
        this.setZbApiKey(baseZbDto, userId);
        List<ZbAccountDetailVo> zbAccountDetailVoList = zbApi.getAccountInfo(baseZbDto);
        for (ZbAccountDetailVo vo : zbAccountDetailVoList) {
            //获取当前quote余额
            if (quote.equals(vo.getKey())) {
                maxBalance = vo.getAvailable();
                break;
            }
        }
        log.info("maxBalance={}", maxBalance);
        return maxBalance;
    }

    /**
     * 设置火币账号api信息
     */
    public HuobiBaseDto setHuobiApiKey(HuobiBaseDto dto) {
        if (StringUtils.isEmpty(dto.getApiKey())) {
            AccountEntity accountEntity = accountService.findByUserIdAndType(dto.getUserId(), DictEnum.MARKET_TYPE_HB.getCode());
            if (accountEntity != null) {
                dto.setApiKey(accountEntity.getApiKey());
                dto.setApiSecret(accountEntity.getApiSecret());
            }
        }
        return dto;
    }

    public BaseZbDto setZbApiKey(BaseZbDto dto, String userId) {
        if (StringUtils.isEmpty(dto.getAccessKey())) {
            AccountEntity accountEntity = accountService.findByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
            if (accountEntity != null) {
                dto.setAccessKey(accountEntity.getApiKey());
                dto.setSecretKey(accountEntity.getApiSecret());
            }
        }
        return dto;
    }
}

