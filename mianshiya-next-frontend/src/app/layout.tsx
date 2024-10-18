"use client"
import React, {useCallback, useEffect} from "react";
import {AntdRegistry} from "@ant-design/nextjs-registry";
import BasicLayout from "@/layouts/BasicLayout";
import "./globals.css";
import store, {AppDispatch} from "@/app/stores";
import {Provider, useDispatch} from "react-redux";
import {getLoginUserUsingGet} from "@/api/userController";
import {setLoginUser} from "@/app/stores/loginUser";

/**
 * 全局初始化页面
 * @param children
 * @constructor
 */
const InitLayout: React.FC<Readonly<{
    children: React.ReactNode;
}>
> = ({children}) => {
    const dispatch = useDispatch<AppDispatch>();
    // 初始化用户信息
    const doInitLoginUser = useCallback(async () => {
        const res = await getLoginUserUsingGet();
        if (res.data) {
            // 更新全局用户状态
        } else {
            // 跳转到登录页面
            setTimeout(() => {
                const testUser = {
                    id: 1,
                    userName: "测试登录",
                    userAvatar: "https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png",
                }
                dispatch(setLoginUser(testUser))
            }, 3000);
        }

    }, [])

    // 只执行一次
    useEffect(() => {
        doInitLoginUser();
    }, [])

    return children;
};

export default function RootLayout({children}: Readonly<{
    children: React.ReactNode;
}>) {

    return (
        <html lang="zh">
        <body>
        <AntdRegistry>
            <Provider store={store}>
                <InitLayout>
                    <BasicLayout>{children}</BasicLayout>
                </InitLayout>
            </Provider>
        </AntdRegistry>
        </body>
        </html>
    );
}
