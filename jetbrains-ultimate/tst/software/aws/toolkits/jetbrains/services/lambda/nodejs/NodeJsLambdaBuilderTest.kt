// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.nodejs

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModuleRootManagerEx
import com.intellij.testFramework.PsiTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import software.amazon.awssdk.services.lambda.model.Runtime
import software.aws.toolkits.jetbrains.services.PathMapping
import software.aws.toolkits.jetbrains.services.lambda.verifyPathMappings
import software.aws.toolkits.jetbrains.utils.rules.HeavyNodeJsCodeInsightTestFixtureRule
import software.aws.toolkits.jetbrains.utils.rules.addLambdaHandler
import software.aws.toolkits.jetbrains.utils.rules.addPackageJsonFile
import software.aws.toolkits.jetbrains.utils.rules.addSamTemplate
import software.aws.toolkits.jetbrains.utils.setSamExecutableFromEnvironment
import java.nio.file.Paths

class NodeJsLambdaBuilderTest {
    @Rule
    @JvmField
    val projectRule = HeavyNodeJsCodeInsightTestFixtureRule()

    private val sut = NodeJsLambdaBuilder()

    @Before
    fun setUp() {
        setSamExecutableFromEnvironment()
        PsiTestUtil.addModule(projectRule.project, ModuleType.EMPTY, "main", projectRule.fixture.tempDirFixture.findOrCreateDir("main"))
    }

    @Test
    fun handlerBaseDirIsCorrect() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler(subPath = "hello-world")
        projectRule.fixture.addPackageJsonFile("hello-world")

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val moduleRoot = ModuleRootManagerEx.getInstanceEx(projectRule.module).contentRoots.first().path
        assertThat(baseDir.toAbsolutePath()).isEqualTo(Paths.get(moduleRoot, "hello-world"))
    }

    @Test
    fun handlerBaseDirIsCorrectInSubDir() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler(subPath = "hello-world/foo-bar")
        projectRule.fixture.addPackageJsonFile("hello-world")

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val moduleRoot = ModuleRootManagerEx.getInstanceEx(projectRule.module).contentRoots.first().path
        assertThat(baseDir.toAbsolutePath()).isEqualTo(Paths.get(moduleRoot, "hello-world"))
    }

    @Test
    fun missingPackageJsonReturnsNullHandlerBaseDir() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler(subPath = "hello-world/foo-bar")

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        assertThat(baseDir).isNull()
    }

    @Test
    fun buildDirectoryIsCorrect() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler()
        projectRule.fixture.addPackageJsonFile()

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val moduleRoot = ModuleRootManagerEx.getInstanceEx(projectRule.module).contentRoots.first().path
        assertThat(baseDir.toAbsolutePath()).isEqualTo(Paths.get(moduleRoot))
    }

    @Test
    fun defaultPathMappingsAreCorrect() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler()
        projectRule.fixture.addPackageJsonFile()
        val codeUri = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val buildDir = sut.getBuildDirectory(projectRule.module)

        val logicalId = "SomeFunction"
        val template = projectRule.fixture.addSamTemplate(logicalId, codeUri.toString(), "app.handle", Runtime.NODEJS12_X)
        val templatePath = Paths.get(template.virtualFile.path)

        val actualMappings = sut.defaultPathMappings(templatePath, logicalId, buildDir)
        sut.verifyPathMappings(
            projectRule.module, actualMappings,
            listOf(
                PathMapping(buildDir.resolve(logicalId).toString(), "/var/task"),
                PathMapping(codeUri.toString(), "/var/task")
            )
        )
    }
}
