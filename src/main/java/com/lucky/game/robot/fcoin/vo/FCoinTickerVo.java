package com.lucky.game.robot.fcoin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * conan
 * 2018/7/3 下午4:44
 **/
@Data
public class FCoinTickerVo {

    /**
     * 行情数据
     *
     "最新成交价",
     "最近一笔成交的成交量",
     "最大买一价",
     "最大买一量",
     "最小卖一价",
     "最小卖一量",
     "24小时前成交价",
     "24小时内最高价",
     "24小时内最低价",
     "24小时内基准货币成交量, 如 btcusdt 中 btc 的量",
     "24小时内计价货币成交量, 如 btcusdt 中 usdt 的量"
     ]
     */
    private List<BigDecimal> ticker;

    /**
     * ticker.btcusdt
     */
    private String type;

    private String seq;
}
