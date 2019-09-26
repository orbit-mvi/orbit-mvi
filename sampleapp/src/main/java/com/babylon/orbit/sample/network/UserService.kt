package com.babylon.orbit.sample.network

import com.babylon.orbit.sample.domain.user.UserProfile
import com.babylon.orbit.sample.domain.user.UserProfileSwitches
import io.reactivex.Single

interface UserService {

    fun getUserProfileSwitches(): Single<UserProfileSwitches>

    fun getUserProfile(userId: Int): Single<UserProfile>
}
