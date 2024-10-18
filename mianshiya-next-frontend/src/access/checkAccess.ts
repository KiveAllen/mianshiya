import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 检查权限
 * @param loginUser 当前登录用户
 * @param needAccess 需要具有的权限
 * @return boolean 有无权限
 */
const checkAccess = (loginUser: API.LoginUserVO, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
    // 获取当前登录用户具有的权限（没有登录默认无权限）
    const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN;
    // 如果当前不需要权限
    if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
        return true;
    }
    // 如果需要登录才能访问
    else if (needAccess === ACCESS_ENUM.USER) {
        if (loginUserAccess === ACCESS_ENUM.NOT_LOGIN) {
            return false;
        }
    }
    // 如果需要管理员才能访问
    else if (needAccess === ACCESS_ENUM.ADMIN) {
        if (loginUserAccess !== ACCESS_ENUM.ADMIN) {
            return false;
        }
    }
    return true;
};

export default checkAccess;