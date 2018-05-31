package com.lucky.game.robot.schedule;

import com.lucky.game.robot.biz.DelteTransBiz;
import com.lucky.game.robot.biz.TransBiz;
import com.lucky.game.robot.biz.HbMarketMonitorBiz;
import com.lucky.game.robot.biz.ShuffleBiz;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.SymBolsDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/1/9 17:58
 **/
@Component
@Slf4j
public class MonitorSchedule {


    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private HbMarketMonitorBiz hbMarketMonitorBiz;

    @Autowired
    private TransBiz transBiz;

    @Autowired
    private DelteTransBiz delteTransBiz;

    @Autowired
    private ShuffleBiz shuffleBiz;

    @Value("${is.schedule:true}")
    private boolean isSchedule;

    /**
     * 检查趋势
     */
    @Value("${is.checkTrend:false}")
    private boolean checkTrend;


    @Value("${is.hb.all.symbol.monitor:true}")
    private boolean isHbAllSymbolMonitor;


    @Value("${hb.one.monitor.count:5}")
    private Integer hbOneMonitorCount;


    @Value("${hb.one.monitor.sleep:2}")
    private Integer hbOneMonitorSleep;


    /**
     * hb所有交易对实时监控
     */
    @Scheduled(cron = "${cron.option[huoBi.symbols]:55 0/3 * * * ?}")
    public void huoBiSymBolsSchedule() {
        if (isSchedule && isHbAllSymbolMonitor) {
            log.info("huobi all symbol monitor start...");
            this.huoBiAllSymBolsMonitor();
            log.info("huobi all symbol monitor end...");
        }
    }

    /**
     * hb限价单监控
     */
    @Scheduled(cron = "${cron.option[hb.trans.model.limit.order]:0 0/4 * * * ?}")
    public void checkHbTransModelLimitOrder() {
        if (isSchedule) {
            log.info("check to trans model limit order start...");
            transBiz.hbTransModelLimitOrder();
            log.info("check to trans model limit order end...");
        }
    }


    /**
     * 检查是否有成交的实时买单可以挂单售出(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[hb.check.order.to.Sale]:0/5 * * * * ?}")
    public void checkHbRealOrderToSale() {
        if (isSchedule) {
            transBiz.hbToSale();
        }
    }

    /**
     * 检查实时卖单是否已完成(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[hb.check.order.hb.sale.finish]:0/30 * * * * ?}")
    public void checkHbRealOrderSaleFinish() {
        if (isSchedule) {
            transBiz.hbCheckSaleFinish();
        }
    }

    /**
     * 异步方法调用不能再同一个类，否则异步注解不起作用
     */
    public void huoBiAllSymBolsMonitor() {
        List<SymBolsDetailVo> list = huobiApi.getSymbolsInfo();
        List<List<SymBolsDetailVo>> allList = averageAssign(list, hbOneMonitorCount);
        for (List<SymBolsDetailVo> subList : allList) {
            try {
                Thread.sleep(hbOneMonitorSleep * 1000);
                hbMarketMonitorBiz.asyncDoMonitor(subList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * zb限价单监控
     */
    @Scheduled(cron = "${cron.option[zb.trans.model.limit.order]:0 0/4 * * * ?}")
    public void checkZbTransModelLimitOrder() {
        if (isSchedule) {
            log.info("zb check to trans model limit order start...");
            transBiz.zbTransModelLimitOrder();
            log.info("zb check to trans model limit order end...");
        }
    }


    /**
     * 检查是否有成交的实时买单可以挂单售出(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[zb.check.order.to.Sale]:0/5 * * * * ?}")
    public void checkZbRealOrderToSale() {
        if (isSchedule) {
            transBiz.zbToSale();
        }
    }

    /**
     * 检查实时卖单是否已完成(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[zb.check.order.hb.sale.finish]:0/30 * * * * ?}")
    public void checkZbRealOrderSaleFinish() {
        if (isSchedule) {
            transBiz.zbCheckSaleFinish();
        }
    }


    /**
     * 检查搬砖单是否已完成(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.shuffle.order.finish]:0 0/3 * * * ?}")
    public void chcekShuffleOrderFinish() {
        if (isSchedule) {
            shuffleBiz.checkHbShuffleOrder();
            shuffleBiz.checkZbShuffleOrder();
        }
    }


    /**
     * 检查hb beta限价单(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.hb.limit.beta.order]:0 0/2 * * * ?}")
    public void chcekHbLimitBetaOrder() {
        if (isSchedule) {
            transBiz.hbCheckLimitBetaOrder();
        }
    }

    /**
     * 检查zb beta限价单(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.zb.limit.beta.order]:30 0/2 * * * ?}")
    public void chcekZbLimitBetaOrder() {
        if (isSchedule) {
            transBiz.zbCheckLimitBetaOrder();
        }
    }

    /**
     * 检查hb gamma限价单(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.hb.limit.gamma.order]:20 0/2 * * * ?}")
    public void chcekHbLimitGammaOrder() {
        if (isSchedule) {
            transBiz.hbCheckLimitGammaOrder();
        }
    }

    /**
     * 检查超时限价单(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.time.out.order]:0 0 0/1 * * ?}")
    public void checkTimeOutOrder() {
        if (isSchedule) {
            log.info("check time out order start...");
            transBiz.checkTimeOutOrder();
            log.info("check time out order end...");
        }
    }

    /**
     * 检查gamma趋势(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.gamma.trend]:28 0/7 * * * ?}")
    public void checkGammaTrend() {
        if (isSchedule && checkTrend) {
            log.info("check gamma trend start...");
            transBiz.checkGammaTrend();
            log.info("check gamma trend end...");
        }
    }

    /**
     * 检查beta趋势(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.beta.trend]:50 0/9 * * * ?}")
    public void checkBetaTrend() {
        if (isSchedule && checkTrend) {
            log.info("check beta trend start...");
            transBiz.checkBetaTrend();
            log.info("check beta trend end...");
        }
    }


    /**
     * 检查delte订单状态(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.delte.order.to.do]:05 0/4 * * * ?}")
    public void checkDelteStatus() {
        if (isSchedule) {
            delteTransBiz.checkDelteStatus();
        }
    }

    /**
     * 检查delte订单是否需要操作(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.delte.order.to.do]:55 0/3 * * * ?}")
    public void checkDelteTodo() {
        if (isSchedule) {
            delteTransBiz.checkDelteTodo();
        }
    }

    /**
     * 检查delte趋势(切日志方法已check开头)
     */
    @Scheduled(cron = "${cron.option[check.delte.ma]:11 0/01 * * * ?}")
    public void checkByMa() {
        if (isSchedule && checkTrend) {
            log.info("check delte ma start...");
            delteTransBiz.checkByMa();
            log.info("check delte ma end...");
        }
    }


    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     */
    private static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        //(先计算出余数)
        int remaider = source.size() % n;
        //然后是商
        int number = source.size() / n;
        //偏移量
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

}
