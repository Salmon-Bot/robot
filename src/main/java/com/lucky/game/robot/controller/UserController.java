package com.lucky.game.robot.controller;

import com.lucky.game.core.component.ext.web.BaseController;
import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.robot.dto.client.UserLoginDto;
import com.lucky.game.robot.vo.BalanceVo;
import com.lucky.game.robot.biz.AccountBiz;
import com.lucky.game.robot.biz.UserBiz;
import com.lucky.game.robot.dto.client.ModifyUserInfoDto;
import com.lucky.game.robot.dto.client.UserRegisterDto;
import com.lucky.game.robot.vo.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author conan
 *         2018/3/28 10:40
 **/
@RestController
@Api(value = "user", description = "用户API")
@RequestMapping(value = "/api/user", produces = "application/json;charset=UTF-8")
public class UserController extends BaseController {

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private AccountBiz accountBiz;

    @RequestMapping(value = "/test/register", method = RequestMethod.POST)
//    @ApiOperation(value = "注册", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "注册参数", required = true, paramType = "body", dataType = "UserRegisterDto")})
    @ResponseBody
    public ResponseData register(@RequestBody UserRegisterDto dto) {
        userBiz.register(dto);
        return ResponseData.success();
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "登录", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "登录参数", required = true, paramType = "body", dataType = "UserLoginDto")})
    @ResponseBody
    public ResponseData login(@RequestBody @Valid UserLoginDto dto) {
        LoginVo vo = userBiz.login(dto.getPhone(), dto.getUserPwd());
        this.setLoginUser(vo.getUserId());
        return ResponseData.success(vo);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "用户基础账号信息", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData info() {
        String userId = this.getLoginUser();
        return userBiz.getUserInfo(userId);
    }

    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    @ApiOperation(value = "修改基础账户信息", notes = "", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "dto", value = "修改参数", required = true, paramType = "body", dataType = "ModifyUserInfoDto")})
    @ResponseBody
    public ResponseData modify(@RequestBody @Valid ModifyUserInfoDto dto) {
        String userId = this.getLoginUser();
        return userBiz.modify(dto, userId);
    }

    @RequestMapping(value = "/getBalance", method = RequestMethod.GET)
    @ApiOperation(value = "账户余额", notes = "", httpMethod = "GET")
    @ResponseBody
    public ResponseData getBalance() {
        String userId = this.getLoginUser();
        BalanceVo vo = accountBiz.getUserBaseCurrencyBalance(userId);
        return ResponseData.success(vo);
    }

}
