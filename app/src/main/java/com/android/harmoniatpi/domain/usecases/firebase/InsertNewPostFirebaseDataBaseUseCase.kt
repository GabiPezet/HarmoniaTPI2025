package com.android.harmoniatpi.domain.usecases.firebase

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.userPreferences.Post
import javax.inject.Inject

class InsertNewPostFirebaseDataBaseUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(post: Post) = repository.insertPostRealTimeDB(post)
}