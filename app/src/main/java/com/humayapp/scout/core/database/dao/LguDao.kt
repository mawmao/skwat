package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.humayapp.scout.core.database.model.BarangayEntity
import com.humayapp.scout.core.database.model.CityMunicipalityEntity
import com.humayapp.scout.core.database.model.ProvinceEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ProvinceDao {
    @Query("SELECT * FROM provinces")
    fun getAllProvinces(): Flow<List<ProvinceEntity>>

    @Query("SELECT * FROM provinces WHERE id = :id")
    suspend fun getProvinceById(id: Int): ProvinceEntity?
}

@Dao
interface CityMunicipalityDao {

    @Query("SELECT * FROM cities_municipalities")
    fun getAllCities(): Flow<List<CityMunicipalityEntity>>

    @Query(
        """
        SELECT cm.* 
        FROM cities_municipalities cm
        JOIN provinces p ON cm.province_id = p.id
        WHERE p.name = :provinceName
    """
    )
    suspend fun getCitiesByProvince(provinceName: String): List<CityMunicipalityEntity>

    @Query("""
        SELECT *
        FROM cities_municipalities 
        WHERE code = :code
    """)
    suspend fun getCitiesByCode(code: String): CityMunicipalityEntity?
}


@Dao
interface BarangayDao {

    @Query("SELECT * FROM barangays")
    fun getAllBarangays(): Flow<List<BarangayEntity>>

    @Query(
        """
        SELECT b.* 
        FROM barangays b
        JOIN cities_municipalities cm ON b.city_municipality_id = cm.id
        WHERE cm.name = :cityName
    """
    )
    suspend fun getBarangaysByCity(cityName: String): List<BarangayEntity>

}
