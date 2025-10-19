package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetAllPostFromFirebaseDataBaseUseCase @Inject constructor(private val repository: Repository) {
   operator fun invoke(userID: String) = repository.getAllPostsFlowRealTimeDB(userID)
}