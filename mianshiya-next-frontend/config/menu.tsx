import {MenuDataItem} from "@ant-design/pro-layout";
import {CrownOutlined} from "@ant-design/icons";
import AccessEnum from "@/access/accessEnum"; // 菜单列表

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
        access: AccessEnum.ADMIN,
        icon: <CrownOutlined/>,
        children: [
            {
                path: "/admin/users",
                name: "用户管理",
                access: AccessEnum.ADMIN,
            },
        ],
    },
] as MenuDataItem[];

// 根据全部路径查找所有菜单
export const findAllMenuItemByPath = (path: string): MenuDataItem | null => {
    return findMenuItemByPath(menus, path);
};

// 根据路径查找所有菜单（递归）
export const findMenuItemByPath = (menus: MenuDataItem[], path: string): MenuDataItem | null => {
    for (const menu of menus) {
        if (menu.path === path) {
            return menu;
        }
        if (menu.children) {
            const matchedMenuItem = findMenuItemByPath(menu.children, path);
            if (matchedMenuItem) {
                return matchedMenuItem;
            }
        }
    }
    return null;
}

