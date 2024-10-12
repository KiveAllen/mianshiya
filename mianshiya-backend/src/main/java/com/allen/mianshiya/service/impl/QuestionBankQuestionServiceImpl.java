package com.allen.mianshiya.service.impl;

import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.mapper.QuestionBankQuestionMapper;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.QuestionBank;
import com.allen.mianshiya.model.entity.QuestionBankQuestion;
import com.allen.mianshiya.service.QuestionBankQuestionService;
import com.allen.mianshiya.service.QuestionBankService;
import com.allen.mianshiya.service.QuestionService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题库题目服务实现
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    @Lazy
    private QuestionBankService questionBankService;

    /**
     * 根据题库id获取题目列表
     *
     * @param questionBankId 题库id
     * @return 题目列表
     */
    @Override
    public List<Question> getQuestionListByQuestionBankId(Long questionBankId) {
        return questionBankQuestionMapper.getQuestionListByQuestionBankId(questionBankId);
    }

    /**
     * 添加关联
     *
     * @param questionBankId 题库id
     * @param questionId     题目id
     * @return 是否成功
     */
    @Override
    public Boolean addQuestionBankQuestion(Long questionBankId, Long questionId) {
        // 校验
        validQuestionBankQuestion(questionBankId, questionId);
        // 添加关联记录
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        questionBankQuestion.setQuestionBankId(questionBankId);
        questionBankQuestion.setQuestionId(questionId);
        boolean insertSuccess = questionBankQuestionMapper.insert(questionBankQuestion) > 0;
        // 判断是否添加成功
        ThrowUtils.throwIf(!insertSuccess, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 删除关联
     *
     * @param questionBankId 题库id
     * @param questionId     题目id
     * @return 是否成功
     */
    @Override
    public Boolean deleteQuestionBankQuestion(Long questionBankId, Long questionId, Boolean isThrowException) {

        boolean deleteSuccess = questionBankQuestionMapper.delete(
                Wrappers.lambdaQuery(QuestionBankQuestion.class)
                        .eq(questionBankId != null, QuestionBankQuestion::getQuestionBankId, questionBankId)
                        .eq(questionId != null, QuestionBankQuestion::getQuestionId, questionId)
        ) > 0;

        ThrowUtils.throwIf(isThrowException && !deleteSuccess, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 添加题目和题库关联记录
     *
     * @param questionBankId 题库id
     * @param questionId     题目id
     */
    private void validQuestionBankQuestion(Long questionBankId, Long questionId) {
        // 题目和题库必须存在
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
    }


}
