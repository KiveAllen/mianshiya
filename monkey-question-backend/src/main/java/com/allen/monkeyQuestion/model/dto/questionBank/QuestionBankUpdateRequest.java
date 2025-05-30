package com.allen.monkeyQuestion.model.dto.questionBank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新题库题目请求
 */
@Data
public class QuestionBankUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
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