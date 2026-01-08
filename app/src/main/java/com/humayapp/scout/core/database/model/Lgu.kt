package com.humayapp.scout.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "provinces")
data class ProvinceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val name: String
)

@Entity(
    tableName = "cities_municipalities",
    foreignKeys = [
        ForeignKey(
            entity = ProvinceEntity::class,
            parentColumns = ["id"],
            childColumns = ["province_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["province_id"])]
)
data class CityMunicipalityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "province_id") val provinceId: Int,
    val code: String,
    val name: String
)

@Entity(
    tableName = "barangays",
    foreignKeys = [
        ForeignKey(
            entity = CityMunicipalityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_municipality_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["city_municipality_id"])]
)
data class BarangayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "city_municipality_id") val cityMunicipalityId: Int,
    val code: String,
    val name: String
)
