// @ts-ignore
/* eslint-disable */
import request from "@/app/libs/request";

/** addQuestionBankQuestion POST /api/questionBankQuestion/add */
export async function addQuestionBankQuestionUsingPost(
  body: API.QuestionBankQuestionAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/questionBankQuestion/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** batchAddQuestionsToBank POST /api/questionBankQuestion/add/batch */
export async function batchAddQuestionsToBankUsingPost(
    body: API.QuestionBankQuestionBatchAddRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>(
        "/api/questionBankQuestion/add/batch",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            data: body,
            ...(options || {}),
        }
    );
}

/** deleteQuestionBankQuestion DELETE /api/questionBankQuestion/delete */
export async function deleteQuestionBankQuestionUsingDelete(
  body: API.QuestionBankQuestionDeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/questionBankQuestion/delete", {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** getQuestionBankQuestion GET /api/questionBankQuestion/list */
export async function getQuestionBankQuestionUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getQuestionBankQuestionUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListQuestionBankQuestionVO_>(
    "/api/questionBankQuestion/list",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** batchRemoveQuestionsFromBank POST /api/questionBankQuestion/remove/batch */
export async function batchRemoveQuestionsFromBankUsingPost(
    body: API.QuestionBankQuestionBatchRemoveRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>(
        "/api/questionBankQuestion/remove/batch",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            data: body,
            ...(options || {}),
        }
    );
}
