package com.example.workplacetrackingapp.retrofit

import com.example.workplacetrackingapp.model.PushNotification
import com.example.workplacetrackingapp.util.Constant.Companion.CONTENT_TYPE
import com.example.workplacetrackingapp.util.Constant.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("/fcm/send")
    suspend fun postNotification(
        @Body notificationData : PushNotification
    ) : Response<ResponseBody>

}