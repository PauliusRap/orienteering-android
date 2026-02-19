package com.orienteering.hunt

import android.app.Application
import com.orienteering.hunt.data.api.ApiService
import com.orienteering.hunt.data.api.AuthManager
import com.orienteering.hunt.data.api.KtorApiService
import com.orienteering.hunt.data.repository.HuntRepository
import com.orienteering.hunt.services.LocationService

class HuntApplication : Application() {
    
    lateinit var repository: HuntRepository
        private set
    
    lateinit var locationService: LocationService
        private set
    
    lateinit var apiService: ApiService
        private set
    
    lateinit var authManager: AuthManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        authManager = AuthManager(this)
        apiService = KtorApiService()
        repository = HuntRepository(apiService)
        locationService = LocationService(this)
    }
}
