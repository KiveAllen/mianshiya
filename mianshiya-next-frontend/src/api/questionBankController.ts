// @ts-ignore
/* eslint-disable */
import request from "@/app/libs/request";

/** addQuestionBank POST /api/questionBank/add */
export async function addQuestionBankUsingPost(
  body: API.QuestionBankAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>("/api/questionBank/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteQuestionBank DELETE /api/questionBank/delete */
export async function deleteQuestionBankUsingDelete(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/questionBank/delete", {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** getQuestionBankById GET /api/questionBank/get */
export async function getQuestionBankByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getQuestionBankByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseQuestionBank_>("/api/questionBank/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getQuestionBankVOById POST /api/questionBank/get/vo */
export async function getQuestionBankVoByIdUsingPost(
  body: API.QuestionBankQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseQuestionBankVO_>("/api/questionBank/get/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** listQuestionBankByPage POST /api/questionBank/list/page */
export async function listQuestionBankByPageUsingPost(
  body: API.QuestionBankQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageQuestionBank_>(
    "/api/questionBank/list/page",
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

/** listQuestionBankVOByPage POST /api/questionBank/list/page/vo */
export async function listQuestionBankVoByPageUsingPost(
  body: API.QuestionBankQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageQuestionBankVO_>(
    "/api/questionBank/list/page/vo",
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

/** updateQuestionBank POST /api/questionBank/update */
export async function updateQuestionBankUsingPost(
  body: API.QuestionBankUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>("/api/questionBank/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
