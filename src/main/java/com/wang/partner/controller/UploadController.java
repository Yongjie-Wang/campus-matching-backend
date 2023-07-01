package com.wang.partner.controller;

import com.wang.partner.utils.QiniuOssUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/upload")
@Api(tags = "上传头像")
@Slf4j
public class UploadController {

        @PostMapping("/img")
        public void uploadImg(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
            System.out.println("收到了请求上传单张图片==");
            System.out.println(file);
            if (file.isEmpty()) {
                return;
            }
            String fileName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            QiniuOssUtils utils = new QiniuOssUtils();

            String upload = utils.upload(inputStream, fileName);
            System.out.println(upload);
        }
}
