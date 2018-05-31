package com.lucky.game.robot;

import com.lucky.game.robot.huobi.api.ApiClient;
import com.lucky.game.robot.huobi.request.WithDrawRequest;
import com.lucky.game.robot.huobi.response.DepositWithdrawResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author conan
 *         2018/4/12 17:19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class HuobiApiTest {

    @Test
    public void withdrawTest() {
        ApiClient client = new ApiClient("3d008259-3cc04933-0a10846b-07ac1", "c46a6b5d-fb92da4b-8ae68067-45c21");
        WithDrawRequest withDrawRequest = new WithDrawRequest();
        withDrawRequest.setCurrency("bts");
        withDrawRequest.setAmount("10");
        withDrawRequest.setAddress("0x171e70136b8a51ff8110236ab7980f8ee4ee0129");
        client.withdraw(withDrawRequest);
    }

    @Test
    public void depositWithdrawTest() {
        ApiClient client = new ApiClient("3d008259-3cc04933-0a10846b-07ac1", "c46a6b5d-fb92da4b-8ae68067-45c21");
        List<DepositWithdrawResponse> response = client.depositWithdraw("bts", "deposit");
        log.info("response={}", response);
    }
}
