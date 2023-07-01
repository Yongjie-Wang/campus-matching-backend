package com.wang.partner.once;

import com.alibaba.excel.EasyExcel;
import com.wang.partner.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class ExcelReader {
    @Autowired
    public static UserService userService;

    public static void main(String[] args) {
        doReader();
    }

    public static void doReader() {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "C:\\Users\\24017\\Desktop\\my.xlsx";
        List<UserExcel> list = EasyExcel.read(fileName).head(UserExcel.class).sheet().doReadSync();
        for (UserExcel data : list) {
            log.info("读取到数据:{}", data);
        }


    }
}
