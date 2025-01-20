package com.bocchi.user.controller.inner;

import com.bocchi.entity.AddressBook;
import com.bocchi.user.service.AddressBookService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "内部用户地址簿接口")
@RestController("innerAddressBookController")
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/{addressBookId}")
    AddressBook getAddressBookById(@PathVariable("addressBookId") Long addressBookId) {
        return addressBookService.getById(addressBookId);
    }




}
