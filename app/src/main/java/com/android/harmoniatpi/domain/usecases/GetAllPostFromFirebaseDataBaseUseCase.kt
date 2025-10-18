package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class GetAllPostFromFirebaseDataBaseUseCase @Inject constructor(private val repository: Repository) {
   operator fun invoke() = repository.getAllPostsFlow()
}