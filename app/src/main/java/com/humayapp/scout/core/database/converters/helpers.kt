package com.humayapp.scout.core.database.converters

import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.network.CollectionTask
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun CollectionTask.toEntity(synced: Boolean = true) = CollectionTaskEntity(
    id = id,
    activityId = activityId,
    retakeOf = retakeOf,
    collectorId = collectorId.toString(),
    seasonId = seasonId,
    mfidId = mfidId,
    activityType = activityType,
    startDate = startDate,
    endDate = endDate,
    canRetake = canRetake,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    assignedAt = assignedAt,
    mfid = mfid,
    collectorName = collectorName,
    farmerName = farmerName,
    province = province,
    cityMunicipality = cityMunicipality,
    barangay = barangay,
    fullAddress = fullAddress,
    isOverdue = isOverdue,
    verificationStatus = verificationStatus,
    collectedBy = collectedBy?.toString(),
    collectedAt = collectedAt,
    verifiedBy = verifiedBy?.toString(),
    verifiedAt = verifiedAt,
    remarks = remarks,
    imageUrls = imageUrls,
    synced = synced,
    dependencyData = dependencyData?.toString(),
)

@OptIn(ExperimentalUuidApi::class)
fun CollectionTaskEntity.toDomain() = CollectionTask(
    id = id,
    activityId = activityId,
    collectorId = Uuid.parse(collectorId),
    seasonId = seasonId,
    mfidId = mfidId,
    activityType = activityType,
    startDate = startDate,
    endDate = endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    assignedAt = assignedAt,
    mfid = mfid,
    retakeOf = retakeOf,
    canRetake = canRetake,
    collectorName = collectorName,
    farmerName = farmerName,
    province = province,
    cityMunicipality = cityMunicipality,
    barangay = barangay,
    fullAddress = fullAddress,
    isOverdue = isOverdue,
    verificationStatus = verificationStatus,
    collectedBy = collectedBy?.let { Uuid.parse(it) },
    collectedAt = collectedAt,
    verifiedBy = verifiedBy?.let { Uuid.parse(it) },
    verifiedAt = verifiedAt,
    remarks = remarks,
    imageUrls = imageUrls,
    dependencyData = dependencyData?.let { Json.decodeFromString(it) }
)
