package com.humayapp.scout.feature.auth.recovery.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator<NavKey>.navigateToRecoveryOtp() = this.push(RecoveryOtpNavKey)
fun StackNavigator<NavKey>.navigateToRecoveryReset() = this.push(RecoveryResetNavKey)
fun StackNavigator<NavKey>.navigateToRecoverySuccess() = this.push(RecoverySuccessNavKey)
