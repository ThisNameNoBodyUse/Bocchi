package com.bocchi.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.bocchi.context.BaseContext;
import com.bocchi.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * MyBatis-plus的公共字段填充操作
 */

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作,自动填充
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]......");
//        LocalDateTime now = LocalDateTime.now().withNano(0);
//        metaObject.setValue("createTime", now);
//        metaObject.setValue("updateTime", now);
        LocalDateTime utcTime = TimeUtil.getNowTime().withNano(0);
        metaObject.setValue("createTime", utcTime);
        metaObject.setValue("updateTime", utcTime);

        Long currentAdminId = BaseContext.getCurrentAdminId();
        Long currentUserId = BaseContext.getCurrentUserId();
        Long userId = currentAdminId != null ? currentAdminId : currentUserId;

        log.info("CurrentId is : {}", userId);

        metaObject.setValue("createUser", userId);
        metaObject.setValue("updateUser", userId);
    }

    /**
     * 修改操作,自动填充
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]......");
//        LocalDateTime now = LocalDateTime.now().withNano(0);
//        metaObject.setValue("updateTime", now);
        LocalDateTime utcTime = TimeUtil.getNowTime().withNano(0);
        metaObject.setValue("updateTime", utcTime);

        Long currentAdminId = BaseContext.getCurrentAdminId();
        Long currentUserId = BaseContext.getCurrentUserId();
        Long userId = currentAdminId != null ? currentAdminId : currentUserId;

        log.info("Id is : {}", userId);

        metaObject.setValue("updateUser", userId);
    }
}
