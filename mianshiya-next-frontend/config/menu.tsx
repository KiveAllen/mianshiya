import { MenuDataItem } from "@ant-design/pro-layout";
import { CrownOutlined } from "@ant-design/icons"; // 菜单列表

// 菜单列表
export const menus = [
  {
    path: "/",
    name: "首页",
  },
  {
    path: "/questions",
    name: "题目",
  },
  {
    path: "/banks",
    name: "题库",
  },
  {
    path: "https://mianshiya.com/",
    name: "面试鸭",
    target: "_blank",
  },
  {
    path: "/admin",
    name: "管理",
    icon: <CrownOutlined />,
    children: [
      {
        path: "/admin/users",
        name: "用户管理",
      },
    ],
  },
] as MenuDataItem[];
