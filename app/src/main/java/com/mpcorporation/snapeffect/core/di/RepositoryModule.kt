package com.mpcorporation.snapeffect.core.di

import com.mpcorporation.snapeffect.data.catalog.EffectCatalogImpl
import com.mpcorporation.snapeffect.data.history.HistoryRepositoryImpl
import com.mpcorporation.snapeffect.data.image.ImageRepositoryImpl
import com.mpcorporation.snapeffect.data.preset.PresetRepositoryImpl
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.repository.HistoryRepository
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import com.mpcorporation.snapeffect.domain.repository.PresetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindPresetRepository(impl: PresetRepositoryImpl): PresetRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindEffectCatalog(impl: EffectCatalogImpl): EffectCatalog
}
