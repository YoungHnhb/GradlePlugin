package com.liyy.plugin;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 19:44
 * Description：
 */
public class MethodFilter {

    public static boolean isConstructor(String methodName) {
        return methodName.contains("<init>");
    }
}
