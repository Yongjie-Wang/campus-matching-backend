package com.wang.partner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.partner.model.domain.Tag;
import com.wang.partner.service.TagService;
import com.wang.partner.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 24017
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-04-20 09:43:45
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




