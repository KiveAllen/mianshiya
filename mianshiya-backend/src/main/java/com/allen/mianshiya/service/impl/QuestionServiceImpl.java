package com.allen.mianshiya.service.impl;

import com.allen.mianshiya.mapper.QuestionMapper;
import com.allen.mianshiya.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.allen.mianshiya.model.entity.Question;

/**
* @author KiveAllen
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-09-27 11:51:57
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}




