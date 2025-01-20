package com.bocchi.user.controller.inner;

import com.bocchi.entity.User;
import com.bocchi.user.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("innerUserController")
@Api(tags = "内部用户接口")
@Slf4j
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    /**
     * 根据用户id查找用户信息
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    User getUserById(@PathVariable("userId") Long userId) {
        return userService.getById(userId);
    }

}
