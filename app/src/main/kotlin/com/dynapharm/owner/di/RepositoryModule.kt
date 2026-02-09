package com.dynapharm.owner.di

import com.dynapharm.owner.data.repository.AuthRepositoryImpl
import com.dynapharm.owner.data.repository.DashboardRepositoryImpl
import com.dynapharm.owner.domain.repository.AuthRepository
import com.dynapharm.owner.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to their implementations.
 * Uses @Binds for efficient dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds AuthRepository interface to AuthRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Binds DashboardRepository interface to DashboardRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository
}
