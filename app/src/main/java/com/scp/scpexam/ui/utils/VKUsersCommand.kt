package com.scp.scpexam.ui.utils

import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKUsersCommand() : ApiCommand<List<VKUser>>() {

    override fun onExecute(manager: VKApiManager): List<VKUser> {
        val call = VKMethodCall.Builder()
            .method("users.get")
            .args("fields", "photo_200")
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private class ResponseApiParser : VKApiJSONResponseParser<List<VKUser>> {
        override fun parse(responseJson: JSONObject): List<VKUser> {
            try {
                val jsonArray = responseJson.getJSONArray("response")
                val r = ArrayList<VKUser>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val user = VKUser(
                        firstName = jsonArray.getJSONObject(i).getString("first_name"),
                        lastName = jsonArray.getJSONObject(i).getString("last_name"),
                        fullName = jsonArray.getJSONObject(i).getString("first_name")
                                + " "
                                + jsonArray.getJSONObject(i).getString("last_name"),
                        avatarUrl = jsonArray.getJSONObject(i).getString("photo_200")
                    )
                    r.add(user)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

}

class VKUser(
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val avatarUrl: String
)