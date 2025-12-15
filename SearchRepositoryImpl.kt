package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.core.database.dao.SearchDao
import com.sarang.torang.core.database.model.search.SearchEntity
import com.sarang.torang.data.Search
import com.sarang.torang.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val searchDao: SearchDao
) :
    SearchRepository {
    override fun getHistoryKeywords(): Flow<List<Search>> {
        return searchDao.getHistoryKeywords().map { it.map { Search.from(it) } }
    }

    override suspend fun saveHistory(keyword: String) {
        val search = SearchEntity(keyword = keyword)
        searchDao.insertAll(search)
    }

    override suspend fun removeKeyword(search: Search) {
        searchDao.delete(search.entity)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchRepositoryModule {
    @Binds
    abstract fun provideSearchRepositoryImpl(searchRepositoryImpl: SearchRepositoryImpl): SearchRepository
}
