package com.liyy.plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MyMethodVisitor extends AdviceAdapter {

    protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String desc, String className) {
        super(api, methodVisitor, access, name, desc);
        TraceMethod traceMethod = TraceMethod.create(0, access, className, name, desc);
        this.methodName = traceMethod.getMethodNameText();
        this.className = className;
        this.name = name;
    }

    Config traceConfig;

    public MyMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor, String className, Config config) {
        this(Opcodes.ASM7, methodVisitor, access, name, descriptor, className);
        traceConfig = config;
    }

    private String methodName = null;
    private String name = null;
    private String className = null;
    private int maxSectionNameLength = 127;

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        String methodName = generatorMethodName();
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(
                INVOKESTATIC,
                traceConfig.mBeatClass,
                "start",
                "(Ljava/lang/String;)V",
                false
        );

        if (traceConfig.mIsNeedLogTraceInfo) {
            System.out.println("MethodTraceMan-trace-method: ${" + methodName + "?: 未知}");
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitLdcInsn(generatorMethodName());
        mv.visitMethodInsn(
                INVOKESTATIC,
                traceConfig.mBeatClass,
                "end",
                "(Ljava/lang/String;)V",
                false
        );
    }

    private String generatorMethodName(){
        String sectionName = methodName;
        int length = sectionName.length();
        if (length > maxSectionNameLength && !(sectionName == null || sectionName.trim().length() == 0)) {
            // 先去掉参数
            int parmIndex = sectionName.indexOf('(');
            sectionName = sectionName.substring(0, parmIndex);
            // 如果依然更大，直接裁剪
            length = sectionName.length();
            if (length > 127) {
                sectionName = sectionName.substring(length - maxSectionNameLength);
            }
        }
        return sectionName;
    }
}
