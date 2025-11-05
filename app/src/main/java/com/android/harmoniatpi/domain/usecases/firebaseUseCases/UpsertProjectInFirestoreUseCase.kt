package com.android.harmoniatpi.domain.usecases.firebaseUseCases


import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.domain.interfaces.Repository
import javax.inject.Inject

/**
 * Caso de uso para insertar o actualizar un proyecto en Firebase Firestore.
 * Una operación "upsert" significa que si el dato que intentas guardar ya existe en la base de datos
 * (por ejemplo, un proyecto con el mismo ID), se actualizará.
 */
class UpsertProjectInFirestoreUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Guarda o actualiza los datos de un proyecto en Firebase Firestore.
     */
    suspend operator fun invoke(projectModel: ProjectFirebaseModel): Result<Unit> {
        return repository.upsertProjectInFirestore(projectModel)
    }
}