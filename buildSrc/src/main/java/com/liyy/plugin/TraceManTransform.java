package com.liyy.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * Author: 李岳阳
 * Date: 11-12
 * Time: 20:17
 * Description：
 */
public class TraceManTransform extends Transform {

    private Project project;

    public TraceManTransform(Project project) {
        this.project = project;
    }

    Config initConfig() {
        MyConfig configuration = (MyConfig) project.getExtensions().findByName("traceMan");
        Config config = new Config();
        config.mTraceConfigFile = configuration.traceConfigFile;
        config.mIsNeedLogTraceInfo = configuration.logTraceInfo;
        return config;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("[MethodTraceMan]: transform()");
        MyConfig traceManConfig = (MyConfig) project.getExtensions().findByName("traceMan");
        String output = traceManConfig.output;
        if (output == null || output.isEmpty()) {
            traceManConfig.output = project.getBuildDir().getAbsolutePath() + File.separator + "traceman_output";
        }

        if (traceManConfig.open) {
            //读取配置
            Config traceConfig = initConfig();
            traceConfig.parseTraceConfigFile();


            Collection<TransformInput> inputs = transformInvocation.getInputs();
            TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
            if (outputProvider != null) {
                outputProvider.deleteAll();
            }

            //遍历
            for (TransformInput input : inputs) {
                for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    traceSrcFiles(directoryInput, outputProvider, traceConfig);
                }

                for (JarInput jarInput : input.getJarInputs()) {
                    traceJarFiles(jarInput, outputProvider, traceConfig);
                }
            }
        }
    }

    private void traceSrcFiles(DirectoryInput directoryInput, TransformOutputProvider outputProvider, Config traceConfig) throws IOException {
        if (directoryInput.getFile().isDirectory()) {
            eachFileRecurse(directoryInput.getFile(), traceConfig);
        }

        //处理完输出给下一任务作为输入
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        FileUtils.copyDirectory(directoryInput.getFile(), dest);
    }

    public void eachFileRecurse(File self, Config traceConfig)
            throws FileNotFoundException, IllegalArgumentException, IOException {
        checkDir(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                processFile(file, traceConfig);
                eachFileRecurse(file, traceConfig);
            }
            processFile(file, traceConfig);
        }
    }

    private void checkDir(File dir) throws FileNotFoundException, IllegalArgumentException {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getAbsolutePath());
        if (!dir.isDirectory())
            throw new IllegalArgumentException("The provided File object is not a directory: " + dir.getAbsolutePath());
    }

    private void processFile(File file, Config traceConfig) throws IOException {
        String name = file.getName();
        if (traceConfig.isNeedTraceClass(name)) {
            ClassReader classReader = new ClassReader(IOGroovyMethods.getBytes(new FileInputStream(file)));
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new MyClassVisitor(classWriter, traceConfig);
            classReader.accept(cv, EXPAND_FRAMES);
            byte[] code = classWriter.toByteArray();
            FileOutputStream fos = new FileOutputStream(
                    file.getParentFile().getAbsolutePath() + File.separator + name);
            fos.write(code);
            fos.close();
        }
    }

    private void traceJarFiles(JarInput jarInput, TransformOutputProvider outputProvider, Config traceConfig) throws IOException {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            //重命名输出文件,因为可能同名,会覆盖
            String jarName = jarInput.getName();
            String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }
            JarFile jarFile = new JarFile(jarInput.getFile());
            Enumeration enumeration = jarFile.entries();

            File tmpFile = new File(jarInput.getFile().getParent() + File.separator + "classes_temp.jar");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }

            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));

            //循环jar包里的文件
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                if (traceConfig.isNeedTraceClass(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry);
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream));
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor cv = new MyClassVisitor(classWriter, traceConfig);
                    classReader.accept(cv, EXPAND_FRAMES);
                    byte[] code = classWriter.toByteArray();
                    jarOutputStream.write(code);
                } else {
                    jarOutputStream.putNextEntry(zipEntry);
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }

            jarOutputStream.close();
            jarFile.close();

            //处理完输出给下一任务作为输入
            File dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
            FileUtils.copyFile(tmpFile, dest);

            tmpFile.delete();
        }
    }

    @Override
    public String getName() {
        return "traceManTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }


}
