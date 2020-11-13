package com.liyy.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 11:46
 * Description：
 */
public class MyPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().create("traceMan", MyConfig.class);
        AppExtension baseExtension = target.getExtensions().getByType(AppExtension.class);
        baseExtension.registerTransform(new TraceManTransform(target));
    }
}
