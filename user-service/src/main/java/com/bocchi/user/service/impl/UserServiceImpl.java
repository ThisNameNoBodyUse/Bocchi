package com.bocchi.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.User;
import com.bocchi.user.mapper.UserMapper;
import com.bocchi.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
