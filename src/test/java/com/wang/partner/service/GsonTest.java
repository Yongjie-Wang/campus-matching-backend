package com.wang.partner.service;

import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.partner.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class GsonTest {
    @Resource
    private UserService userService;
    @Test
    void gsonTest(){


    }

}
