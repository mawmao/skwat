package com.humayapp.scout.feature.form.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.navigation.OverlayType
import com.humayapp.scout.navigation.RootNavKey

fun StackNavigator<NavKey>.navigateToFormScan() = this.push(FormScanNavKey)

fun StackNavigator<NavKey>.navigateToFormScanOverlay(formType: FormType) = this.push(RootNavKey.Overlay(OverlayType.Scan(formType.name)))

fun StackNavigator<NavKey>.navigateToFormConfirm(mfid: String) = this.push(FormConfirmNavKey(mfid))

fun StackNavigator<NavKey>.navigateToFormWizard() = this.push(FormWizardNavKey)

fun StackNavigator<NavKey>.navigateToFormReview() = this.push(FormReviewNavKey)
