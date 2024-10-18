import {usePathname} from "next/navigation";
import React from "react";
import {RootState} from "@/app/stores";
import {useSelector} from "react-redux";
import {findAllMenuItemByPath} from "../../config/menu";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";
import Forbidden from "@/forbidden";

/**
 * 统一权限校验拦截器
 * @param children
 * @constructor
 */
const AccessLayout: React.FC<Readonly<{
    children: React.ReactNode;
}>
> = ({children}) => {
    const pathname = usePathname();
    // 当前登录用户
    const loginUser = useSelector((state: RootState) => state.loginUser)
    // 当前路径需要的权限
    const menu = findAllMenuItemByPath(pathname);
    const needAccess = menu?.access ?? ACCESS_ENUM.NOT_LOGIN;

    // 校验权限
    const canAccess = checkAccess(loginUser, needAccess);
    if (!canAccess) {
        return <Forbidden/>
    }
    return children;
};

export default AccessLayout;