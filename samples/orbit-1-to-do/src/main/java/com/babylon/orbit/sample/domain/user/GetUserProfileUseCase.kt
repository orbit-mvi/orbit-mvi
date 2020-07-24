package com.babylon.orbit.sample.domain.user

import com.babylon.orbit.sample.network.UserService
import io.reactivex.Observable

class GetUserProfileUseCase(
    private val userService: UserService
) {

    fun getUserProfile(userId: Int): Observable<UserProfileStatus> {
        return userService.getUserProfile(userId)
            .map<UserProfileStatus> { UserProfileStatus.Result(it) }
            .toObservable()
            .startWith(UserProfileStatus.Loading)
            .onErrorReturn { UserProfileStatus.Failure(it) }
    }
}
