package com.lucky.game.robot.controller;

import com.lucky.game.core.component.ext.web.BaseController;
import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.robot.biz.FcoinLimitConfigBiz;
import com.lucky.game.robot.dto.fcoin.FcoinLimitConfigDto;
import com.lucky.game.robot.vo.FcoinLimitConfigVo;
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
@Api(value = "fcoinLimitConfig", description = "fcoin配置API")
@RequestMapping(value = "/api/fcoinLimitConfig", produces = "application/json;charset=UTF-8")
public class FcoinLimitConfigController extends BaseController {

    @Autowired
    private FcoinLimitConfigBiz fcoinLimitConfigBiz;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "列表", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData list() {
        String userId = this.getLoginUser();
        List<FcoinLimitConfigVo> list = fcoinLimitConfigBiz.findAllList(userId);
        return ResponseData.success(list);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "明细", notes = "", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "query", dataType = "String")})
    @ResponseBody
    public ResponseData info(String oid) {
        this.getLoginUser();
        FcoinLimitConfigVo info = fcoinLimitConfigBiz.info(oid);
        return ResponseData.success(info);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "保存", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "FcoinLimitConfigDto")})
    @ResponseBody
    public ResponseData save(@RequestBody @Valid FcoinLimitConfigDto dto) {
        String userId = this.getLoginUser();
        fcoinLimitConfigBiz.save(dto, userId);
        return ResponseData.success();
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation(value = "删除", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "oid", value = "oid", required = true, paramType = "String", dataType = "String")})
    @ResponseBody
    public ResponseData delete(@RequestBody FcoinLimitConfigDto dto) {
        this.getLoginUser();
        fcoinLimitConfigBiz.delete(dto.getOid());
        return ResponseData.success();
    }

    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @ApiOperation(value = "停用/启用", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "dto", required = true, paramType = "body", dataType = "FcoinLimitConfigDto")})
    @ResponseBody
    public ResponseData updateStatus(@RequestBody @Valid FcoinLimitConfigDto dto) {
        this.getLoginUser();
        fcoinLimitConfigBiz.updateStatus(dto);
        return ResponseData.success();
    }
}

