package com.allen.mianshiya.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.allen.mianshiya.model.dto.question.QuestionQueryRequest;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.vo.QuestionVO;

/**
 * 题库题目服务
 *
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question 问题类
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题库题目封装
     *
     * @param question 问题类
     * @return 封装类
     */
    QuestionVO getQuestionVO(Question question);

    /**
     * 分页查询题目
     * @param questionQueryRequest 查询条件
     * @return 分页结果
     */
    Page<Question> getQuestionPage(QuestionQueryRequest questionQueryRequest);

    /**
     * 分页获取题库题目封装
     *
     * @param questionQueryRequest 查询条件
     * @return 封装类分页
     */
    Page<QuestionVO> getQuestionVOPage(QuestionQueryRequest questionQueryRequest);

    /**
     * 添加题目
     * @param question 问题类
     * @return 添加结果
     */
    Long addQuestion(Question question);

    /**
     * 更新题目
     * @param question 问题类
     * @return 更新结果
     */
    Boolean updateQuestion(Question question);

    /**
     * 删除题目
     * @param id id
     * @return 删除结果
     */
    Boolean deleteQuestion(Long id);
}
