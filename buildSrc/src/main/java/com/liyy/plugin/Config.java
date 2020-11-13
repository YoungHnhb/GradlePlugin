package com.liyy.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 15:08
 * Description：
 */
public class Config {
    //一些默认无需插桩的类
    String[] UNNEED_TRACE_CLASS = new String[]{"R.class", "R$", "Manifest", "BuildConfig"};

    //插桩配置文件
    String mTraceConfigFile = null;

    //需插桩的包
    private HashSet<String> mNeedTracePackageMap = new HashSet<String>();

    //在需插桩的包范围内的 无需插桩的白名单
    private HashSet<String> mWhiteClassMap = new HashSet<String>();

    //在需插桩的包范围内的 无需插桩的包名
    private HashSet<String> mWhitePackageMap = new HashSet<String>();

    //插桩代码所在类
    String mBeatClass = null;

    //是否需要打印出所有被插桩的类和方法
    boolean mIsNeedLogTraceInfo = false;

    public boolean isNeedTraceClass(String fileName) {
        boolean isNeed = true;
        if (fileName.endsWith(".class")) {
            for (String unTraceCls : UNNEED_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    isNeed = false;
                    break;
                }
            }
        } else {
            isNeed = false;
        }
        return isNeed;
    }

    //判断是否是traceConfig.txt中配置范围的类
    public boolean isConfigTraceClass(String className) {
        if (mNeedTracePackageMap.isEmpty()) {
            System.out.println("1");
            return !(isInWhitePackage(className) || isInWhiteClass(className));
        } else {
            if (isInNeedTracePackage(className)) {
                System.out.println("2");
                return !(isInWhitePackage(className) || isInWhiteClass(className));
            } else {
                System.out.println("3");
                return false;
            }
        }
    }

    private boolean isInNeedTracePackage(String className) {
        boolean isIn = false;
        for (String it : mNeedTracePackageMap) {
            System.out.println(it + "***" + className);
            if (className.contains(it)) {
                System.out.println("FFFF");
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    private boolean isInWhitePackage(String className) {
        boolean isIn = false;
        for (String it : mWhitePackageMap) {
            if (className.contains(it)) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    private boolean isInWhiteClass(String className) {
        boolean isIn = false;
        for (String it : mWhiteClassMap) {
            if (className.equals(it)) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    /**
     * 解析插桩配置文件
     */
    public void parseTraceConfigFile() throws FileNotFoundException {
        System.out.println("parseTraceConfigFile start!!!!!!!!!!!!");
        File traceConfigFile = new File(mTraceConfigFile);
        if (!traceConfigFile.exists()) {
            throw new FileNotFoundException(
                    "Trace config file not exist, " +
                            "Please read quickstart.找不到 $" + mTraceConfigFile + " 配置文件, " +
                            "尝试阅读一下 QuickStart。");
        }

        String configStr = Utils.readFileAsString(traceConfigFile.getAbsolutePath());
        List<String> configArray = Arrays.asList(configStr.split(System.lineSeparator()));

        for (String config : configArray) {
            if (config == null || config.trim().length() == 0) {
                continue;
            }
            if (config.startsWith("#")) {
                continue;
            }
            if (config.startsWith("[")) {
                continue;
            }

            if (config.startsWith("-tracepackage")) {
                config = config.replace("-tracepackage ", "");
                mNeedTracePackageMap.add(config);
                System.out.println("tracepackage:$config");
            } else if (config.startsWith("-keepclass ")) {
                config = config.replace("-keepclass ", "");
                mWhiteClassMap.add(config);
                System.out.println("keepclass:$config");
            } else if (config.startsWith("-keeppackage ")) {
                config = config.replace("-keeppackage ", "");
                mWhitePackageMap.add(config);
                System.out.println("keeppackage:$config");
            } else if (config.startsWith("-beatclass ")) {
                config = config.replace("-beatclass ", "");
                mBeatClass = config;
                System.out.println("beatclass:$config");
            }
        }
    }

}
