package com.bocchi.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String EMAIL_HAD_EXIT = "该邮箱已被注册!";
    public static final String REGISTER_SUCCESS = "注册成功!";
    public static final String REGISTER_FAILED = "注册失败!";
    public static final String CODE_ERROR_OR_TIMEOUT = "验证码错误或超时";
    public static final String SEND_CODE_FAILED = "无法发送验证码!";
    public static final String JWT_EMPTY = "JWT为空";
    public static final String JWT_INVALID = "JWT无效";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ACCOUNT_NOT_ADMIN = "该账号不是超级管理员！";
    public static final String ADMIN_CANNOT_DELETE_ADMIN  = "超级管理员不能删除自己！";
    public static final String ADMIN_CANNOT_UPDATE_ADMIN = "超级管理员不能修改自己！";
    public static final String LOGOUT_SUCCESS = "退出登录成功";
    public static final String SAVE_FAILED = "数据保存失败";
    public static final String INSERT_FAILED = "数据插入失败";
    public static final String NEWEST_NOT_FOUND = "没有找到最新订单";
    public static final String PAY_ORDER_IS_NULL = "支付单为空！";
    public static final String HAD_PAYED = "不能重复支付！";
    public static final String ORDER_TIME_OUT = "支付失败，订单已超时取消！";
    public static final String GET_PAY_LOCK_FAILED = "获取支付锁失败，请稍后重试";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String SAVE_DISH_SUCCESS = "菜品保存成功!";
    public static final String UPDATE_DISH_SUCCESS = "菜品修改成功!";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String ON_SALE_SUCCESS = "起售成功!";
    public static final String BATCH_ON_SALE_SUCCESS = "批量起售成功!";
    public static final String STOP_SALE_SUCCESS = "停售成功!";
    public static final String BATCH_STOP_SALE_SUCCESS = "批量停售成功!";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String EXIT_DISH_ON_SALE = "存在菜品正在售卖中，无法删除!";
    public static final String DISH_DELETE_SUCCESS = "删除菜品成功!";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "该套餐正在售卖中，无法删除!";
    public static final String EXIT_SETMEAL_ON_SALE = "存在套餐正在售卖中，无法删除!";
    public static final String SETMEAL_DELETE_SUCCESS = "删除套餐成功!";
    public static final String BATCH_DELETE_SUCCESS = "批量删除成功!";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";


    public static final String UPLOAD_TOO_BIG = "文件大小只能在2M以内！";
    public static final String UPLOAD_PATTERN_ERROR = "文件只能是jpg/png格式！";

    public static final String CART_FREQUENTLY = "操作太频繁，请稍后再试 ！";
    public static final String CART_ADD_SUCCESS = "数量增加成功";
    public static final String CART_SUB_SUCCESS = "数量减少成功";
    public static final String CART_CLEAR_SUCCESS = "清空购物车成功！";

    public static final String USERNAME_UPDATE_SUCCESS = "用户名修改成功!";
    public static final String SEX_UPDATE_SUCCESS = "性别修改成功!";
    public static final String AVATAR_UPDATE_SUCCESS = "头像修改成功!";

    public static final String INSERT_ADDRESS_SUCCESS = "成功插入地址!";
    public static final String SAVE_SUCCESS = "保存成功!";
    public static final String ADDRESS_DELETE_SUCCESS = "地址删除成功!";
    public static final String DEFAULT_ADDRESS_UPDATE_SUCCESS = "默认值修改成功!";

    public static final String ADMIN_USERNAME = "admin";
    public static final String INIT_PASSWORD = "123456";

}
