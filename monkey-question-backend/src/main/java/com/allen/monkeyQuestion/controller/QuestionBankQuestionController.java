package com.allen.monkeyQuestion.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.allen.monkeyQuestion.common.BaseResponse;
import com.allen.monkeyQuestion.common.ErrorCode;
import com.allen.monkeyQuestion.common.ResultUtils;
import com.allen.monkeyQuestion.constant.UserConstant;
import com.allen.monkeyQuestion.exception.ThrowUtils;
import com.allen.monkeyQuestion.model.dto.questionBankQuestion.*;
import com.allen.monkeyQuestion.model.entity.User;
import com.allen.monkeyQuestion.model.vo.QuestionBankQuestionVO;
import com.allen.monkeyQuestion.service.QuestionBankQuestionService;
import com.allen.monkeyQuestion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目联系接口
 */
@RestController
@RequestMapping("/questionBankQuestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    /**
     * 添加题库题目联系
     *
     * @param addRequest 添加题库题目联系
     * @return Boolean
     */
    @PostMapping("/add")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addQuestionBankQuestion(@RequestBody QuestionBankQuestionAddRequest addRequest,
                                                         HttpServletRequest request) {
        // 校验
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = addRequest.getQuestionBankId();
        Long questionId = addRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionId == null, ErrorCode.PARAMS_ERROR);

        // 用户
        User loginUser = userService.getLoginUser(request);

        // 调用服务
        return ResultUtils.success(questionBankQuestionService.addQuestionBankQuestion(
                addRequest.getQuestionBankId(), addRequest.getQuestionId(), loginUser.getId()
        ));
    }

    /**
     * 删除题库题目联系
     *
     * @param deleteRequest 删除题库题目联系请求
     * @return Boolean
     */
    @DeleteMapping("/delete")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBankQuestion(@RequestBody QuestionBankQuestionDeleteRequest deleteRequest) {
        // 校验
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = deleteRequest.getQuestionBankId();
        Long questionId = deleteRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null || questionId == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(questionBankQuestionService
                .deleteQuestionBankQuestion(questionBankId, questionId, true));
    }

    /**
     * 获取题库题目联系
     *
     * @param getRequest 获取题库题目联系请求
     * @return Boolean
     */
    @GetMapping("/list")
    public BaseResponse<List<QuestionBankQuestionVO>> getQuestionBankQuestion(QuestionBankQuestionGetRequest getRequest) {
        // 校验
        ThrowUtils.throwIf(getRequest == null, ErrorCode.PARAMS_ERROR);

        Long questionBankId = getRequest.getQuestionBankId();
        Long questionId = getRequest.getQuestionId();
        ThrowUtils.throwIf(questionBankId == null && questionId == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(questionBankQuestionService
                .getQuestionBankQuestion(questionBankId, questionId));
    }

    /**
     * 批量添加题库题目联系
     */
    @PostMapping("/add/batch")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchAddQuestionsToBank(@RequestBody QuestionBankQuestionBatchAddRequest batchAddRequest,
                                                         HttpServletRequest request) {
        // 校验
        ThrowUtils.throwIf(batchAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 用户
        User loginUser = userService.getLoginUser(request);

        // 调用服务
        questionBankQuestionService.batchAddQuestionsToBank(
                batchAddRequest.getQuestionIdList(),
                batchAddRequest.getQuestionBankId(),
                loginUser
        );

        return ResultUtils.success(true);
    }

    /**
     * 批量删除题库题目联系
     */
    @PostMapping("/remove/batch")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchRemoveQuestionsFromBank(@RequestBody QuestionBankQuestionBatchRemoveRequest batchDeleteRequest) {
        // 校验
        ThrowUtils.throwIf(batchDeleteRequest == null, ErrorCode.PARAMS_ERROR);

        // 调用服务
        questionBankQuestionService.batchRemoveQuestionsFromBank(
                batchDeleteRequest.getQuestionIdList(),
                batchDeleteRequest.getQuestionBankId()
        );

        return ResultUtils.success(true);
    }
}
