package com.example.sufragio.network

import com.example.sufragio.ui.creation.PollResponse
import com.example.sufragio.ui.detail.PollOptionResponse
import com.example.sufragio.ui.detail.UserInfoResponse
import com.example.sufragio.ui.list.PollResponseL
import com.example.sufragio.ui.login_register.LoginResponse
import com.example.sufragio.ui.mypage.UserResponse
import com.example.sufragio.ui.mypage.VoteResponse
import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("signup")
    suspend fun registerUser(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("nickname") nickname: String
    ): Response<Void>

    @POST("signin")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<LoginResponse>

    @GET("user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST("refresh-token")
    suspend fun refreshAccessToken(
        @Query("refresh_token") refreshToken: String
    ): Response<AccessTokenResponse>


    @PATCH("user")
    suspend fun updateNickname(
        @Query("nickname") newNickname: String,
        @Header("Authorization") accessToken: String
    ): Response<Void>

    @POST("polls/")
    suspend fun createPoll(
        @Header("Authorization") authHeader: String,
        @Query("title") title: String,
        @Query("description") description: String,
        @Query("is_anonymous") isAnonymous: Boolean,
        @Query("is_multiple_choice") isMultipleChoice: Boolean,
        @Query("is_option_add_allowed") isOptionAddAllowed: Boolean,
        @Query("is_revoting_allowed") isRevotingAllowed: Boolean,
        @Body options: List<String>
    ): Response<PollResponse>

    @GET("polls/")
    suspend fun getAllPolls(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1000000
    ): Response<List<PollResponseL>>

    @DELETE("polls/{id}/")
    suspend fun deletePoll(
        @Path("id") pollId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("polls/{poll_id}/vote/")
    suspend fun getVotesForPoll(
        @Path("poll_id") pollId: String,
        @Header("Authorization") authHeader: String
    ): Response<VoteResponse>

    @GET("polls/{poll_id}/results/")
    suspend fun getPollResults(
        @Path("poll_id") pollId: String
    ): Response<ResponseBody>

    @GET("polls/{poll_id}/options/")
    suspend fun getPollOptions(
        @Path("poll_id") pollId: String
    ): Response<List<PollOptionResponse>>

    @POST("polls/{poll_id}/vote/")
    suspend fun votePoll(
        @Path("poll_id") pollId: String,
        @Body optionIds: List<String>,
        @Header("Authorization") token: String
    ): Response<Any>

    @GET("user/{user_id}")
    suspend fun getUserNickname(
        @Path("user_id") userId: String,
        @Header("Authorization") token: String
    ): Response<UserInfoResponse>

    @POST("polls/{poll_id}/options/")
    suspend fun addPollOption(
        @Path("poll_id") pollId: String,
        @Query("option_text") optionText: String,
        @Header("Authorization") token: String
    ): Response<Void>

}