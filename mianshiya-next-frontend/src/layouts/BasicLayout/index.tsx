"use client";
import {GithubFilled, LogoutOutlined, SearchOutlined,} from '@ant-design/icons';
import {ProLayout,} from '@ant-design/pro-components';

import {Dropdown, Input,} from 'antd';
import React from 'react';
import Image from 'next/image';
import {usePathname} from 'next/navigation';
import Link from 'next/link';
import GlobalFooter from "@/components/GlobalFooter";
import './index.css'
import {menus} from "../../../config/menu";
import {RootState} from "@/app/stores";
import {useSelector} from 'react-redux';
import menuAccess from "@/access/menuAccess";

/**
 * 搜索框
 * @constructor
 */
const SearchInput = () => {
    return (
        <div
            key="SearchOutlined"
            aria-hidden
            style={{
                display: "flex",
                alignItems: "center",
                marginInlineEnd: 24,
            }}
            onMouseDown={(e) => {
                e.stopPropagation();
                e.preventDefault();
            }}
        >
            <Input
                style={{
                    borderRadius: 4,
                    marginInlineEnd: 12,
                }}
                prefix={<SearchOutlined/>}
                placeholder="搜索题目"
                variant="borderless"
            />
        </div>
    );
};

interface Props {
    children: React.ReactNode;
}

export default function BasicLayout({children}: Props) {
    const pathname = usePathname();
    // 当前登录用户
    const loginUser = useSelector((state: RootState) => state.loginUser)

    return (
        <div
            id="basicLayout"
            style={{
                height: "100vh",
                overflow: "auto",
            }}
        >
            <ProLayout
                title="面试鸭刷题平台"
                layout="top"
                logo={
                    <Image
                        src="/assets/logo.jpg"
                        height={32}
                        width={32}
                        alt="面试鸭刷题网站 -- 小幽"
                    />
                }
                location={{
                    pathname,
                }}
                avatarProps={{
                    src: loginUser.userAvatar || "/assets/logo.png",
                    size: "small",
                    title: loginUser.userName || "小幽",
                    render: (_props, dom) => {
                        return (
                            <Dropdown
                                menu={{
                                    items: [
                                        {
                                            key: "logout",
                                            icon: <LogoutOutlined/>,
                                            label: "退出登录",
                                        },
                                    ],
                                }}
                            >
                                {dom}
                            </Dropdown>
                        );
                    },
                }}
                actionsRender={(props) => {
                    if (props.isMobile) return [];
                    return [
                        <SearchInput key="search"/>,
                        <a
                            key="github"
                            target="_blank"
                            href="https://github.com/ant-design/pro-components"
                        >
                            <GithubFilled key="GithubFilled"/>
                        </a>,
                    ];
                }}
                headerTitleRender={(logo, title) => {
                    return (
                        <a>
                            {logo}
                            {title}
                        </a>
                    );
                }}
                // 渲染底部栏
                footerRender={() => {
                    return <GlobalFooter/>;
                }}
                onMenuHeaderClick={(e) => console.log(e)}
                // 菜单数据渲染
                menuDataRender={() => {
                    return menuAccess(loginUser, menus);
                }}
                // 定义了菜单项如何渲染
                menuItemRender={(item, dom) => (
                    <Link href={item.path || "/"} target={item.target}>
                        {dom}
                    </Link>
                )}
            >

                {children}
            </ProLayout>
        </div>
    );
}
