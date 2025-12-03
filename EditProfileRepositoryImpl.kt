package com.sarang.torang.di.repository

import com.sarang.torang.api.ApiProfile
import com.sarang.torang.data.EditProfileResponse
import com.sarang.torang.repository.EditProfileRepository
import com.sarang.torang.session.SessionClientService
import com.sarang.torang.util.CountingFileRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditProfileRepositoryImpl @Inject constructor(
    private val apiProfile: ApiProfile,
    private val sessionClientService: SessionClientService
) : EditProfileRepository {
    override suspend fun editProfile(
        name: String?,
        uri: String?
    ): EditProfileResponse {
        var response = EditProfileResponse.NO_USER

        val token = sessionClientService.getToken()!!

        val params: HashMap<String, RequestBody> = HashMap()
        params["user_name"] = ("" + name).toRequestBody("text/plain".toMediaTypeOrNull())

        val pictureList = ArrayList<MultipartBody.Part>()
        if (uri != null) {
            val file = File(uri)
            val requestFile = CountingFileRequestBody(file, "image/*", object :
                CountingFileRequestBody.ProgressListener {
                override fun transferred(num: Long) {

                }
            })
            pictureList.add(MultipartBody.Part.createFormData("file", file.name, requestFile))
        }

        // 레트로핏으로 사용자 프로필 업데이트 Rest API 처리
        apiProfile.updateProfile(token, params, pictureList)
        response = EditProfileResponse.SUCCESS

        return response
    }
}