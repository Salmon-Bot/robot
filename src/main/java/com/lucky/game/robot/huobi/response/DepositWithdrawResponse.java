package com.lucky.game.robot.huobi.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author conan
 *         2018/4/12 17:50
 **/
@Data
public class DepositWithdrawResponse {

    private String id;

    private String type;

    private String currency;

    @SerializedName("tx-hash")
    private String txHash;

    private long amount;

    private String address;

    @SerializedName("address-tag")
    private String addressTag;

    private String fee;

    private String state;

    @SerializedName("created-at")
    private long createdAt;

    @SerializedName("updated-at")
    private long updatedAt;
}
