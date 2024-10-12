package com.allen.mianshiya.controller;

import com.allen.mianshiya.annotation.AuthCheck;
import com.allen.mianshiya.common.BaseResponse;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.common.ResultUtils;
import com.allen.mianshiya.constant.UserConstant;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.model.dto.questionBankQuestion.QuestionBankQuestionAddRequest;
import com.allen.mianshiya.model.dto.questionBankQuestion.QuestionBankQuestionDeleteRequest;
import com.allen.mianshiya.service.QuestionBankQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 题库题目接口
 */
@RestController
@RequestMapping("/questionBankQuestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    /**
     * 添加题目
     *
     * @param addRequest 添加题目
     * @return Boolean
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addQuestionBankQuestion(QuestionBankQuestionAddRequest addRequest) {
        // 校验
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = addRequest.getQuestionBankId();
        Long questionId = addRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionId == null, ErrorCode.PARAMS_ERROR);
        // 调用服务
        return ResultUtils.success(questionBankQuestionService.addQuestionBankQuestion(
                addRequest.getQuestionBankId(), addRequest.getQuestionId()
        ));
    }

    /**
     * 删除题目
     *
     * @param deleteRequest 删除题目请求
     * @return Boolean
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBankQuestion(@RequestParam QuestionBankQuestionDeleteRequest deleteRequest) {
        // 校验
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = deleteRequest.getQuestionBankId();
        Long questionId = deleteRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionId == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(questionBankQuestionService
                .deleteQuestionBankQuestion(questionBankId, questionId, true));
    }

}
