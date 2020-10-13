package com.liyy.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class MyTransform extends Transform {

    MyTransform() {

    }

    @Override
    String getName() {
        return 'MyTransform'
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        super.transform(transformInvocation)
        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider
        inputs.each { TransformInput input ->
            input.jarInputs.each {
                File dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                println("Jar: ${it.file}")
                println("Jar Dest: ${dest}")
                FileUtils.copyFile(it.file, dest)
            }
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name
                        if (name.endsWith(".class")) {
                            ClassReader classReader = new ClassReader(file.bytes)
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                            ClassVisitor cv = new MyClassVisitor(classWriter)
                            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                            byte[] code = classWriter.toByteArray()
                            String pathName = file.parentFile.absolutePath + File.separator + name
                            println("Dir-pre: ${pathName}")
                            FileOutputStream fos = new FileOutputStream(pathName)
                            fos.write(code)
                            fos.close()
                        }
                    }
                }
                //处理完输出给下一任务作为输入
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                println("Dir: ${directoryInput.file}")
                println("Dir Dest: ${dest}")
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }
}