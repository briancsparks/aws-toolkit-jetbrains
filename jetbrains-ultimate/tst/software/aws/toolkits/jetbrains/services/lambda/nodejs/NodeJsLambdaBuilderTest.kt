// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.nodejs

import com.intellij.openapi.roots.ModuleRootManagerEx
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import software.aws.toolkits.jetbrains.utils.rules.NodeJsCodeInsightTestFixtureRule
import software.aws.toolkits.jetbrains.utils.rules.addLambdaHandler
import software.aws.toolkits.jetbrains.utils.rules.addPackageJsonFile
import software.aws.toolkits.jetbrains.utils.setSamExecutableFromEnvironment
import java.nio.file.Paths

class NodeJsLambdaBuilderTest {
    @Rule
    @JvmField
    val projectRule = NodeJsCodeInsightTestFixtureRule()

    private val sut = NodeJsLambdaBuilder()

    @Before
    fun setUp() {
        setSamExecutableFromEnvironment()
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
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler(subPath =   "hello-world/foo-bar")
        projectRule.fixture.addPackageJsonFile("hello-world")

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val moduleRoot = ModuleRootManagerEx.getInstanceEx(projectRule.module).contentRoots.first().path
        assertThat(baseDir.toAbsolutePath()).isEqualTo(Paths.get(moduleRoot, "hello-world"))
    }

    @Test
    fun buildDirectoryIsCorrect() {
        val expectedHandlerFile = projectRule.fixture.addLambdaHandler()
        projectRule.fixture.addPackageJsonFile()

        val baseDir = sut.handlerBaseDirectory(projectRule.module, expectedHandlerFile)
        val moduleRoot = ModuleRootManagerEx.getInstanceEx(projectRule.module).contentRoots.first().path
        assertThat(baseDir.toAbsolutePath()).isEqualTo(Paths.get(moduleRoot))
    }

//    @Test
//    fun builtFromTemplate() {
//        val subPath = "hello_world"
//        val fileName = "app"
//        val handlerName = "lambdaHandler"
//        val logicalName = "SomeFunction"
//
//        projectRule.fixture.addLambdaHandler(subPath, fileName, handlerName)
//        projectRule.fixture.addPackageJsonFile(subPath)
//
//        val templateFile = projectRule.fixture.addSamTemplate(
//            logicalName = logicalName,
//            codeUri = subPath,
//            handler = "$fileName.$handlerName",
//            runtime = Runtime.NODEJS12_X
//        )
//        val templatePath = Paths.get(templateFile.virtualFile.path)
//
//        val builtLambda = sut.buildLambdaFromTemplate(projectRule.module, templatePath, logicalName)
//
//        LambdaBuilderTestUtils.verifyEntries(
//            builtLambda,
//            "$fileName.js",
//            "package.json"
//        )
//        LambdaBuilderTestUtils.verifyPathMappings(
//            projectRule.module,
//            builtLambda,
//            "%PROJECT_ROOT%/$subPath" to "/var/task/",
//            "%BUILD_ROOT%" to "/var/task/"
//        )
//    }
//
//    @Test
//    fun dependenciesAreAdded() {
//        val subPath = "hello_world"
//        val fileName = "app"
//        val handlerName = "lambdaHandler"
//
//        val module = projectRule.module
//        val handler = projectRule.fixture.addLambdaHandler(subPath, fileName, handlerName)
//        projectRule.fixture.addPackageJsonFile(
//            content =
//            """
//             {
//                 "name": "hello-world",
//                 "version": "1.0.0",
//                 "dependencies": {
//                     "axios": "^0.18.0"
//                 }
//             }
//                 """.trimIndent()
//        )
//        val builtLambda = sut.buildLambda(module, handler, Runtime.NODEJS12_X, "$subPath/$fileName.$handlerName")
//        LambdaBuilderTestUtils.verifyEntries(
//            builtLambda,
//            "$subPath/$fileName.js",
//            "node_modules/axios/package.json",
//            "package.json"
//        )
//        LambdaBuilderTestUtils.verifyPathMappings(
//            module,
//            builtLambda,
//            "%PROJECT_ROOT%" to "/var/task/",
//            "%BUILD_ROOT%" to "/var/task"
//        )
//    }
}
