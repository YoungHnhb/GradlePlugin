package com.liyy.plugin;

import org.objectweb.asm.Opcodes;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 19:45
 * Description：
 */
public class TraceMethod {

    private int id = 0;
    private int accessFlag = 0;
    private String className = null;
    private String methodName = null;
    private String desc = null;

    public static TraceMethod create(int id, int accessFlag,String className,String methodName,String desc)  {
        TraceMethod traceMethod = new TraceMethod();
        traceMethod.id = id;
        traceMethod.accessFlag = accessFlag;
        traceMethod.className = className.replace("/", ".");
        traceMethod.methodName = methodName;
        traceMethod.desc = desc.replace("/", ".");
        return traceMethod;
    }

    public String getMethodNameText() {
        if (desc == null || isNativeMethod()) {
            return this.className + "." + this.methodName;
        } else {
            return this.className + "." + this.methodName + "." + desc;
        }
    }

    public String toString() {
        if (desc == null || isNativeMethod()) {
            return "$id,$accessFlag,$className $methodName";
        } else {
            return "$id,$accessFlag,$className $methodName $desc";
        }
    }

    public boolean isNativeMethod() {
        return (accessFlag & Opcodes.ACC_NATIVE) != 0;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TraceMethod) {
            TraceMethod tm = (TraceMethod) obj;
            return tm.getMethodNameText().equals(getMethodNameText());
        } else {
            return false;
        }
    }
}
