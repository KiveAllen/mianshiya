import {menus} from "../../config/menu";
import checkAccess from "@/access/checkAccess";

/**
 * 获取可访问的菜单（递归）
 * @param loginUser
 * @param menuItems
 */
const getAccessibleMenu = (loginUser: API.LoginUserVO, menuItems = menus) => {
    return menuItems.filter((item) => {
        if (!checkAccess(loginUser, item.access)) {
            return false
        }
        if (item.children) {
            item.children = getAccessibleMenu(loginUser, item.children)
        }
        return true;
    })
};

export default getAccessibleMenu;