package com.lucky.game.robot.vo.huobi;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author conan
 *         2018/1/5 16:48
 **/
@Data
public class MarketInfoVo implements Serializable {

    private static final long serialVersionUID = 5257839166596610845L;

    /**
     * 请求处理结果
     */
    private String status;

    /**
     * 数据所属的 channel，格式： market.$symbol.kline.$period
     */
    private String ch;

    /**
     * 响应生成时间点，单位：毫秒
     */
    private String ts;

    /**
     * 数据
     */
    private List<MarketDetailVo> data;

//   private List<MarketDetailVo> marketDetailVoList;

}

