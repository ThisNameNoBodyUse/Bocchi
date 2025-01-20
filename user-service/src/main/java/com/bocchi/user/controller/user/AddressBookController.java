package com.bocchi.user.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.bocchi.context.BaseContext;
import com.bocchi.entity.AddressBook;
import com.bocchi.result.Result;
import com.bocchi.user.service.AddressBookService;
import com.bocchi.utils.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.bocchi.constant.MessageConstant.*;

@Api(tags = "用户地址簿接口")
@RestController
@Slf4j
@RequestMapping("/user/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @ApiOperation("新增地址")
    @PostMapping("/insert")
    public Result<Object> insert(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentUserId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return Result.success(INSERT_ADDRESS_SUCCESS);
    }

    @ApiOperation("查看地址列表")
    @GetMapping("/list")
    public Result<Object> list() {
        Long userId = BaseContext.getCurrentUserId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<AddressBook>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return Result.success(list);
    }

    @ApiOperation("修改地址")
    @PostMapping("/update")
    public Result<Object> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success(SAVE_SUCCESS);
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id) {
        addressBookService.removeById(id);
        return Result.success(ADDRESS_DELETE_SUCCESS);
    }

    @ApiOperation("修改默认地址")
    @PostMapping("/isDefault")
    public Result<Object> isDefault(@RequestBody AddressBook addressBook) {
        LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        addressBookLambdaUpdateWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentUserId());
        addressBookLambdaUpdateWrapper.set(AddressBook::getIsDefault, 0);
        //先把该用户所有的地址设置为0
        addressBookService.update(addressBookLambdaUpdateWrapper);
        //再把传进来的地址的id的默认值设置为该默认值
        LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper1 = new LambdaUpdateWrapper<>();
        addressBookLambdaUpdateWrapper1.eq(AddressBook::getId, addressBook.getId());
        addressBookLambdaUpdateWrapper1.set(AddressBook::getIsDefault, addressBook.getIsDefault());
        addressBookLambdaUpdateWrapper1.set(AddressBook::getUpdateTime, TimeUtil.getNowTime().withNano(0));
        addressBookService.update(addressBookLambdaUpdateWrapper1);

        return Result.success(DEFAULT_ADDRESS_UPDATE_SUCCESS);
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<Object> defaultAddressBook() {
        Long userId = BaseContext.getCurrentUserId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId).eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return Result.success(addressBook);
    }



}
