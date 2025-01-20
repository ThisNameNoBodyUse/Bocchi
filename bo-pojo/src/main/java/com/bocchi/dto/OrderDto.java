package com.bocchi.dto;


import com.bocchi.entity.OrderDetail;
import com.bocchi.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto extends Orders {

    List<OrderDetail> details = new ArrayList<>();
}
