package com.android.harmoniatpi.domain.usecases.friendUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.userPreferences.FriendRequestReceived
import javax.inject.Inject

class AcceptFriendRequestUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(
        currentUser: UserPreferences,
        request: FriendRequestReceived
    ): Result<UserPreferences> {
        return repository.acceptFriendRequest(currentUser, request)
    }
}