package com.mpcorporation.snapeffect.domain.usecase

import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.model.UserPreset
import com.mpcorporation.snapeffect.domain.repository.PresetRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoadUserPresetsUseCase @Inject constructor(
    private val repository: PresetRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(): List<UserPreset> =
        withContext(dispatchers.io) { repository.load() }
}

class AddUserPresetUseCase @Inject constructor(
    private val repository: PresetRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(preset: UserPreset) =
        withContext(dispatchers.io) { repository.add(preset) }
}

class DeleteUserPresetUseCase @Inject constructor(
    private val repository: PresetRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(index: Int) =
        withContext(dispatchers.io) { repository.delete(index) }
}
