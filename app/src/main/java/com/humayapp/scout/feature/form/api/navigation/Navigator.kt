package com.humayapp.scout.feature.form.api.navigation

import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator.navigateToFormScan() = this.push(FormScanNavKey)

fun StackNavigator.navigateToFormConfirm() = this.push(FormConfirmNavKey)

fun StackNavigator.navigateToFormWizard() = this.push(FormWizardNavKey)

fun StackNavigator.navigateToFormReview() = this.push(FormReviewNavKey)
