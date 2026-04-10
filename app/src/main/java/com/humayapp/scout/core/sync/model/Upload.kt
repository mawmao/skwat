package com.humayapp.scout.core.sync.model

import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.model.TaskWithFormRelation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class UploadDataRequest(
    val mfid: String,

    @SerialName("collection_task_id")
    val collectionTaskId: Int,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("collected_by")
    val collectedBy: String,

    @SerialName("collected_at")
    val collectedAt: String,

    @SerialName("synced_at")
    val syncedAt: String?,

    @SerialName("image_urls")
    val imageUrls: List<String>,

    val payload: JsonElement
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun fromQueue(task: TaskWithFormRelation, imageUrls: List<String>, payload: String?): UploadDataRequest =
            UploadDataRequest(
                mfid = task.task.mfid,
                collectionTaskId = task.task.id,
                activityType = task.task.activityType,
                seasonId = task.task.seasonId,
                collectedBy = task.task.collectedBy.toString(),
                collectedAt = task.task.collectedAt.toString(),
                syncedAt = Clock.System.now().toString(),
                imageUrls = imageUrls,
                payload = Json.parseToJsonElement(payload ?: unreachable("payload in this context must be available"))
            )
    }
}
