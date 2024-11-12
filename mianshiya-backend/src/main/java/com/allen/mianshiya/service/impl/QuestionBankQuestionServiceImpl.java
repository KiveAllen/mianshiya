package com.allen.mianshiya.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.exception.BusinessException;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.mapper.QuestionBankQuestionMapper;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.QuestionBank;
import com.allen.mianshiya.model.entity.QuestionBankQuestion;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.vo.QuestionBankQuestionVO;
import com.allen.mianshiya.service.QuestionBankQuestionService;
import com.allen.mianshiya.service.QuestionBankService;
import com.allen.mianshiya.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public Boolean addQuestionBankQuestion(Long questionBankId, Long questionId, Long userId) {
        // 校验
        validQuestionBankQuestion(questionBankId, questionId);

        // 判断关联记录是否存在
        boolean exist = questionBankQuestionMapper.exists(
                Wrappers.lambdaQuery(QuestionBankQuestion.class)
                        .eq(questionBankId != null, QuestionBankQuestion::getQuestionBankId, questionBankId)
                        .eq(questionId != null, QuestionBankQuestion::getQuestionId, questionId)
        );
        ThrowUtils.throwIf(exist, ErrorCode.OPERATION_ERROR, "关联记录已存在");

        // 添加关联记录
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        questionBankQuestion.setQuestionBankId(questionBankId);
        questionBankQuestion.setQuestionId(questionId);
        questionBankQuestion.setUserId(userId);
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
    public Boolean deleteQuestionBankQuestion(Long questionBankId, Long questionId, Boolean throwEx) {

        boolean deleteSuccess = questionBankQuestionMapper.delete(
                Wrappers.lambdaQuery(QuestionBankQuestion.class)
                        .eq(questionBankId != null, QuestionBankQuestion::getQuestionBankId, questionBankId)
                        .eq(questionId != null, QuestionBankQuestion::getQuestionId, questionId)
        ) > 0;

        ThrowUtils.throwIf(throwEx && !deleteSuccess, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 获取关联
     *
     * @param questionBankId 题库id
     * @param questionId     题目id
     * @return 关联
     */
    @Override
    public List<QuestionBankQuestionVO> getQuestionBankQuestion(Long questionBankId, Long questionId) {
        List<QuestionBankQuestion> questionList = this.list(new LambdaQueryWrapper<QuestionBankQuestion>()
                .eq(questionBankId != null, QuestionBankQuestion::getQuestionBankId, questionBankId)
                .eq(questionId != null, QuestionBankQuestion::getQuestionId, questionId)
        );

        return questionList.stream().map(QuestionBankQuestionVO::objToVo).collect(Collectors.toList());
    }

    /**
     * 校验题目和题库
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 检查题目 id 是否存在
        List<Question> questionList = questionService.listByIds(questionIdList);

        // 合法的题目 id
        List<Long> validQuestionIdList = questionList.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "合法的题目列表为空");

        // 检查题库 id 是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");

        // 执行插入
        List<QuestionBankQuestion> questionBankQuestionList = new ArrayList<>();
        for (Long questionId : validQuestionIdList) {
            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
            questionBankQuestion.setQuestionBankId(questionBankId);
            questionBankQuestion.setQuestionId(questionId);
            questionBankQuestion.setUserId(loginUser.getId());
            questionBankQuestionList.add(questionBankQuestion);
        }

        boolean result = this.saveBatch(questionBankQuestionList);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库非法");

        // 构造批量删除的条件
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .in(QuestionBankQuestion::getQuestionId, questionIdList)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);

        // 执行批量删除
        boolean result = this.remove(lambdaQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "从题库移除题目失败");
        }
    }

}
