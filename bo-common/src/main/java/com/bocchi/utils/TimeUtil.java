package com.bocchi.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeUtil {
    public static LocalDateTime getNowTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime utcTime = now.toLocalDateTime();
        return utcTime;
    }
}
