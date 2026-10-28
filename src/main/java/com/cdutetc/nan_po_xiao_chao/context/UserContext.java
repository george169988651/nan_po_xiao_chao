package com.cdutetc.nan_po_xiao_chao.context;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.annotation.TableField;

public class UserContext {

    public static final ThreadLocal<Pair<String,String>> userContext = new ThreadLocal<>();

    public static void setCurrentUser(Pair<String,String> userId) {
        userContext.set(userId);
    }
    public static Pair<String,String> getCurrentUser() {
        return userContext.get();
    }
    public static void removeCurrentUser() {
        userContext.remove();
    }
}
