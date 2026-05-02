package com.mpcorporation.snapeffect.presentation.common.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.model.UserPreset
import com.mpcorporation.snapeffect.domain.usecase.AddUserPresetUseCase
import com.mpcorporation.snapeffect.domain.usecase.DeleteUserPresetUseCase
import com.mpcorporation.snapeffect.domain.usecase.LoadUserPresetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPresetViewModel @Inject constructor(
    private val loadUserPresets: LoadUserPresetsUseCase,
    private val addUserPreset: AddUserPresetUseCase,
    private val deleteUserPreset: DeleteUserPresetUseCase
) : ViewModel() {

    suspend fun load(): List<UserPreset> = loadUserPresets()

    fun add(preset: UserPreset) {
        viewModelScope.launch { addUserPreset(preset) }
    }

    fun delete(index: Int) {
        viewModelScope.launch { deleteUserPreset(index) }
    }
}
