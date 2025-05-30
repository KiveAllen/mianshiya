package com.allen.monkeyQuestion.model.dto.question;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 添加题目请求
 */
@Data
public class QuestionAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 标签列表
     */
    private List<String> tags;
    /**
     * 推荐答案
     */
    private String answer;
}