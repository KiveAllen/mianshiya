"use client";
import {searchQuestionVoByPageUsingPost,} from "@/api/questionController";
import type {ActionType, ProColumns} from "@ant-design/pro-components";
import {ProTable} from "@ant-design/pro-components";
import React, {useRef, useState} from "react";
import TagList from "@/components/TagList";
import Link from "next/link";

interface Props {
    // 默认值用于传入数据
    defaultQuestionList?: API.QuestionVO[];
    defaultTotal?: number;
    // 默认搜索条件
    defaultSearchParams?: API.QuestionQueryRequest;
}

/**
 * 题目表格组件
 *
 * @constructor
 */
const QuestionsTable: React.FC = (props: Props) => {
    const {defaultQuestionList, defaultTotal, defaultSearchParams} = props;

    const actionRef = useRef<ActionType>();

    // 题目列表
    const [questionList, setQuestionList] = useState<API.QuestionVO[]>(defaultQuestionList || []);
    // 题目总数
    const [total, setTotal] = useState<number>(defaultTotal || 0);
    // 用于判断是否首次加载
    const [init, setInit] = useState(true);

    /**
     * 表格列配置
     */
    const columns: ProColumns<API.QuestionVO>[] = [
        {
            title: "搜索",
            dataIndex: "searchText",
            valueType: "text",
            hideInTable: true
        },
        {
            title: "标题",
            dataIndex: "title",
            valueType: "text",
            hideInSearch: true,
            render: (_, record) => {
                return <Link href={`/question/${record.id}`}>{record.title}</Link>
            }
        },
        {
            title: "标签",
            dataIndex: "tagList",
            valueType: "select",
            fieldProps: {
                mode: "tags",
            },
            render: (_, record) => {
                return <TagList tagList={record.tagList}/>
            }
        },
    ];
    return (
        <div className={"question-table"}>
            <ProTable<API.QuestionVO>
                actionRef={actionRef}
                size="large"
                rowKey="key"
                search={{
                    labelWidth: "auto",
                }}
                form={{
                    initialValues: defaultSearchParams,
                }}
                dataSource={questionList}
                pagination={{
                    pageSize: 12,
                    showTotal: (total) => `共 ${total} 条`,
                    showSizeChanger: false,
                    total
                }}
                request={async (params, sort, filter) => {
                    // 首次请求
                    if (init) {
                        setInit(false);
                        // 如果传入了默认值，则不请求数据
                        if (defaultQuestionList && defaultTotal) {
                            return {
                                success: true,
                                data: defaultQuestionList,
                                total: defaultTotal
                            };
                        }
                    }

                    const sortField = Object.keys(sort)?.[0] || 'createTime';
                    const sortOrder = sort?.[sortField] ?? 'descend';

                    const {data, code} = await searchQuestionVoByPageUsingPost({
                        ...params,
                        sortField,
                        sortOrder,
                        ...filter,
                    } as API.QuestionQueryRequest);

                    // 更新结果
                    const newData = data?.records || []
                    const newTotal = data?.total || 0

                    setQuestionList(newData);
                    setTotal(newTotal);

                    return {
                        success: code === 0,
                        data: newData,
                        total: newTotal
                    };
                }}
                columns={columns}
            />
        </div>

    );
};
export default QuestionsTable;
