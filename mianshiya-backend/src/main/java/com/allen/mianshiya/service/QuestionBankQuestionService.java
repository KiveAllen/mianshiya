package com.allen.mianshiya.service;

import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.QuestionBankQuestion;
import com.allen.mianshiya.model.vo.QuestionBankQuestionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 题库题目服务
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 根据题库id获取题目列表
     * @param questionBankId 题库id
     * @return 题目列表
     */
    List<Question> getQuestionListByQuestionBankId(Long questionBankId);

    /**
     * 添加关联
     * @param questionBankId 题库id
     * @param questionId 题目id
     * @return 是否成功
     */
    Boolean addQuestionBankQuestion(Long questionBankId, Long questionId, Long userId);

    /**
     * 删除关联
     * @param questionBankId 题库id
     * @param questionId 题目id
     * @return 是否成功
     */
    Boolean deleteQuestionBankQuestion(Long questionBankId, Long questionId, Boolean throwEx);

    /**
     * 获取关联
     * @param questionBankId 题库id
     * @param questionId 题目id
     * @return 关联
     */
    List<QuestionBankQuestionVO> getQuestionBankQuestion(Long questionBankId, Long questionId);
}
