/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.distribution.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskDependencyMatchers
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import org.gradle.util.TestUtil

class DistributionPluginTest extends AbstractProjectBuilderSpec {
    private final Project project = TestUtil.builder(temporaryFolder).withName("test-project").build()

    def "adds convention object and a main distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def distributions = project.extensions.getByType(DistributionContainer.class)
        def dist = distributions.main
        dist.name == 'main'
        dist.baseName == 'test-project'
    }

    def "provides default values for additional distributions"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def distributions = project.extensions.getByType(DistributionContainer.class)
        def dist = distributions.create('custom')
        dist.name == 'custom'
        dist.baseName == 'test-project-custom'
    }

    def "adds distZip task for main distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def task = project.tasks.distZip
        task instanceof Zip
        task.archivePath == project.file("build/distributions/test-project.zip")
    }

    def "adds distZip task for custom distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.distributions.create('custom')

        then:
        def task = project.tasks.customDistZip
        task instanceof Zip
        task.archivePath == project.file("build/distributions/test-project-custom.zip")
    }

    def "adds distTar task for main distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def task = project.tasks.distTar
        task instanceof Tar
        task.archivePath == project.file("build/distributions/test-project.tar")
    }

    def "adds distTar task for custom distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.distributions.create('custom')

        then:
        def task = project.tasks.customDistTar
        task instanceof Tar
        task.archivePath == project.file("build/distributions/test-project-custom.tar")
    }

    def "adds assembleDist task for custom distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.distributions.create('custom')

        then:
        def task = project.tasks.assembleCustomDist
        task instanceof DefaultTask
        task TaskDependencyMatchers.dependsOn ("customDistZip","customDistTar")
    }

    def "distribution names include project version when specified"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.version = '1.2'

        then:
        def zip = project.tasks.distZip
        zip.archivePath == project.file("build/distributions/test-project-1.2.zip")
        def tar = project.tasks.distTar
        tar.archivePath == project.file("build/distributions/test-project-1.2.tar")
    }

    def "adds installDist task for main distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def task = project.installDist
        task instanceof Sync
        task.destinationDir == project.file("build/install/test-project")
    }

    def "adds installDist task for custom distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.distributions.create('custom')

        then:
        def task = project.installCustomDist
        task instanceof Sync
        task.destinationDir == project.file("build/install/test-project-custom")
    }

    def "adds assembleDist task for main distribution"() {
        when:
        project.pluginManager.apply(DistributionPlugin)

        then:
        def task = project.assembleDist
        task.dependsOn.findAll {it instanceof Task}.collect{ it.path }.containsAll([":distTar", ":distZip"])
    }

    public void "distribution name is configurable"() {
        when:
        project.pluginManager.apply(DistributionPlugin)
        project.distributions.main.baseName = "SuperApp";

        then:
        def distZipTask = project.tasks.distZip
        distZipTask.archiveName == "SuperApp.zip"
    }
}
