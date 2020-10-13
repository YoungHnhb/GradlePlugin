package com.liyy.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println 'Hello Author!'
        def extension = project.extensions.create('liyueyang', MyExtension)
        project.afterEvaluate {
            println "Hello2 ${extension.aa}!!"
        }
        def transform = new MyTransform()
        def baseExtension = project.extensions.getByType(BaseExtension)
        baseExtension.registerTransform(transform)
    }
}