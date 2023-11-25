package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.data.dao.FeedDao
import com.sryang.torang_repository.api.ApiReport
import com.sryang.torang_repository.repository.ReportAfterSupport
import com.sryang.torang_repository.repository.ReportReason
import com.sryang.torang_repository.repository.ReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportService: ApiReport,
    private val feedDao: FeedDao
) : ReportRepository {

    override suspend fun sendReportReason(reportReason: ReportReason, reviewId: Int): Boolean {
        val map = HashMap<String, String>().apply {
            put("reportReason", reportReason.name)
            put("reviewId", reviewId.toString())
        }

        if (reportService.reportReason(reviewId = reviewId, reason = reportReason.name)) {
            //신고 성공 후 해당 피드 로컬 데이터에서 삭제하기
            feedDao.deleteFeed(reviewId)
            return true
        }
        return false
    }

    override suspend fun sendReportAfterSupport(
        reportAfterSupport: ReportAfterSupport,
        reviewId: Int
    ): Boolean {
        return true
    }

    override suspend fun hasFeed(reviewId: Int): Boolean {
        return try {
            reportService.hasFeed(reviewId = reviewId)
        } catch (e: Exception) {
            false
        }
        return false
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportRepositoryModule {
    @Binds
    abstract fun provideReportRepository(reportRepositoryImpl: ReportRepositoryImpl): ReportRepository
}