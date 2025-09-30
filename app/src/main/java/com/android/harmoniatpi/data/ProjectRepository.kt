package com.android.harmoniatpi.data

import com.android.harmoniatpi.data.database.dao.ProjectDao
import com.android.harmoniatpi.data.database.toDomain
import com.android.harmoniatpi.data.database.toEntity
import com.android.harmoniatpi.domain.model.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAllProjects().map { list -> list.map { it.toDomain() } }

    suspend fun saveProject(project: Project) {
        projectDao.guardarProject(project.toEntity())
    }
}

