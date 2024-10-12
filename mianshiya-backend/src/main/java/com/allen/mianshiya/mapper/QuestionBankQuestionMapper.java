package com.allen.mianshiya.mapper;

import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.QuestionBankQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author KiveAllen
* @description 针对表【question_bank_question(题库题目)】的数据库操作Mapper
* @createDate 2024-09-27 11:51:57
* @Entity com.allen.mianshiya.model.entity.QuestionBankQuestion
*/
public interface QuestionBankQuestionMapper extends BaseMapper<QuestionBankQuestion> {

    List<Question> getQuestionListByQuestionBankId(Long questionBankId);

}




