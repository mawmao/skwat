package com.humayapp.scout.feature.form.impl.data.repository

import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import jakarta.inject.Inject

interface LocationRepository {
    suspend fun getMunicipalitiesByProvince(provinceName: String?): List<String>
    suspend fun getBarangaysByCityMunicipality(cityMunicipalityName: String?): List<String>
}

class LocationRepositoryImpl @Inject constructor(
    private val provinceDao: ProvinceDao,
    private val cityMunicipalityDao: CityMunicipalityDao,
    private val barangayDao: BarangayDao
) : LocationRepository {

    override suspend fun getMunicipalitiesByProvince(provinceName: String?): List<String> {
        if (provinceName == null) return emptyList()
        val result = cityMunicipalityDao.getCitiesByProvince(provinceName)
            .map { it.name }
        return result
    }

    override suspend fun getBarangaysByCityMunicipality(cityMunicipalityName: String?): List<String> {
        if (cityMunicipalityName == null) return emptyList()
        val result = barangayDao.getBarangaysByCity(cityMunicipalityName)
            .map { it.name }
        return result
    }
}
