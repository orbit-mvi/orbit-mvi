package com.babylon.orbit.sample.domain.user

sealed class UserProfileSwitchesStatus {

    data class Result(val userProfileSwitches: UserProfileSwitches) : UserProfileSwitchesStatus()

    object Loading : UserProfileSwitchesStatus()

    data class Failure(val error: Throwable) : UserProfileSwitchesStatus()
}
