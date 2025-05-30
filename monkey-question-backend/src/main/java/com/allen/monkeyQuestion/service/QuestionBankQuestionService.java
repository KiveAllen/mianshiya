package com.allen.monkeyQuestion.service;

import com.allen.monkeyQuestion.model.entity.Question;
import com.allen.monkeyQuestion.model.entity.QuestionBankQuestion;
import com.allen.monkeyQuestion.model.entity.User;
import com.allen.monkeyQuestion.model.vo.QuestionBankQuestionVO;
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

    /**
     * 批量添加题目到题库
     *
     * @param questionIdList 题目id列表
     * @param questionBankId 题库id
     * @param loginUser      登录用户
     */
    void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser);

    /**
     * 批量添加题目到题库
     *
     * @param questionBankQuestions 题库题目
     */
    void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions);



    /**
     * 批量移除题目
     *
     * @param questionIdList 题目id列表
     * @param questionBankId 题库id
     */
    void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId);
}
