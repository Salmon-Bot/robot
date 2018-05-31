package com.lucky.game.robot.controller;

import com.lucky.game.core.component.ext.web.BaseController;
import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.robot.biz.SymbolTradeConfigBiz;
import com.lucky.game.robot.dto.client.SymbolTradeConfigDto;
import com.lucky.game.robot.dto.client.TradeConfigStatusDto;
import com.lucky.game.robot.vo.SymbolTradeConfigVo;
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
 *         2018/3/28 13:54
 **/
@RestController
@Api(value = "symbolConfig", description = "实时单配置API")
@RequestMapping(value = "/api/symbolConfig", produces = "application/json;charset=UTF-8")
public class SymbolConfigController extends BaseController {

    @Autowired
    private SymbolTradeConfigBiz symbolTradeConfigBiz;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "列表", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData list() {
        String userId = this.getLoginUser();
        List<SymbolTradeConfigVo> list = symbolTradeConfigBiz.findAllList(userId);
        return ResponseData.success(list);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "明细", notes = "", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "query", dataType = "String")})
    @ResponseBody
    public ResponseData info(String oid) {
        String userId = this.getLoginUser();
        SymbolTradeConfigVo info = symbolTradeConfigBiz.info(oid);
        return ResponseData.success(info);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "保存", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "配置保存", required = true, paramType = "body", dataType = "SymbolTradeConfigDto")})
    @ResponseBody
    public ResponseData save(@RequestBody @Valid SymbolTradeConfigDto dto) {
        String userId = this.getLoginUser();
        symbolTradeConfigBiz.save(dto, userId);
        return ResponseData.success();
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation(value = "删除", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "query", dataType = "String")})
    @ResponseBody
    public ResponseData delete(@RequestBody TradeConfigStatusDto dto) {
        this.getLoginUser();
        symbolTradeConfigBiz.delete(dto.getOid());
        return ResponseData.success();
    }

    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @ApiOperation(value = "停用/启用", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "TradeConfigStatusDto")})
    @ResponseBody
    public ResponseData updateStatus(@RequestBody @Valid TradeConfigStatusDto dto) {
        this.getLoginUser();
        symbolTradeConfigBiz.updateStatus(dto);
        return ResponseData.success();
    }
}

