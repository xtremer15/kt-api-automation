package specs

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import reporter.CustomReporter
import utils.RequestContext

abstract class BaseSpec(body: FreeSpec.() -> Unit = {}) : FreeSpec() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    init {
        register(CustomReporter)

        afterTest {
            RequestContext.reset()
        }

        body()
    }
}
