package com.bocchi.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.AddressBook;
import com.bocchi.user.mapper.AddressBookMapper;
import com.bocchi.user.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
