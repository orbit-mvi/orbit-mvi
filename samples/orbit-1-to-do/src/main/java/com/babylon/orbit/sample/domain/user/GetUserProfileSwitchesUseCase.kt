package com.babylon.orbit.sample.domain.user

import com.babylon.orbit.sample.network.UserService
import io.reactivex.Observable

class GetUserProfileSwitchesUseCase(
    private val userService: UserService
) {

    fun getUserProfileSwitches(): Observable<UserProfileSwitchesStatus> {
        return userService.getUserProfileSwitches()
            .map<UserProfileSwitchesStatus> { UserProfileSwitchesStatus.Result(it) }
            .toObservable()
            .startWith(UserProfileSwitchesStatus.Loading)
            .onErrorReturn { UserProfileSwitchesStatus.Failure(it) }
    }
}
