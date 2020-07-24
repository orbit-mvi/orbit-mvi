package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.user.UserProfileSwitchesStatus

data class UserProfileExtra(
    val userProfileSwitchesStatus: UserProfileSwitchesStatus,
    val userId: Int
)
