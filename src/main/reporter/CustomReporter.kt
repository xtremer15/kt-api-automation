package reporter

import di.FrameworkModule
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.listeners.SpecListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestStatus
import io.qameta.allure.Allure
import utils.Logger
import utils.Utils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeoutException

object CustomReporter : TestListener, SpecListener, ProjectListener {
    private val testResults = mutableListOf<Map<String, Any?>>()

    // === Universal Wrapper Hooks ===
    override suspend fun beforeAny(testCase: TestCase) {
        val testName = testCase.name.testName
        val typeStr = testCase.type.name
        Logger.info("[beforeAny] Starting $typeStr node: $testName")
        recordEvent(testCase, "ANY_STARTED", null)
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        val testName = testCase.name.testName
        val typeStr = testCase.type.name
        Logger.success("[afterAny] Completed $typeStr node: $testName [${result.status}]")
        recordEvent(testCase, "ANY_FINISHED", null)
    }

    // === Scope Teardown Hook ===
    override suspend fun afterScope(testCase: TestCase, result: TestResult) {
        val testName = testCase.name.testName
        Logger.info("[afterScope] Scope Completed for node: $testName [${result.status}]")
        recordEvent(testCase, "SCOPE_FINISHED", null)
    }

    // === Container Scope Hooks ===
    override suspend fun beforeContainer(testCase: TestCase) {
        val testName = testCase.name.testName
        Logger.info("[beforeContainer] Entering container group: $testName")
        recordEvent(testCase, "CONTAINER_STARTED", null)
    }

    override suspend fun afterContainer(testCase: TestCase, result: TestResult) {
        val testName = testCase.name.testName
        Logger.success("[afterContainer] Finished container group: $testName [${result.status}]")
        recordEvent(testCase, "CONTAINER_FINISHED", null)
    }

    // === Leaf Test (Each) Hooks ===
    override suspend fun beforeEach(testCase: TestCase) {
        val uuid = Utils.generateUUID()
        val threadId = Thread.currentThread().id
        val testName = testCase.name.testName

        Allure.label("uuid", uuid)
        Allure.label("env", "dev")
        Allure.label("thread", threadId.toString())

        Logger.info("[beforeEach] Starting leaf test: $testName on thread $threadId")
        recordEvent(testCase, "TEST_STARTED", null)
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        val testName = testCase.name.testName
        when (result.status) {
            TestStatus.Success -> {
                Logger.success("[afterEach] Completed leaf test: $testName [SUCCEEDED]")
                recordEvent(testCase, "SUCCEEDED", null)
            }
            TestStatus.Error, TestStatus.Failure -> {
                val reason = if (result.errorOrNull is TimeoutException) "timeout" else "unknown"
                Allure.label("flakiness", reason)
                Logger.fail("[afterEach] Test failed: $testName [FAILED - $reason]")
                recordEvent(testCase, "FAILED", reason)
            }
            TestStatus.Ignored -> {
                Logger.warn("[afterEach] Test skipped: $testName [SKIPPED]")
                recordEvent(testCase, "SKIPPED", null)
            }
        }
    }

    // === Invocation Iteration Hooks ===
    override suspend fun beforeInvocation(testCase: TestCase, iteration: Int) {
        Logger.info("[beforeInvocation] Test ${testCase.name.testName} — Iteration #$iteration started")
    }

    override suspend fun afterInvocation(testCase: TestCase, iteration: Int) {
        Logger.info("[afterInvocation] Test ${testCase.name.testName} — Iteration #$iteration finished")
    }

    // === Spec Lifecycle Hooks ===
    override suspend fun beforeSpec(spec: Spec) {
        val specName = spec::class.simpleName ?: "UnknownSpec"
        Logger.info("[beforeSpec] Entering Spec Scope: $specName")
    }

    override suspend fun afterSpec(spec: Spec) {
        val specName = spec::class.simpleName ?: "UnknownSpec"
        Logger.success("[afterSpec] Exiting Spec Scope: $specName")
    }

    // === Project Scope Teardown Hook ===
    override suspend fun afterProject() {
        Logger.success("[afterProject] Test run completed — dumping run_metadata.json...")
        writeResultsToFile()
    }

    private fun recordEvent(testCase: TestCase, status: String, flakiness: String?) {
        val testName = testCase.name.testName
        val threadId = Thread.currentThread().id

        val metadata = mapOf(
            "name" to testName,
            "type" to testCase.type.name,
            "env" to "dev",
            "thread" to threadId,
            "status" to status,
            "flakiness" to flakiness
        )

        synchronized(testResults) {
            testResults.add(metadata)
        }
    }

    private fun writeResultsToFile() {
        val resourceDir = "target/test-status"
        val filePath = "$resourceDir/run_metadata.json"
        try {
            Files.createDirectories(Paths.get(resourceDir))
            val jsonString = FrameworkModule.prettyGson.toJson(testResults)
            File(filePath).writeText(jsonString)
            Logger.success("Metadata written to $filePath")
        } catch (e: Exception) {
            Logger.error("Failed to write run_metadata.json", e)
        }
    }
}
