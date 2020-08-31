package cn.heshiqian.alipaydemo.controller;

import cn.heshiqian.alipaydemo.model.Order;
import cn.heshiqian.alipaydemo.repo.OrderRepository;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PaymentController {

    @Resource
    OrderRepository orderRepository;

    /**
     * 发起支付接口
     * @param money 金额
     * @param response 输出生成的表单产生跳转
     */
    @PostMapping("/pay/money")
    public void payMoney(@RequestParam int money, HttpServletResponse response){

        //预创建订单
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderState(0);
        order.setOrderTime(LocalDateTime.now());
        order.setOrderPrice(money);
        //保存订单
        orderRepository.save(order);

        //写出生成好的Form表单，用于跳转到沙箱支付界面
        try {
            AlipayTradePagePayResponse pay =
                    Factory.Payment.Page().pay("购买"+money+"元的虚拟物品",order.getOrderNumber(),String.valueOf(money),
                            //这个地方就是同步回调，可以以读配置文件的形式放置
                            "http://localhost:8080/pay/callback/sync");
            //输出HTML
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(pay.body);
            response.getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 同步回调接口
     *
     * !! 理论上，GET同步请求不能作为支付成功的依据，但是目测可以使用订单号反查订单状态来确认支付成功
     * 建议使用异步的回调接口来进行数据库操作
     *
     * !! 另外同步接口貌似不支持签名验证，应该是缺少参数返回
     * 使用官方文档中说明的类{@link Factory.Payment#Common()}，中的{@link com.alipay.easysdk.payment.common.Client#verifyNotify(Map)}方法
     * 传入参数后始终返回False，无解。目测只能用于异步通知验签
     *
     * @param param 参数
     * @param response 输出提示跳转用
     * @throws IOException IO异常
     */
    @GetMapping("/pay/callback/sync")
    public void payCallback(@RequestParam Map<String,String> param, HttpServletResponse response) throws IOException {
        try {
            String out_trade_no = param.get("out_trade_no");//之前创建的订单号，这里会返回一个
            String trade_no = param.get("trade_no");//支付宝创建的订单号
            String total_amount = param.get("total_amount");//总金额
            //使用工具类查询订单支付状态
            AlipayTradeQueryResponse query = Factory.Payment.Common().query(out_trade_no);
            String tradeStatus = query.tradeStatus;
            System.out.println(trade_no+" -> "+ tradeStatus);
            //支付状态 枚举值详见：https://opendocs.alipay.com/apis/api_1/alipay.trade.page.pay#%E8%A7%A6%E5%8F%91%E9%80%9A%E7%9F%A5%E7%B1%BB%E5%9E%8B
            if (tradeStatus.equals("TRADE_SUCCESS")){
                //订单支付成功，查询对应订单号的订单
                Optional<Order> one = orderRepository.findByOrderNumber(out_trade_no);
                if (one.isPresent()){
                    //存在订单
                    Order order1 = one.get();
                    //二次检验金额是否一致
                    if (order1.getOrderPrice() == Double.parseDouble(total_amount)){
                        //修改订单状态，重定向支付成功页面
                        order1.setOrderState(1);
                        orderRepository.save(order1);
                        response.sendRedirect("/paySuccess.html");
                        return;
                    }
                }
            }
            //对于所有支付失败的情况
            doPayFailure(out_trade_no,total_amount,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doPayFailure(String out_trade_no,String amount,HttpServletResponse response) throws Exception {
        //还是使用工具类进行退款操作
        AlipayTradeRefundResponse refund = Factory.Payment.Common().refund(out_trade_no, amount);
        String fundChange = refund.fundChange;
        //操作状态 若金额变化为Y，则代表退款操作成功，其他则否
        //https://opendocs.alipay.com/apis/api_1/alipay.trade.refund#%E5%93%8D%E5%BA%94%E5%8F%82%E6%95%B0
        if (fundChange.equals("Y")){
            System.out.println("已回退此次交易");
        }else {
            System.out.println("交易回退失败");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write("<h1 style='color: green'>交易失败，自动退款失败，请通过订单号联系管理员："+out_trade_no+"</h1>");
            response.getWriter().close();
            return;
        }
        response.sendRedirect("/payFailure.html");
    }

}
