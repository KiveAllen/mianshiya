package com.allen.monkeyQuestion.service;

import com.allen.monkeyQuestion.model.dto.question.QuestionQueryRequest;
import com.allen.monkeyQuestion.model.entity.Question;
import com.allen.monkeyQuestion.model.vo.QuestionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 题库题目服务
 */
public interface QuestionService extends IService<Question> {

    /**
     * 添加题目
     *
     * @param question 问题类
     * @return 添加结果
     */
    Long addQuestion(Question question);

    /**
     * 删除题目
     *
     * @param id id
     * @return 删除结果
     */
    Boolean deleteQuestion(Long id);

    /**
     * 更新题目
     *
     * @param question 问题类
     * @return 更新结果
     */
    Boolean updateQuestion(Question question);

    /**
     * 分页查询题目
     *
     * @param questionQueryRequest 查询条件
     * @return 分页结果
     */
    Page<Question> getQuestionPage(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题库题目封装
     *
     * @param id 问题id
     * @return 封装类
     */
    QuestionVO getQuestionVO(Long id);

    /**
     * 分页获取题库题目封装
     *
     * @param questionQueryRequest 查询条件
     * @return 封装类分页
     */
    Page<QuestionVO> getQuestionVOPage(QuestionQueryRequest questionQueryRequest);

    /**
     * 从 ES 查询题目
     *
     * @param questionQueryRequest 查询条件
     * @return 分页结果
     */
    Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest);

    /**
     * 批量删除题目
     *
     * @param questionIdList 题目id列表
     */
    Boolean batchDeleteQuestions(List<Long> questionIdList);

}
