package com.nonsense.chat.di

import javax.inject.Qualifier

/** Application-lifetime coroutine scope (survives individual screens/VMs). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
