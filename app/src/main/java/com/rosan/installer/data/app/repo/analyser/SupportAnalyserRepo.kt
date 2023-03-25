package com.rosan.installer.data.app.repo.analyser

import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.repo.AnalyserRepo

interface SupportAnalyserRepo : AnalyserRepo {
    fun isSupport(data: DataEntity): Boolean
}