package com.shakhawat.ghhumbhango.domain.model

/**
 * Types of actions user can trigger to resolve a readiness issue.
 */
enum class ReadinessActionType {
    REQUEST_LOCATION_PERMISSION,
    REQUEST_NOTIFICATION_PERMISSION,
    OPEN_LOCATION_SETTINGS,
    REQUEST_BATTERY_OPTIMIZATION,
    OPEN_APPLICATION_SETTINGS,
    NONE
}

/**
 * Single system capability check item.
 */
data class ReadinessItem(
    val id: String,
    val title: String,
    val description: String,
    val isReady: Boolean,
    val isCritical: Boolean,
    val actionType: ReadinessActionType = ReadinessActionType.NONE
)

/**
 * Full snapshot of device & system readiness for transit journey tracking.
 */
data class SystemReadiness(
    val items: List<ReadinessItem>
) {
    val isFullyReady: Boolean
        get() = items.all { it.isReady }

    val hasCriticalIssues: Boolean
        get() = items.any { it.isCritical && !it.isReady }

    val readyCount: Int
        get() = items.count { it.isReady }

    val totalCount: Int
        get() = items.size

    val readinessPercentage: Int
        get() = if (totalCount == 0) 100 else ((readyCount.toFloat() / totalCount) * 100).toInt()
}

