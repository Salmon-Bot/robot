package com.lucky.game.robot.constant;

/**
 * conan
 * 2017/10/10 16:33
 **/
public enum ErrorEnum {


    SUCCESS("0", "success"),

    USER_REGISTER_PHONE_IS_NULL("1001", "手机号不能为空"),
    USER_REGISTER_PHONE_CHECK_FAIL("1002", "手机号格式不正确"),
    USER_REGISTER_PWD_IS_NULL("1003", "密码不不能为空"),
    USER_REGISTER_PWD_CHECK_FAIL("1004", "密码不符合要求"),
    USER_NOT_FOUND("1005", "账号不存在"),
    USER_PWD_FAIL("1006", "密码错误"),
    USER_NOT_FORBIDDEN("1007", "账号已冻结"),

    MARKEY_INFO_FAIL("1008", "获取市场行情失败"),

    API_KEY_NOT_EMPTY("1009","API秘钥不能为空"),

    ACCOUNT_BALANCE_TOO_LITTLE("1009", "余额不足"),

    RECOLD_NOT_FOUND("1010","记录不存在"),

    SMS_CONTEXT_ERROR("1011","无效的短信内容"),
    SMS_CODE_ERROR("1012","生成验证码失败"),
    SMS_CODE_CHECK_FAIL("1013","无效的验证码"),

    CANCEL_ORDER_FAIL("1014", "撤销订单失败"),
    USER_API_NOT_FOUND("1015", "用户api信息未设置或设置错误"),
    CREATE_ORDER_FAIL("1016", "创建订单失败"),

    SYSTEM_ERROR("-1", "系统异常");

    private String code;

    private String value;

    ErrorEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }


    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
