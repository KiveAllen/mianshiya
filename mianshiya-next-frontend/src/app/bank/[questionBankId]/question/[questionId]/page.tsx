"use server"
import Title from "antd/es/typography/Title";
import {getQuestionBankVoByIdUsingPost} from "@/api/questionBankController";
import {Flex, Menu} from "antd";
import "./index.css";
import {getQuestionVoByIdUsingGet} from "@/api/questionController";
import React from "react";
import Sider from "antd/es/layout/Sider";
import {Content} from "antd/es/layout/layout";
import QuestionCard from "@/components/QuestionCard";
import Link from "next/link";
import {headers} from "next/headers";

/**
 * 题库题目详情页面
 * @constructor
 */
export default async function BankQuestionPage({ params }) {
    const { questionBankId, questionId } = params;

    let bank, question;

    const headersList = headers();

    const cookie = headersList.get("cookie");

    try {
        const [bankRes, questionRes] = await Promise.all([
            getQuestionBankVoByIdUsingPost({
                id: questionBankId,
                needQueryQuestionList: true,
                pageSize: 100,
                current: 1,
                },
                {
                    headers: {
                        Cookie: cookie || "", // 添加 cookie
                    }
                }),
            getQuestionVoByIdUsingGet({
                id: questionId,
                },
                {
                    headers: {
                        Cookie: cookie || "", // 添加 cookie
                    }
                }),
        ]);

        bank = bankRes.data;
        question = questionRes.data;
    } catch (e) {
        console.error("获取题库或题目详情失败，" + e.message);
        return <div>获取详情失败，请刷新重试</div>;
    }

    if (!bank) {
        return <div>获取题库详情失败，请刷新重试</div>;
    }
    if (!question) {
        return <div>获取题目详情失败，请刷新重试</div>;
    }

    // 题目菜单列表
    const questionMenuItemList = (bank.questionPage?.records || []).map(
        (question) => {
            return {
                label: <Link href={`/bank/${questionBankId}/question/${question.id}`} prefetch={false}>{question.title}</Link>,
                key: question.id,
            };
        },
    );

    return (
        <div id="bankQuestionPage">
            <Flex gap={24}>
                <Sider width={240} theme="light" style={{ padding: "24px 0" }}>
                    <Title level={4} style={{ padding: "0 20px" }}>
                        {bank.title}
                    </Title>
                    <Menu items={questionMenuItemList} selectedKeys={[questionId]} />
                </Sider>
                <Content>
                    <QuestionCard question={question}></QuestionCard>
                </Content>
            </Flex>
        </div>
    );
}
