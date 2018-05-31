package com.lucky.game.robot.controller;

import com.lucky.game.core.component.ext.web.BaseController;
import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.robot.biz.LimitBetaConfigBiz;
import com.lucky.game.robot.dto.client.LimitBetaConfigDto;
import com.lucky.game.robot.vo.LimitBetaConfigVo;
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
@Api(value = "limitBetaConfig", description = "beta限价单配置API")
@RequestMapping(value = "/api/limitBetaConfig", produces = "application/json;charset=UTF-8")
public class LimitBetaConfigController extends BaseController {

    @Autowired
    private LimitBetaConfigBiz limitBetaConfigBiz;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "列表", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData list() {
        String userId = this.getLoginUser();
        List<LimitBetaConfigVo> list = limitBetaConfigBiz.findAllList(userId);
        return ResponseData.success(list);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "明细", notes = "", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "query", dataType = "String")})
    @ResponseBody
    public ResponseData info(String oid) {
        this.getLoginUser();
        LimitBetaConfigVo info = limitBetaConfigBiz.info(oid);
        return ResponseData.success(info);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "保存", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "LimitBetaConfigDto")})
    @ResponseBody
    public ResponseData save(@RequestBody @Valid LimitBetaConfigDto dto) {
        String userId = this.getLoginUser();
        limitBetaConfigBiz.save(dto, userId);
        return ResponseData.success();
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation(value = "删除", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "String", dataType = "String")})
    @ResponseBody
    public ResponseData delete(@RequestBody LimitBetaConfigDto dto) {
        this.getLoginUser();
        limitBetaConfigBiz.delete(dto.getOid());
        return ResponseData.success();
    }

    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @ApiOperation(value = "停用/启用", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "LimitBetaConfigDto")})
    @ResponseBody
    public ResponseData updateStatus(@RequestBody @Valid LimitBetaConfigDto dto) {
        this.getLoginUser();
        limitBetaConfigBiz.updateStatus(dto);
        return ResponseData.success();
    }
}

