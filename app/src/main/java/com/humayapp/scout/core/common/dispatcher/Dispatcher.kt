package com.humayapp.scout.core.common.dispatcher

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val scoutDispatcher: ScoutDispatchers)

enum class ScoutDispatchers { Default, IO, }
