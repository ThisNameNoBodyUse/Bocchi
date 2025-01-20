package com.bocchi.api.client;

import com.bocchi.entity.AddressBook;
import com.bocchi.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("/user/{userId}")
    User getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/addressBook/{addressBookId}")
    AddressBook getAddressBookById(@PathVariable("addressBookId") Long addressBookId);
}
