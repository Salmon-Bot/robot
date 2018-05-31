package com.lucky.game.robot.dto.huobi;

import lombok.Data;

/**
 * @author conan
 *         2018/3/14 17:16
 **/
@Data
public class IntrustOrderDto extends HuobiBaseDto{

    private String symbol;       //true	string	交易对		btcusdt, bccbtc, rcneth ...
    private String types;       //false	string	查询的订单类型组合，使用','分割		buy-market：市价买, sell-market：市价卖, hbToBuy-limit：限价买, sell-limit：限价卖
    private String startDate;   //false	string	查询开始日期, 日期格式yyyy-mm-dd
    private String endDate;       //false	string	查询结束日期, 日期格式yyyy-mm-dd
    private String states;       //true	string	查询的订单状态组合，使用','分割		pre-submitted 准备提交, submitted 已提交, partial-filled 部分成交,
    // partial-canceled 部分成交撤销, filled 完全成交, canceled 已撤销
    private String from;           //false	string	查询起始 ID
    private String direct;       //false	string	查询方向		prev 向前，next 向后
    private String size;           //false	string	查询记录大小
}
