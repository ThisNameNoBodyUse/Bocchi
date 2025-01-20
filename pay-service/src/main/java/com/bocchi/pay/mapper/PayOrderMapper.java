package com.bocchi.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
