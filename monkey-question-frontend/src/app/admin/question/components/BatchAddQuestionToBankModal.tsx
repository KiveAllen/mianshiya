import {Button, Form, message, Modal, Select} from "antd";
import React, {useEffect, useState} from "react";
import {batchAddQuestionsToBankUsingPost} from "@/api/questionBankQuestionController";
import {listQuestionBankVoByPageUsingPost} from "@/api/questionBankController";

interface Props {
    questionIdList?: number[];
    visible: boolean;
    onSubmit: () => void;
    onCancel: () => void;
}

/**
 * 批量向题库添加题目弹窗
 * @param props
 * @constructor
 */
const BatchAddQuestionsToBankModal: React.FC<Props> = (props) => {
    const {questionIdList, visible, onCancel, onSubmit} = props;
    const [form] = Form.useForm();
    const [questionBankList, setQuestionBankList] = useState<API.QuestionBankVO[]>([]);

    /**
     * 提交
     * @param fields
     */
    const doSubmit = async (fields: API.QuestionBankQuestionBatchAddRequest) => {
        const hide = message.loading("正在操作");
        const questionBankId = fields.questionBankId;
        try {
            await batchAddQuestionsToBankUsingPost({
                questionIdList,
                questionBankId,
            });
            hide();
            message.success("操作成功");
            onSubmit?.();
        } catch (error: any) {
            hide();
            message.error("操作失败，" + error.message);
        }
    };

    // 获取所有题库列表
    const getQuestionBankList = async () => {
        // 全量获取题库
        const pageSize = 200;

        try {
            const questionBankRes = await listQuestionBankVoByPageUsingPost({
                sortField: 'createTime',
                sortOrder: 'descend',
                pageSize,
            })
            setQuestionBankList(questionBankRes.data.records ?? [])
        } catch (e) {
            console.error('获取所有题库列表失败，' + e.message);
        }
    }

    useEffect(() => {
        getQuestionBankList()
    }, []);

    return (
        <Modal
            destroyOnClose
            title={"批量向题库添加题目"}
            open={visible}
            footer={null}
            onCancel={() => {
                onCancel?.();
            }}
        >
            <Form form={form} style={{marginTop: 24}} onFinish={doSubmit}>
                <Form.Item label="选择题库" name="questionBankId">
                    <Select
                        style={{width: '100%'}}
                        options={
                            questionBankList.map((questionBank) => {
                                return {
                                    label: questionBank.title,
                                    value: questionBank.id
                                }
                            })
                        }
                    />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit">提交</Button>
                </Form.Item>
            </Form>
        </Modal>
    );
};
export default BatchAddQuestionsToBankModal;
