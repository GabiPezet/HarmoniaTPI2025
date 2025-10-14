package com.android.harmoniatpi.domain.cache

import com.android.harmoniatpi.domain.model.project.Project
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HoloJamCache @Inject constructor() {
    var currentProjectSelected : Project? = null
}