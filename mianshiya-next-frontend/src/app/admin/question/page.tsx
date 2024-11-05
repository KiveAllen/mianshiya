"use client";
import CreateModal from "./components/CreateModal";
import UpdateModal from "./components/UpdateModal";
import {deleteQuestionUsingDelete, listQuestionByPageUsingPost,} from "@/api/questionController";
import {PlusOutlined} from "@ant-design/icons";
import type {ActionType, ProColumns} from "@ant-design/pro-components";
import {PageContainer, ProTable} from "@ant-design/pro-components";
import {Button, message, Popconfirm, Space, Typography} from "antd";
import React, {useRef, useState} from "react";
import TagList from "@/components/TagList";
import MdEditor from "@/components/MdEditor";
import UpdateBankModal from "@/app/admin/question/components/UpdateBankModal";

/**
 * 题目管理页面
 *
 * @constructor
 */
const QuestionAdminPage: React.FC = () => {
    // 是否显示新建窗口
    const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
    // 是否显示更新窗口
    const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
    // 是否显示更新痛苦窗口
    const [updateBankModalVisible, setUpdateBankModalVisible] = useState<boolean>(false);
    // actionRef
    const actionRef = useRef<ActionType>();
    // 当前题目点击的数据
    const [currentRow, setCurrentRow] = useState<API.Question>();

    /**
     * 删除节点
     *
     * @param row
     */
    const handleDelete = async (row: API.Question) => {
        const hide = message.loading("正在删除");
        if (!row) return true;
        try {
            await deleteQuestionUsingDelete({
                id: row.id,
            });
            hide();
            message.success("删除成功");
            actionRef?.current?.reload();
            return true;
        } catch (error) {
            hide();
            message.error("删除失败，" + error.message);
            return false;
        }
    };

    /**
     * 表格列配置
     */
    const columns: ProColumns<API.Question>[] = [
        {
            title: "id",
            dataIndex: "id",
            valueType: "text",
            hideInForm: true,
            hideInSearch: true
        },
        {
            title: "所属题库",
            dataIndex: "questionBankId",
            hideInForm: true,
            hideInTable: true
        },
        {
            title: "标题",
            dataIndex: "title",
            valueType: "text",
        },
        {
            title: "内容",
            dataIndex: "content",
            valueType: "text",
            hideInSearch: true,
            width: 240,
            renderFormItem: (_, {type, defaultRender, formItemProps, fieldProps, ...rest}, form,
            ) => {
                return (
                    // value 和 onchange 会通过 form 自动注入。
                    <MdEditor
                        // 组件的配置
                        {...fieldProps}
                    />
                );
            },
        },

        {
            title: "答案",
            dataIndex: "answer",
            valueType: "text",
            hideInSearch: true,
            width: 640,
            render: (_,record) => {
                // 控制字符数量
                return <div>
                    {record.answer.length > 90 ? record.answer.slice(0, 90) + "..." : record.answer}
                </div>
            },
            renderFormItem: (_, {type, defaultRender, formItemProps, fieldProps, ...rest}, form,
            ) => {
                return (
                    // value 和 onchange 会通过 form 自动注入。
                    <MdEditor
                        // 组件的配置
                        {...fieldProps}
                    />
                );
            },
        },
        {
            title: "标签",
            dataIndex: "tags",
            valueType: "select",
            fieldProps: {
                mode: "tags",
            },
            render: (_, record) => {
                const tagList = JSON.parse(record.tags || "[]") as string[];
                return <TagList tagList={tagList}/>
            }
        },
        {
            title: "创建用户",
            dataIndex: "userId",
            valueType: "text",
            hideInForm: true,
        },

        {
            title: "创建时间",
            sorter: true,
            dataIndex: "createTime",
            valueType: "dateTime",
            hideInSearch: true,
            hideInForm: true,
        },
        {
            title: "编辑时间",
            sorter: true,
            dataIndex: "editTime",
            valueType: "dateTime",
            hideInSearch: true,
            hideInForm: true,
        },
        {
            title: "更新时间",
            sorter: true,
            dataIndex: "updateTime",
            valueType: "dateTime",
            hideInSearch: true,
            hideInForm: true,
        },
        {
            title: "操作",
            dataIndex: "option",
            valueType: "option",
            render: (_, record) => (
                <Space size="middle">
                    <Typography.Link
                        onClick={() => {
                            setCurrentRow(record);
                            setUpdateModalVisible(true);
                        }}
                    >
                        修改
                    </Typography.Link>
                    <Typography.Link
                        onClick={() => {
                            setCurrentRow(record);
                            setUpdateBankModalVisible(true);
                        }}
                    >
                        所属题库
                    </Typography.Link>
                    <Popconfirm
                        placement="topRight"
                        title="是否删除此题目"
                        onConfirm={() => handleDelete(record)}
                        okText="是"
                        cancelText="否"
                    >
                        <Typography.Link type="danger">删除</Typography.Link>
                    </Popconfirm>
                </Space>
            ),
        },
    ];
    return (
        <PageContainer className="max-width-content">
            <ProTable<API.Question>
                headerTitle={"查询表格"}
                actionRef={actionRef}
                rowKey="key"
                scroll={{
                    x: true,
                }}
                search={{
                    labelWidth: 120,
                }}
                toolBarRender={() => [
                    <Button
                        type="primary"
                        key="primary"
                        onClick={() => {
                            setCreateModalVisible(true);
                        }}
                    >
                        <PlusOutlined/> 新建
                    </Button>,
                ]}
                request={async (params, sort, filter) => {
                    const sortField = Object.keys(sort)?.[0];
                    const sortOrder = sort?.[sortField] ?? undefined;

                    const {data, code} = await listQuestionByPageUsingPost({
                        ...params,
                        sortField,
                        sortOrder,
                        ...filter,
                    } as API.QuestionQueryRequest);
                    return {
                        success: code === 0,
                        data: data?.records || [],
                        total: Number(data?.total) || 0,
                    };
                }}
                columns={columns}
            />
            <CreateModal
                visible={createModalVisible}
                columns={columns}
                onSubmit={() => {
                    setCreateModalVisible(false);
                    actionRef.current?.reload();
                }}
                onCancel={() => {
                    setCreateModalVisible(false);
                }}
            />
            <UpdateModal
                visible={updateModalVisible}
                columns={columns}
                oldData={currentRow}
                onSubmit={() => {
                    setUpdateModalVisible(false);
                    setCurrentRow(undefined);
                    actionRef.current?.reload();
                }}
                onCancel={() => {
                    setUpdateModalVisible(false);
                }}
            />
            <UpdateBankModal
                visible={updateBankModalVisible}
                questionId={currentRow?.id}
                onCancel={() => {
                    setUpdateBankModalVisible(false);
                }}
            />
        </PageContainer>
    );
};
export default QuestionAdminPage;
