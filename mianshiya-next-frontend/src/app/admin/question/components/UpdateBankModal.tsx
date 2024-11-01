import {Form, message, Modal, Select} from "antd";
import React, {useEffect, useState} from "react";
import {
    addQuestionBankQuestionUsingPost,
    deleteQuestionBankQuestionUsingDelete,
    getQuestionBankQuestionUsingGet
} from "@/api/questionBankQuestionController";
import {listQuestionBankVoByPageUsingPost} from "@/api/questionBankController";

interface Props {
    questionId?: number;
    visible: boolean;
    onCancel: () => void;
}

/**
 * 更新弹窗
 * @param props
 * @constructor
 */
const UpdateBankModal: React.FC<Props> = (props) => {
    const {questionId, visible, onCancel} = props;
    const [form] = Form.useForm();
    const [questionBankList, setQuestionBankList] = useState<API.QuestionBankVO[]>([]);

    // 获取题目所属题库列表
    const getCurrentQuestionBankList = async () => {
        try {
            const questionBankQuestionRes = await getQuestionBankQuestionUsingGet({
                questionId: questionId
            })
            const list = (questionBankQuestionRes.data ?? []).map((item: {
                questionBankId: number;
            }) => item.questionBankId)
            form.setFieldValue("questionBankIdList", list);
        } catch (e) {
            console.error('获取题目所属题库列表失败，' + e.message);
        }
    }

    useEffect(() => {
        if (questionId) {
            getCurrentQuestionBankList()
        }
    }, [questionId]);

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
            title={"更新所属题库"}
            open={visible}
            footer={null}
            onCancel={() => {
                onCancel?.();
            }}
        >
            <Form form={form} style={{marginTop: 24}}>
                <Form.Item label="所属题库" name="questionBankIdList">
                    <Select
                        mode="multiple"
                        style={{width: '100%'}}
                        options={
                            questionBankList.map((questionBank) => {
                                return {
                                    label: questionBank.title,
                                    value: questionBank.id
                                }
                            })
                        }
                        onSelect={async (value) => {
                            const hide = message.loading("正在更新");
                            try {
                                await addQuestionBankQuestionUsingPost({
                                    questionId: questionId,
                                    questionBankId: value
                                });
                                hide();
                                message.success("绑定题库成功");
                            } catch (e) {
                                hide();
                                message.error("更新失败" + e.message);
                            }
                        }}
                        onDeselect={async (value) => {
                            const hide = message.loading("正在更新");
                            try{
                                await deleteQuestionBankQuestionUsingDelete({
                                    questionId: questionId,
                                    questionBankId: value
                                });
                                hide();
                                message.success("取消绑定题库成功");
                            } catch (e) {
                                hide();
                                message.error("更新失败" + e.message);
                            }
                        }}
                    />
                </Form.Item>
            </Form>
        </Modal>
    );
};
export default UpdateBankModal;
