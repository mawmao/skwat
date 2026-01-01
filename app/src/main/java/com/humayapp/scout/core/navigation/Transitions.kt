package com.humayapp.scout.core.navigation

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
    fun anchoredTop() = verticalSlide(enterOffset = { -it }, exitOffset = { it }, duration = 400)
    fun anchoredBottom() = verticalSlide(enterOffset = { it }, exitOffset = { -it }, duration = 400)
    fun anchoredRight() = horizontalSlide(enterOffset = { it }, exitOffset = { -it }, duration = 360)
    fun anchoredLeft() = horizontalSlide(enterOffset = { -it }, exitOffset = { it }, duration = 360)

    fun fade(enterAlpha: Float = 0F, exitAlpha: Float = 0F, duration: Int = 150) =
        NavDisplay.transitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        } + NavDisplay.popTransitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        } + NavDisplay.predictivePopTransitionSpec {
            fadeIn(tween(duration), enterAlpha) togetherWith fadeOut(tween(duration), exitAlpha)
        }

    fun verticalSlide(enterOffset: AnimOffset, exitOffset: AnimOffset, duration: Int) =
        NavDisplay.transitionSpec {
            slideInVertically(
                initialOffsetY = enterOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutVertically(
                targetOffsetY = exitOffset,
                animationSpec = tween(duration)
            )
        } + NavDisplay.popTransitionSpec {
            slideInVertically(
                initialOffsetY = exitOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutVertically(
                targetOffsetY = enterOffset,
                animationSpec = tween(duration)
            )
        } + NavDisplay.predictivePopTransitionSpec {
            slideInVertically(
                initialOffsetY = exitOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutVertically(
                targetOffsetY = enterOffset,
                animationSpec = tween(duration)
            )
        }

    fun horizontalSlide(enterOffset: AnimOffset, exitOffset: AnimOffset, duration: Int = 450) =
        NavDisplay.transitionSpec {
            slideInHorizontally(
                initialOffsetX = enterOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = exitOffset,
                animationSpec = tween(duration)
            )
        } + NavDisplay.popTransitionSpec {
            slideInHorizontally(
                initialOffsetX = exitOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = enterOffset,
                animationSpec = tween(duration)
            )
        } + NavDisplay.predictivePopTransitionSpec {
            slideInHorizontally(
                initialOffsetX = exitOffset,
                animationSpec = tween(duration)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = enterOffset,
                animationSpec = tween(duration)
            )
        }
}

