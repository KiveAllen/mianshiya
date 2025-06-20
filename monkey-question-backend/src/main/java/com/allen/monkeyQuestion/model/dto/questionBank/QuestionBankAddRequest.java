package com.allen.monkeyQuestion.model.dto.questionBank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建题库题目请求
 */
@Data
public class QuestionBankAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 图片
     */
    private String picture;
}