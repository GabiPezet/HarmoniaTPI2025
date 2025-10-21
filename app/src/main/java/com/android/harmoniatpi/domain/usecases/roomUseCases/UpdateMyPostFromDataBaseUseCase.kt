package com.android.harmoniatpi.domain.usecases.roomUseCases

import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.userPreferences.Post
import javax.inject.Inject

class UpdateMyPostFromDataBaseUseCase @Inject constructor(
    private val repository: Repository,
    private val jsonUtils: JsonUtils
) {
    suspend operator fun invoke(post: Post) = repository.updateMyPost(post.toDataBase(jsonUtils))
}