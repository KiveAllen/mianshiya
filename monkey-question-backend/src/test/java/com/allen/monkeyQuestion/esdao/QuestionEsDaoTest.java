package com.allen.monkeyQuestion.esdao;

import com.allen.monkeyQuestion.model.dto.question.QuestionEsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class QuestionEsDaoTest {

    @Resource
    private QuestionEsDao questionEsDao;

    @Test
    void findByUserId() {
        List<QuestionEsDTO> byUserId = questionEsDao.findByUserId(1L);
        byUserId.forEach(System.out::println);
    }
}

