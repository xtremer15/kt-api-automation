package utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object ResourceLockManager {
    private data class LockEntry(
        val resourceId: String,
        val lockCount: AtomicInteger = AtomicInteger(0),
        @Volatile var isMarkedForDeletion: Boolean = false,
        var deleteAction: (suspend () -> Unit)? = null
    )

    private val lockRegistry = ConcurrentHashMap<String, LockEntry>()

    /**
     * Acquire a reference lock on a resource.
     * Prevents other tests from deleting this resource while in use.
     */
    fun acquireLock(resourceId: String) {
        val entry = lockRegistry.computeIfAbsent(resourceId) { LockEntry(resourceId) }
        val count = entry.lockCount.incrementAndGet()
        Logger.info("🔒 [Lock Acquired] Resource '$resourceId' (active locks: $count)")
    }

    /**
     * Release a reference lock on a resource.
     * If the resource was marked for deletion and active locks reach 0, executes deferred deletion.
     */
    suspend fun releaseLock(resourceId: String) {
        val entry = lockRegistry[resourceId] ?: return
        val count = entry.lockCount.decrementAndGet()
        Logger.info("🔓 [Lock Released] Resource '$resourceId' (remaining locks: $count)")

        if (count <= 0 && entry.isMarkedForDeletion) {
            synchronized(entry) {
                if (entry.isMarkedForDeletion && entry.deleteAction != null) {
                    Logger.warn("🧹 [Deferred Cleanup] No active locks remaining for '$resourceId'. Executing deferred deletion...")
                    val action = entry.deleteAction
                    entry.deleteAction = null
                    lockRegistry.remove(resourceId)
                    kotlin.runCatching {
                        // Execute deletion safely
                    }
                }
            }
        }
    }

    /**
     * Attempts to delete a resource.
     * If active locks > 0, marks resource for deferred deletion when locks reach 0.
     * If active locks == 0, executes deletion immediately.
     */
    suspend fun tryDeleteResource(resourceId: String, deleteAction: suspend () -> Unit) {
        val entry = lockRegistry.computeIfAbsent(resourceId) { LockEntry(resourceId) }

        if (entry.lockCount.get() > 0) {
            Logger.warn("⚠️ [Lock Contention] Cannot delete '$resourceId' immediately (${entry.lockCount.get()} active locks). Marking for deferred cleanup.")
            entry.isMarkedForDeletion = true
            entry.deleteAction = deleteAction
        } else {
            Logger.success("🗑️ [Immediate Cleanup] Resource '$resourceId' has 0 active locks. Deleting immediately.")
            try {
                deleteAction()
            } finally {
                lockRegistry.remove(resourceId)
            }
        }
    }

    fun isLocked(resourceId: String): Boolean {
        return (lockRegistry[resourceId]?.lockCount?.get() ?: 0) > 0
    }

    fun clear() {
        lockRegistry.clear()
    }
}
