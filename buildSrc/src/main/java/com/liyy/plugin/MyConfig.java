package com.liyy.plugin;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 11:49
 * Description：
 */
public class MyConfig {
    String output;
    boolean open;
    String traceConfigFile;
    boolean logTraceInfo;

    public MyConfig() {
        open = true;
        output = "";
        logTraceInfo = false;
    }
}
