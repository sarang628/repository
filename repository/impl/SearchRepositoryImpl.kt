package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import com.sryang.torang_repository.data.dao.SearchDao
import com.sryang.torang_repository.data.entity.SearchEntity
import com.sryang.torang_repository.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val searchDao: SearchDao
) :
    SearchRepository {
    override fun getHistoryKeywords(): Flow<List<SearchEntity>> {
        return searchDao.getHistoryKeywords()
    }

    override suspend fun saveHistory(keyword: String) {
        val search = SearchEntity(keyword = keyword)
        searchDao.insertAll(search)
    }

    override suspend fun removeKeyword(search: SearchEntity) {
        searchDao.delete(search)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchRepositoryModule {
    @Binds
    abstract fun provideSearchRepositoryImpl(searchRepositoryImpl: SearchRepositoryImpl): SearchRepository
}
