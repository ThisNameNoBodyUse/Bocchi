package com.bocchi.context;

public class BaseContext {

    private static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> adminThreadLocal = new ThreadLocal<>();

    // 设置当前用户ID
    public static void setCurrentUserId(Long userId) {
        userThreadLocal.set(userId);
    }

    // 获取当前用户ID
    public static Long getCurrentUserId() {
        return userThreadLocal.get();
    }

    // 移除当前用户ID
    public static void removeCurrentUserId() {
        userThreadLocal.remove();
    }

    // 设置当前管理员ID
    public static void setCurrentAdminId(Long adminId) {
        adminThreadLocal.set(adminId);
    }

    // 获取当前管理员ID
    public static Long getCurrentAdminId() {
        return adminThreadLocal.get();
    }

    // 移除当前管理员ID
    public static void removeCurrentAdminId() {
        adminThreadLocal.remove();
    }
}
