package com.babylon.orbit.sample.network

import com.babylon.orbit.sample.domain.user.UserProfile
import com.babylon.orbit.sample.domain.user.UserProfileSwitches
import io.reactivex.Single
import java.lang.RuntimeException

class UserServiceImpl : UserService {

    override fun getUserProfileSwitches(): Single<UserProfileSwitches> {
        return Single.create { it.onSuccess(UserProfileSwitches(true)) }
    }

    override fun getUserProfile(userId: Int): Single<UserProfile> {
        return Single.create {
            if (userId == 1) {
                it.onSuccess(UserProfile(1, "Babylon User", "user@babylon.com"))
            } else {
                it.onError(RuntimeException("user with id $userId not found"))
            }
        }
    }
}
