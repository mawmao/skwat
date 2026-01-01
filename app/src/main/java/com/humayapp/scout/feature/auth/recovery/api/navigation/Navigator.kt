package com.humayapp.scout.feature.auth.recovery.api.navigation

import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator.navigateToRecoveryOtp() = this.push(RecoveryOtpNavKey)
fun StackNavigator.navigateToRecoveryReset() = this.push(RecoveryResetNavKey)
fun StackNavigator.navigateToRecoverySuccess() = this.push(RecoverySuccessNavKey)
