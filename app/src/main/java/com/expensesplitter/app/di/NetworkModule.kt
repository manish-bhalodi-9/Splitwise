package com.expensesplitter.app.di

import android.content.Context
import com.expensesplitter.app.data.remote.GoogleApiClient
import com.expensesplitter.app.data.remote.GoogleDriveService
import com.expensesplitter.app.data.remote.GoogleSheetsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGoogleApiClient(
        @ApplicationContext context: Context
    ): GoogleApiClient {
        return GoogleApiClient(context)
    }
    
    @Provides
    @Singleton
    fun provideGoogleSheetsService(
        googleApiClient: GoogleApiClient
    ): GoogleSheetsService {
        return GoogleSheetsService(googleApiClient)
    }
    
    @Provides
    @Singleton
    fun provideGoogleDriveService(
        googleApiClient: GoogleApiClient
    ): GoogleDriveService {
        return GoogleDriveService(googleApiClient)
    }
}
