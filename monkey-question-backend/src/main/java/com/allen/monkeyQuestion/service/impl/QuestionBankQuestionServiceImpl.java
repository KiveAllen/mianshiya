package com.allen.monkeyQuestion.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.allen.monkeyQuestion.common.ErrorCode;
import com.allen.monkeyQuestion.exception.BusinessException;
import com.allen.monkeyQuestion.exception.ThrowUtils;
import com.allen.monkeyQuestion.mapper.QuestionBankQuestionMapper;
import com.allen.monkeyQuestion.model.entity.Question;
import com.allen.monkeyQuestion.model.entity.QuestionBank;
import com.allen.monkeyQuestion.model.entity.QuestionBankQuestion;
import com.allen.monkeyQuestion.model.entity.User;
import com.allen.monkeyQuestion.model.vo.QuestionBankQuestionVO;
import com.allen.monkeyQuestion.service.QuestionBankQuestionService;
import com.allen.monkeyQuestion.service.QuestionBankService;
import com.allen.monkeyQuestion.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIdList);

        // 合法的题目 id
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper, obj -> (Long) obj);
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "合法的题目列表为空");


        // 检查题库 id 是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");

        // 检查哪些题目不存在题库中避免插入
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);

        // 已经存在题库中的题目id
        List<Long> existQuestionIdList = existQuestionList.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .toList();

        // 移除已经存在的题目id
        validQuestionIdList.removeAll(existQuestionIdList);

        // 若是为空，则所有题库都存在于题库中
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "所有题目都存在于题库中");

        // 自定义线程池
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                20,                              // 核心线程数
                50,                                          // 最大线程数
                60L,                                         // 线程空闲存活时间
                TimeUnit.SECONDS,                            // 存活时间单位
                new LinkedBlockingQueue<>(10000),   // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy()    // 拒绝策略：由调用线程处理任务
        );

        // 用于保存所有批次的 CompletableFuture
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 分批处理避免长事务，假设每次处理 1000 条数据
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIdList.size();
        for (int i = 0; i < totalQuestionListSize; i += batchSize) {
            // 生成每批次的数据
            List<Long> subList = validQuestionIdList.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream().map(questionId -> {
                QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                questionBankQuestion.setQuestionBankId(questionBankId);
                questionBankQuestion.setQuestionId(questionId);
                questionBankQuestion.setUserId(loginUser.getId());
                return questionBankQuestion;
            }).collect(Collectors.toList());

            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            // 异步处理每批数据并添加到 futures 列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                    questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions), customExecutor);
            futures.add(future);
        }

        // 等待所有批次操作完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 关闭线程池
        customExecutor.shutdown();
    }

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                // 执行插入操作
                // 成功则跳出重试循环
                try {
                    boolean result = this.saveBatch(questionBankQuestions);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
                } catch (DataIntegrityViolationException e) {
                    log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
                } catch (DataAccessException e) {
                    log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
                } catch (Exception e) {
                    // 捕获其他异常，做通用处理
                    log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
                }
                break;
            } catch (Exception e) {
                log.warn("插入失败，重试次数: {}", i + 1);
                if (i == retryCount - 1) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "多次重试后操作仍然失败");
                }
            }
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

        List<QuestionBankQuestion> list = this.list(lambdaQueryWrapper);

        // 如果为空则都不在该题库
        ThrowUtils.throwIf(CollUtil.isEmpty(list), ErrorCode.PARAMS_ERROR, "所选题目都不存在于该题库");

        // 执行批量删除
        boolean remove = this.remove(lambdaQueryWrapper);
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "从题库移除题目失败");
    }

}
