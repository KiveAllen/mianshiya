"use server"

import "./index.css";
import {listQuestionVoByPageUsingPost} from "@/api/questionController";
import {Flex} from "antd";
import Title from "antd/es/typography/Title";
import QuestionsTable from "@/components/QuestionTable";


/**
 * 题库列表页面
 * @constructor
 */
export default async function QuestionsPage({searchParams}) {
    // 获取 url 的查询参数
    const {q: searchText} = searchParams;
    // 题目和总数
    let questionList = [];
    let total = 0;

    try {
        const questionRes = await listQuestionVoByPageUsingPost({
            title: searchText,
            pageSize: 12,
            sortField: 'createTime',
            sortOrder: 'descend',
        })
        questionList = questionRes.data.records ?? [];
        total = questionRes.data.total ?? 0;

    } catch (e) {
        console.error('获取题目列表失败，' + e.message);
    }

    return <div id="questionsPage" className="max-width-content">
        <Flex justify="space-between" align="center">
            <Title level={3}>题目大全</Title>
        </Flex>
        <QuestionsTable defaultQuestionList={questionList} defaultTotal={total} defaultSearchParams={{
          title: searchText
        }}/>

    </div>;
}


