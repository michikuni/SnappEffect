package com.mpcorporation.snapeffect.domain.repository

import com.mpcorporation.snapeffect.domain.model.UserPreset

interface PresetRepository {
    suspend fun load(): List<UserPreset>
    suspend fun add(preset: UserPreset)
    suspend fun delete(index: Int)
}
