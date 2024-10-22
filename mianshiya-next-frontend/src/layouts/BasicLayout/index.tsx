"use client";
import {GithubFilled, LogoutOutlined, SearchOutlined,} from "@ant-design/icons";
import {ProLayout} from "@ant-design/pro-components";

import {Dropdown, Input, message} from "antd";
import React from "react";
import Image from "next/image";
import {usePathname, useRouter} from "next/navigation";
import Link from "next/link";
import GlobalFooter from "@/components/GlobalFooter";
import "./index.css";
import {menus} from "../../../config/menu";
import {AppDispatch, RootState} from "@/app/stores";
import {useDispatch, useSelector} from "react-redux";
import menuAccess from "@/access/menuAccess";
import {userLogoutUsingPost} from "@/api/userController";
import {setLoginUser} from "@/app/stores/loginUser";
import {DEFAULT_USER} from "@/constants/user";

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
        prefix={<SearchOutlined />}
        placeholder="搜索题目"
        variant="borderless"
      />
    </div>
  );
};

interface Props {
  children: React.ReactNode;
}

export default function BasicLayout({ children }: Props) {
  const pathname = usePathname();
  // 当前登录用户
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();

  // 注销登录
  const userLogout = async () => {
    try {
      await userLogoutUsingPost();
      message.success("退出登录成功！");
      // 保存用户登录态
      dispatch(setLoginUser(DEFAULT_USER));
      router.replace("/user/login");
    } catch (e) {
      message.error("退出登录失败，" + e);
    }
  };

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
            if (!loginUser.id) {
              return (
                <div
                  onClick={() => {
                    router.push("/user/login");
                  }}
                >
                  {dom}
                </div>
              );
            }
            return (
              <Dropdown
                menu={{
                  items: [
                    {
                      key: "logout",
                      icon: <LogoutOutlined />,
                      label: "退出登录",
                    },
                  ],
                  onClick: async (event: { key: React.Key }) => {
                    const { key } = event;
                    if (key === "logout") {
                      await userLogout();
                    }
                  },
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
            <SearchInput key="search" />,
            <a
              key="github"
              target="_blank"
              href="https://github.com/ant-design/pro-components"
            >
              <GithubFilled key="GithubFilled" />
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
          return <GlobalFooter />;
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
