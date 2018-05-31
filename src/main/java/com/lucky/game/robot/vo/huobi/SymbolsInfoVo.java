package com.lucky.game.robot.vo.huobi;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author conan
 *         2018/3/8 17:55
 **/
@Data
public class SymbolsInfoVo implements Serializable{

    private static final long serialVersionUID = 5257839166596610845L;

    /**
     * 请求处理结果
     */
    private String status;

    /**
     * 数据
     */
    private List<SymBolsDetailVo> data;

}
