package com.humayapp.scout.core.navigation

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

typealias AnimOffset = (Int) -> Int

object NavTransition {

    const val DEFAULT_NAV_DURATION = 320

    fun anchoredTop() = verticalSlide(enterOffset = { -it }, exitOffset = { it }, duration = DEFAULT_NAV_DURATION)
    fun anchoredBottom() = verticalSlide(enterOffset = { it }, exitOffset = { -it }, duration = DEFAULT_NAV_DURATION)
    fun anchoredRight(keepTransitionOnEnter: Boolean = false) =
        horizontalSlide(
            enterOffset = { it },
            exitOffset = { -it },
            duration = DEFAULT_NAV_DURATION,
            keepTransitionOnEnter = keepTransitionOnEnter
        )

    fun anchoredLeft() = horizontalSlide(enterOffset = { -it }, exitOffset = { it }, duration = DEFAULT_NAV_DURATION)

    val screenTransitionEasing = FastOutSlowInEasing

    fun <T> defaultTween(duration: Int = DEFAULT_NAV_DURATION): TweenSpec<T> =
        tween(duration, easing = screenTransitionEasing)

    fun fade(enterAlpha: Float = 0F, exitAlpha: Float = 0F, duration: Int = 150) =
        NavDisplay.transitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        } + NavDisplay.popTransitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        } + NavDisplay.predictivePopTransitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        }

    fun verticalSlide(
        enterOffset: AnimOffset,
        exitOffset: AnimOffset,
        duration: Int,
        keepTransitionOnEnter: Boolean = false
    ) =
        NavDisplay.transitionSpec {
            slideInVertically(
                initialOffsetY = enterOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            ) togetherWith if (keepTransitionOnEnter) ExitTransition.KeepUntilTransitionsFinished else slideOutVertically(
                targetOffsetY = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        } + NavDisplay.popTransitionSpec {
            slideInVertically(
                initialOffsetY = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            ) togetherWith slideOutVertically(
                targetOffsetY = enterOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        } + NavDisplay.predictivePopTransitionSpec {
            slideInVertically(
                initialOffsetY = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            ) togetherWith slideOutVertically(
                targetOffsetY = enterOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        }

    fun horizontalSlide(
        enterOffset: AnimOffset,
        exitOffset: AnimOffset,
        duration: Int,
        keepTransitionOnEnter: Boolean = false
    ) =
        NavDisplay.transitionSpec {
            slideInHorizontally(
                initialOffsetX = enterOffset,
                animationSpec = defaultTween(duration)
            ) togetherWith if (keepTransitionOnEnter) ExitTransition.KeepUntilTransitionsFinished else slideOutHorizontally(
                targetOffsetX = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        } + NavDisplay.popTransitionSpec {
            slideInHorizontally(
                initialOffsetX = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = enterOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        } + NavDisplay.predictivePopTransitionSpec {
            slideInHorizontally(
                initialOffsetX = exitOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = enterOffset,
                animationSpec = tween(duration, easing = screenTransitionEasing)
            )
        }
}

