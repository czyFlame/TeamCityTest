import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.1"

project {

    buildType(Test)
    buildType(Build)
    buildType(Release)
    buildType(CompositeTest)
    buildType(AAA)

    template(First)

    features {
        feature {
            id = "PROJECT_EXT_3"
            type = "buildtype-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "BuildTestStatus",
                    "key": "BuildTestStatus"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("hideFilters", "")
            param("title", "First")
            param("defaultFilters", "")
            param("seriesTitle", "Serie")
        }
        feature {
            id = "PROJECT_EXT_4"
            type = "buildtype-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "czyTest",
                    "key": "czyTest"
                  },
                  {
                    "type": "valueType",
                    "title": "czyTest2",
                    "key": "czyTest2"
                  },
                  {
                    "type": "valueType",
                    "title": "czyTest3",
                    "key": "czyTest3"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("hideFilters", "")
            param("title", "New chart title")
            param("defaultFilters", "")
            param("seriesTitle", "Serie")
        }
        feature {
            id = "PROJECT_EXT_5"
            type = "buildtype-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Java-blank",
                    "key": "Java-blank"
                  },
                  {
                    "type": "valueType",
                    "title": "Java-code",
                    "key": "Java-code"
                  },
                  {
                    "type": "valueType",
                    "title": "Java-comment",
                    "key": "Java-comment"
                  },
                  {
                    "type": "valueType",
                    "title": "Java-nFiles",
                    "key": "Java-nFiles"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("title", "cloc-java")
            param("seriesTitle", "Serie")
        }
    }
}

object AAA : BuildType({
    name = "AAA"

    params {
        param("NEW_VERSION", "1233456")
    }

    steps {
        script {
            name = "AAA-test"
            scriptContent = """
                #!/bin/bash
                
                echo "this is a test from settings"
                echo ${'$'}NEW_VERSION
            """.trimIndent()
        }
    }
})

object Build : BuildType({
    name = "Build"

    artifactRules = """
        target/TeamCityTest-1.0-SNAPSHOT.jar
        target/TeamCityTest-*-SNAPSHOT.jar => test
    """.trimIndent()

    params {
        param("env.NEW_VERSION", "test")
        param("env.newVersion", "%env.NEW_VERSION%")
        param("system.newVersion", "try")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            name = "gradle test"
            tasks = "hello"
        }
        gradle {
            tasks = "propertyTry"
        }
    }

    triggers {
        vcs {
        }
    }
})

object CompositeTest : BuildType({
    name = "CompositeTest"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        snapshot(Build) {
        }
    }
})

object Release : BuildType({
    name = "Release"

    artifactRules = "+:**/*"

    params {
        param("NEW_VERSION", "${Build.depParamRefs["env.NEW_VERSION"]}")
        param("env.NEW_VERSION", "${Build.depParamRefs["env.NEW_VERSION"]}")
        param("ARTIFACTDIR", "artifact/")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "release"
            scriptContent = """
                #!/bin/bash
                
                echo "release test"
                echo ${'$'}NEW_VERSION
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(Build) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
        snapshot(CompositeTest) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})

object Test : BuildType({
    name = "Test"

    params {
        param("env.SOURCE_BRANCH", "%system.teamcity.projectName%-%system.teamcity.buildConfName%-%build.counter%")
    }

    steps {
        script {
            name = "parameter test"
            scriptContent = """
                #!/bin/bash
                
                version_suffix=`date +'%%Y%%m%%d'`
                echo ${'$'}version_suffix
            """.trimIndent()
        }
    }
})

object ThirdTest : BuildType({
    name = "ThirdTest"

    params {
        param("env.SOURCE_BRANCH", "%system.teamcity.projectName%-%system.teamcity.buildConfName%-%build.counter%")
    }

    steps {
        script {
            name = "parameter test"
            scriptContent = """
                #!/bin/bash
                
                version_suffix=`date +'%%Y%%m%%d'`
                echo ${'$'}version_suffix
                echo "third test"
            """.trimIndent()
        }
    }
})

object First : Template({
    name = "first"
    description = "sdf"
})
