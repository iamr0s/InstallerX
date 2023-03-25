package com.rosan.installer.data.installer.model.entity

sealed class ProgressEntity {
    object Finish : ProgressEntity()

    object Ready : ProgressEntity()

    object Error : ProgressEntity()

    object Resolving : ProgressEntity()

    object ResolvedFailed : ProgressEntity()

    object ResolveSuccess : ProgressEntity()

    object Analysing : ProgressEntity()

    object AnalysedFailed : ProgressEntity()

    object AnalysedSuccess : ProgressEntity()

    object Installing : ProgressEntity()

    object InstallFailed : ProgressEntity()

    object InstallSuccess : ProgressEntity()
}