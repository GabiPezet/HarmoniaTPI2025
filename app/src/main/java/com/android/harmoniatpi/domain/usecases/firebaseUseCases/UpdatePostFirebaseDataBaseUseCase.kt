package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.userPreferences.Post
import javax.inject.Inject

class UpdatePostFirebaseDataBaseUseCase @Inject constructor(private val repository: Repository){
    suspend operator fun invoke(post: Post) = repository.updatePostRealTimeDB(post)
}