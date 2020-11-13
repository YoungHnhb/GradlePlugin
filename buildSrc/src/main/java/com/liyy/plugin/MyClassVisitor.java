package com.liyy.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyClassVisitor extends ClassVisitor {
    private String className;
    private boolean isABSClass = false;
    private boolean isBeatClass = false;
    private boolean isConfigTraceClass = false;
    private Config traceConfig;

    public MyClassVisitor(ClassVisitor classVisitor, Config config) {
        super(Opcodes.ASM7, classVisitor);
        traceConfig = config;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
        //抽象方法或者接口
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }

        //插桩代码所属类
        String resultClassName = name.replace(".", "/");
        if (resultClassName.equals(traceConfig.mBeatClass)) {
            this.isBeatClass = true;
        }

        //是否是配置的需要插桩的类
        isConfigTraceClass = traceConfig.isConfigTraceClass(className);

        boolean isNotNeedTraceClass = isABSClass || isBeatClass || !isConfigTraceClass;
        if (traceConfig.mIsNeedLogTraceInfo && !isNotNeedTraceClass) {
            System.out.println("MethodTraceMan-trace-class: ${" + className + " ?: 未知}");
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isConstructor = MethodFilter.isConstructor(name);
        if (isABSClass || isBeatClass || !isConfigTraceClass || isConstructor) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            System.out.println("LifecycleClassVisitor : visitMethod:" + name);
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            return new MyMethodVisitor(mv, access, name, desc, className, traceConfig);
        }
    }
}
