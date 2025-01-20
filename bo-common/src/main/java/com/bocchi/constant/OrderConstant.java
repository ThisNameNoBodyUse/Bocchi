package com.bocchi.constant;

/**
 * 订单状态常量类
 */
public class OrderConstant {
    public final static int WAIT_FOR_PAY = 1; // 待支付
    public final static int WAIT_FOR_SEND = 2; // 待派送
    public final static int HAD_SEND = 3; // 派送中
    public final static int COMPILE = 4; // 已完成
    public final static int CANCEL = 5; // 已取消
}
