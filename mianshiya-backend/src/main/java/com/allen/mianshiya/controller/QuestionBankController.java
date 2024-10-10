package com.allen.mianshiya.controller;

import com.allen.mianshiya.annotation.AuthCheck;
import com.allen.mianshiya.common.BaseResponse;
import com.allen.mianshiya.common.DeleteRequest;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.common.ResultUtils;
import com.allen.mianshiya.constant.UserConstant;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.model.dto.questionBank.QuestionBankAddRequest;
import com.allen.mianshiya.model.dto.questionBank.QuestionBankQueryRequest;
import com.allen.mianshiya.model.dto.questionBank.QuestionBankUpdateRequest;
import com.allen.mianshiya.model.entity.QuestionBank;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.vo.QuestionBankVO;
import com.allen.mianshiya.service.QuestionBankService;
import com.allen.mianshiya.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题库接口
 */
@RestController
@RequestMapping("/questionBank")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private UserService userService;

    // region 增删改查 (管理员)

    /**
     * 创建题库（仅管理员可用）
     *
     * @param questionBankAddRequest 创建题库参数
     * @param request                http请求
     * @return 创建成功
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest questionBankAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankAddRequest == null, ErrorCode.PARAMS_ERROR);

        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankAddRequest, questionBank);

        User loginUser = userService.getLoginUser(request);
        questionBank.setUserId(loginUser.getId());

        return ResultUtils.success(questionBankService.addQuestionBank(questionBank));
    }

    /**
     * 删除题库（仅管理员可用）
     *
     * @param deleteRequest 删除题库的id
     * @return 删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionBankService.deleteQuestionBank(deleteRequest.getId()));
    }

    /**
     * 更新题库（仅管理员可用）
     *
     * @param questionBankUpdateRequest 更新题库参数
     * @return 更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody QuestionBankUpdateRequest questionBankUpdateRequest) {
        ThrowUtils.throwIf(questionBankUpdateRequest == null
                           || questionBankUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankUpdateRequest, questionBank);

        return ResultUtils.success(questionBankService.updateQuestionBank(questionBank));
    }

    /**
     * 分页获取题库题目列表（仅管理员可用）
     *
     * @param questionBankQueryRequest 查询参数
     * @return 分页列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest) {
        ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionBankService.listQuestionBankByPage(questionBankQueryRequest));
    }

    /**
     * 根据 id 获取题库
     *
     * @param id 题库id
     * @return 封装类
     */
    @GetMapping("/get")
    public BaseResponse<QuestionBank> getQuestionBankById(@RequestParam Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBank(id));
    }

    // endregion

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param id 题库id
     * @return 封装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankVO> getQuestionBankVOById(@RequestParam Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBankVO(id));
    }

    /**
     * 分页获取题库列表（封装类）
     *
     * @param questionBankQueryRequest 查询参数
     * @return 分页列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest) {
        long size = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBankVOPage(questionBankQueryRequest));
    }
}
