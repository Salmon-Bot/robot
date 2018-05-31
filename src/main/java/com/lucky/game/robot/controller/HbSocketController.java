package com.lucky.game.robot.controller;

import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.robot.biz.HbSocketBiz;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author conan
 *         2018/5/10 17:33
 **/
@RestController
@Api(value = "hbSocket", description = "hb socket API")
@RequestMapping(value = "/api/hb/socket", produces = "application/json;charset=UTF-8")
public class HbSocketController {

    @Autowired
    HbSocketBiz hbSocketBiz;

    @RequestMapping(value = "/allSymbol", method = RequestMethod.GET)
    @ApiOperation(value = "所有交易对", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData allSymbol() {
        return ResponseData.success( hbSocketBiz.getAllSymbol());
    }



    @RequestMapping(value = "/kLine", method = RequestMethod.POST)
    @ApiOperation(value = "k线", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "List<MarketDetailVo>")})
    @ResponseBody
    public ResponseData kLine(@RequestBody @Valid List<MarketDetailVo> dtoList) {
        hbSocketBiz.reciveKLine(dtoList);
        return ResponseData.success();
    }


}
