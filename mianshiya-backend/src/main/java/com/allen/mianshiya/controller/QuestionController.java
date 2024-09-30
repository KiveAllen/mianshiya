package com.allen.mianshiya.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.allen.mianshiya.annotation.AuthCheck;
import com.allen.mianshiya.common.BaseResponse;
import com.allen.mianshiya.common.DeleteRequest;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.common.ResultUtils;
import com.allen.mianshiya.constant.UserConstant;
import com.allen.mianshiya.exception.BusinessException;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.model.dto.question.QuestionAddRequest;
import com.allen.mianshiya.model.dto.question.QuestionQueryRequest;
import com.allen.mianshiya.model.dto.question.QuestionUpdateRequest;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.vo.QuestionVO;
import com.allen.mianshiya.service.QuestionService;
import com.allen.mianshiya.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目接口
 *
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest 问题创建请求
     * @param request http请求
     * @return 新写入的数据 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        // 检验请求是否存在
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 拷贝属性
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());

        // 调用 service
        return ResultUtils.success(questionService.addQuestion(question));
    }

    /**
     * 删除题目
     *
     * @param deleteRequest 数据删除请求
     * @param request http 请求
     * @return 删除是否成功
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 检验请求是否存在
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionService.deleteQuestion(deleteRequest.getId()));
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        return ResultUtils.success(questionService.updateQuestion(question));
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest 查询条件
     * @param request http 请求
     * @return Page<QuestionVO>
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest 查询条件
     * @return Page<Question>
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    // endregion
}
