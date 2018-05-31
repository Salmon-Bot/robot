package com.lucky.game.robot.biz;

import com.lucky.game.core.util.StrRedisUtil;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.huobi.BatchCancelDto;
import com.lucky.game.robot.dto.huobi.CreateOrderDto;
import com.lucky.game.robot.dto.zb.ZbCancelOrderDto;
import com.lucky.game.robot.dto.zb.ZbCreateOrderDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.huobi.api.ApiClient;
import com.lucky.game.robot.huobi.api.ApiException;
import com.lucky.game.robot.huobi.request.CreateOrderRequest;
import com.lucky.game.robot.huobi.response.*;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbCreateOrderVo;
import com.lucky.game.robot.zb.vo.ZbResponseVo;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.dto.huobi.IntrustOrderDto;
import com.lucky.game.robot.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author conan
 *         2018/3/14 11:25
 **/
@Slf4j
@Component
public class TradeBiz {

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private ZbApi zbApi;

    @Autowired
    private RedisTemplate<String, String> redis;

    /**
     * create hb order
     */

    public String hbCreateOrder(CreateOrderDto dto) {
        if (StringUtils.isEmpty(dto.getApiKey())) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(dto.getUserId(), DictEnum.MARKET_TYPE_HB.getCode());
            dto.setApiKey(accountEntity.getApiKey());
            dto.setApiSecret(accountEntity.getApiSecret());
            dto.setAccountId(accountEntity.getAccountId());
        }
        CreateOrderRequest createOrderReq = new CreateOrderRequest();
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        createOrderReq.setAccountId(dto.getAccountId());
        createOrderReq.setAmount(dto.getAmount().setScale(4, BigDecimal.ROUND_DOWN).toString());
        createOrderReq.setPrice(dto.getPrice().setScale(4, BigDecimal.ROUND_DOWN).toString());
        if (dto.getSymbol().endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode()) || dto.getSymbol().endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())) {
            createOrderReq.setPrice(dto.getPrice().setScale(8, BigDecimal.ROUND_DOWN).toString());
            createOrderReq.setPrice(dto.getPrice().setScale(8, BigDecimal.ROUND_DOWN).toString());
        }
        createOrderReq.setSymbol(dto.getSymbol());
        createOrderReq.setType(dto.getOrderType());
        createOrderReq.setSource("api");
        Long orderId;
        try {
            orderId = client.createOrder(createOrderReq);
        } catch (ApiException e) {
            log.info("dto={},errMsg={}", dto, e.getMessage());
            Integer scale = 8;
            //order realPrice precision error, scale: `2`
            String[] message = e.getMessage().split("scale:");
            if (message.length >= 2) {
                scale = Integer.valueOf(message[1].replaceAll("`", "").trim());
            }
            if (e.getMessage().contains("price")) {
                createOrderReq.setPrice(dto.getPrice().setScale(scale, BigDecimal.ROUND_DOWN).toString());
            }
            if (e.getMessage().contains("amount")) {
                createOrderReq.setAmount(dto.getAmount().setScale(scale, BigDecimal.ROUND_DOWN).toString());
            }
            orderId = client.createOrder(createOrderReq);
        }
        log.info("hbCreateOrder,dto={},createOrderReq={},orderId={}", dto, createOrderReq, orderId);
        return client.placeOrder(orderId);
    }

    /**
     * hb cancel order
     */
    public void hbCancelOrder(HuobiBaseDto dto) {
        accountBiz.setHuobiApiKey(dto);
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        SubmitcancelResponse response = client.submitcancel(dto.getOrderId());
        if (!"ok".equals(response.getStatus())) {
            log.info("撤销订单失败,orderId={},response={}", dto.getOrderId(), response);
            throw new BizException(response.getErrCode(), response.getErrMsg());
        }
    }

    /**
     * batch cancel order
     */
    public Batchcancel<List, BatchcancelBean> batchCancel(BatchCancelDto dto) {
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        BatchcancelResponse<Batchcancel<List, BatchcancelBean>> response = client.submitcancels(dto.getOrderIds());
        return response.getData();
    }

    /**
     * order detail
     */
    public OrdersDetail getHbOrderDetail(HuobiBaseDto dto) {
        accountBiz.setHuobiApiKey(dto);
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        OrdersDetailResponse<OrdersDetail> response = client.ordersDetail(dto.getOrderId());
        if (!"ok".equals(response.getStatus())) {
            log.info("获取订单详情失败,orderId={},response={}", dto.getOrderId(), response);
            return null;
        }
        return response.getData();
    }


    /**
     * 订单成交明细
     */
    public MatchresultsOrdersDetail hbMatchresults(HuobiBaseDto dto) {
        if (StringUtils.isEmpty(dto.getApiKey())) {
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(dto.getUserId(), DictEnum.MARKET_TYPE_HB.getCode());
            dto.setApiKey(accountEntity.getApiKey());
            dto.setApiSecret(accountEntity.getApiSecret());
        }
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        MatchresultsOrdersDetailResponse<MatchresultsOrdersDetail> response = client.matchresults(dto.getOrderId());
        return response.getData();
    }


    /**
     * 当前委托、历史委托
     */
    public List<IntrustDetail> intrustOrdersDetail(IntrustOrderDto dto) {
        log.info("intrustOrdersDetail,dto={}", dto);
        ApiClient client = new ApiClient(dto.getApiKey(), dto.getApiSecret());
        Map<String, String> map = new HashMap<>();
        map.put("symbol", dto.getSymbol());
        map.put("states", dto.getStates());
        map.put("types", dto.getTypes());
        map.put("direct", dto.getDirect());
        map.put("startDate", dto.getStartDate());
        map.put("endDate", dto.getEndDate());
        map.put("from", dto.getFrom());
        map.put("size", dto.getSize());
        IntrustDetailResponse<List<IntrustDetail>> response = client.intrustOrdersDetail(map);
        return response.getData();
    }

    /**
     * 创建zb订单
     */
    public String zbCreateOrder(String symbol, BigDecimal price, BigDecimal amount, String tradeType, String userId) {
        log.info("zbCreateOrder,symbol={},price={},amount={},tradeType={},userId={}", symbol, price, amount, tradeType, userId);
        AccountEntity account = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
        if (StringUtils.isEmpty(account.getApiKey()) || StringUtils.isEmpty(account.getApiSecret())) {
            throw new BizException(ErrorEnum.USER_API_NOT_FOUND);
        }
        Integer priceScale = Integer.valueOf(StrRedisUtil.get(redis, DictEnum.ZB_CURRENCY_KEY_PRICE.getCode() + symbol));
        Integer amountScale = Integer.valueOf(StrRedisUtil.get(redis, DictEnum.ZB_CURRENCY__KEY_AMOUNT.getCode() + symbol));
        ZbCreateOrderDto dto = new ZbCreateOrderDto();
        dto.setAccessKey(account.getApiKey());
        dto.setSecretKey(account.getApiSecret());
        dto.setCurrency(symbol);
        dto.setPrice(price.setScale(priceScale, BigDecimal.ROUND_HALF_UP).toString());
        dto.setAmount(amount.setScale(amountScale, BigDecimal.ROUND_DOWN).toString());
        dto.setTradeType(tradeType);
        ZbCreateOrderVo vo = zbApi.createOrder(dto);
        if (!"1000".equals(vo.getCode())) {
            log.error("订单创建失败,errno={},errMsg={},dto={},apiKey={},secretKey={}", vo.getCode(), vo.getMessage(), dto, dto.getAccessKey(), dto.getSecretKey());
            throw new BizException(ErrorEnum.CREATE_ORDER_FAIL);
        }
        return vo.getId();
    }


    public ZbResponseVo zbCancelOrder(String orderId, String symbol, String userId) {
        ZbCancelOrderDto dto = new ZbCancelOrderDto();
        dto.setOrderId(orderId);
        dto.setCurrency(symbol);
        AccountEntity accountEntity = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
        dto.setAccessKey(accountEntity.getApiKey());
        dto.setSecretKey(accountEntity.getApiSecret());
        ZbResponseVo vo = zbApi.cancelOrder(dto);
        if (!"1000".equals(vo.getCode())) {
            log.info("撤单失败,vo={}", vo);
            throw new BizException(ErrorEnum.CREATE_ORDER_FAIL);
        }
        return vo;
    }



}
