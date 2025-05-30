package com.allen.monkeyQuestion.mapper;

import com.allen.monkeyQuestion.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author KiveAllen
 * @description 描述 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-09-27 11:51:57
*/
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 获取最近五分钟未删除的数据
     */
    @Select("select * from question where update_time > #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);
}




