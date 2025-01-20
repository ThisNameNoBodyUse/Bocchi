package com.bocchi.manager.controller.admin;


import com.bocchi.constant.MessageConstant;
import com.bocchi.result.Result;
import com.bocchi.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Api(tags = "通用接口")
@RestController("adminCommonController")
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @ApiOperation("文件上传接口")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传 : {}", file);

        try {
            // 检查文件格式
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.equalsIgnoreCase(".jpg") && !extension.equalsIgnoreCase(".png")) {
                return Result.error(MessageConstant.UPLOAD_PATTERN_ERROR);
            }

            // 检查文件大小（10M以内）
            long fileSizeInBytes = file.getSize();
            long maxSizeInBytes = 10 * 1024 * 1024; // 10MB
            if (fileSizeInBytes > maxSizeInBytes) {
                return Result.error(MessageConstant.UPLOAD_TOO_BIG);
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 构造新的文件名称并上传
            String objectName = UUID.randomUUID() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            // 记录结束时间
            long endTime = System.currentTimeMillis();
            log.info("文件上传耗时: {} 毫秒", (endTime - startTime));
            log.info("文件路径 : {}", filePath);

            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败 ： {}", e.getMessage());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
