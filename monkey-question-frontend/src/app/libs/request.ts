import axios from "axios";

// 创建 Axios 示例
const myAxios = axios.create({
  // 请求地址
  baseURL: "http://localhost:8101",
  // 请求超时
  timeout: 10000,
  // cookies 携带
  withCredentials: true,
});

// 创建请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 请求执行前执行
    // 日志：请求的URL，请求方法，请求参数
    console.log("请求：", config.url, config.method, config.data);
    return config;
  },
  function (error) {
    // 处理请求错误
    // 日志：错误
    console.log("请求错误：", error);
    return Promise.reject(error);
  },
);

// 创建响应拦截器
myAxios.interceptors.response.use(
  // 2xx 响应触发
  function (response) {
    // 处理响应数据
    const { data } = response;
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息接口，或者不是登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes("user/get/login") &&
        !window.location.pathname.includes("/user/login")
      ) {
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
    } else if (data.code !== 0) {
      // 其他错误
      throw new Error(data.message ?? "服务器错误");
    }
    return data;
  },
  // 非 2xx 响应触发
  function (error) {
    // 处理响应错误
    return Promise.reject(error);
  },
);

export default myAxios;
