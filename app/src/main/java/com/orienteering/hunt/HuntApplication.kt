package com.orienteering.hunt

import android.app.Application
import com.orienteering.hunt.data.repository.HuntRepository
import com.orienteering.hunt.services.LocationService

class HuntApplication : Application() {
    
    lateinit var repository: HuntRepository
        private set
    
    lateinit var locationService: LocationService
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        repository = HuntRepository()
        locationService = LocationService(this)
    }
}
