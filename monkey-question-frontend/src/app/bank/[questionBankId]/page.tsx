"use server"
import Title from "antd/es/typography/Title";

import {getQuestionBankVoByIdUsingPost} from "@/api/questionBankController";
import QuestionList from "@/components/QuestionList";
import {Avatar, Button, Card} from "antd";
import Meta from "antd/es/card/Meta";
import Paragraph from "antd/es/typography/Paragraph";
import "./index.css";

/**
 * 题库详情页面
 * @constructor
 */
export default async function BankPage({params}) {
    const {questionBankId} = params;

    let bank: undefined;

    try {
        const bankRes = await getQuestionBankVoByIdUsingPost({
            id: questionBankId,
            needQueryQuestionList: true,
            pageSize: 100,
            current: 1,
        });
        bank = bankRes.data;
    } catch (e) {
        console.error("获取题库详情失败，" + e.message);
    }

    if (!bank) {
        return <div>获取题库详情失败，请刷新重试</div>;
    }

    // 获取第一题
    let firstQuestionId;
    if (bank.questionPage?.records && bank.questionPage.records.length > 0) {
        firstQuestionId = bank.questionPage.records[0].id;
    }

    return (
        <div id="bankPage" className="max-width-content">
            <Card>
                <Meta
                    avatar={<Avatar src={bank.picture} size={72}/>}
                    title={
                        <Title level={3} style={{marginBottom: 0}}>
                            {bank.title}
                        </Title>
                    }
                    description={
                        <>
                            <Paragraph type="secondary">
                                {bank.description}
                            </Paragraph>
                            <Button
                                type="primary"
                                shape="round"
                                href={`/bank/${questionBankId}/question/${firstQuestionId}`}
                                target="_blank"
                                disabled={!firstQuestionId}
                            >
                                开始刷题
                            </Button>
                        </>

                    }
                ></Meta>

            </Card>
            <div style={{marginBottom: 16}}/>
            <QuestionList
                questionList={bank.questionPage?.records || []}
                cardTitle={`题目列表（${
                    bank.questionPage?.total || 0
                }）`}
                questionBankId={questionBankId}
            />
        </div>
    );
}
