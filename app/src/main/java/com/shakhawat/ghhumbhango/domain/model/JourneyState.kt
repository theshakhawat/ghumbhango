package com.shakhawat.ghhumbhango.domain.model

/**
 * State representing the active tracking lifecycle of a transit journey.
 */
enum class JourneyState {
    /**
     * No journey is currently being tracked.
     */
    IDLE,

    /**
     * Background GPS service is actively streaming locations and evaluating proximity.
     */
    TRACKING,

    /**
     * User has paused tracking temporarily (conserving battery or during prolonged transit stoppage).
     */
    PAUSED,

    /**
     * Proximity threshold reached! User is within the alarm distance.
     */
    APPROACHING,

    /**
     * User reached their stop and confirmed dismissal.
     */
    COMPLETED,

    /**
     * Journey was stopped or cancelled by user.
     */
    STOPPED
}

