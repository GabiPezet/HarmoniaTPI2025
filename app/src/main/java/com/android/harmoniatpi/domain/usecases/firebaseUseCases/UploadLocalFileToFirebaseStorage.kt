package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

class UploadLocalFileToFirebaseStorage @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(localFilePath: String, remotePath: String) =
        repository.uploadLocalFileToFirebaseStorage(localFilePath, remotePath)
}