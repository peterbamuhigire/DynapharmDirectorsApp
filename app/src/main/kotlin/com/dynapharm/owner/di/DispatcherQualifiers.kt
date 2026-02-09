package com.dynapharm.owner.di

import javax.inject.Qualifier

/**
 * Qualifier for IO Dispatcher.
 * Used for IO-bound operations like network calls and database access.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for Main Dispatcher.
 * Used for UI operations on the main thread.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
