package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ClientUtils {
    private const val CLIENT_ID = "client_id"
    private const val SAMPLE_ID = "sample_id"
    private const val BAIDU_ID = "baidu_id"

    private val clientIdKey = stringPreferencesKey(CLIENT_ID)
    private val sampleIdKey = stringPreferencesKey(SAMPLE_ID)
    private val baiduIdKey = stringPreferencesKey(BAIDU_ID)

    var clientId: String? = null
    var sampleId: String? = null
    var baiduId: String? = null

    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            clientId = withContext(Dispatchers.IO) {
                context.dataStore.data.map { it[clientIdKey] }.firstOrNull()
            }
            sampleId = withContext(Dispatchers.IO) {
                context.dataStore.data.map { it[sampleIdKey] }.firstOrNull()
            }
            baiduId = withContext(Dispatchers.IO) {
                context.dataStore.data.map { it[baiduIdKey] }.firstOrNull()
            }
            sync(context)
        }
    }

    suspend fun saveBaiduId(context: Context, id: String) {
        baiduId = id
        context.dataStore.edit {
            it[baiduIdKey] = id
        }
    }

    private suspend fun save(context: Context, clientId: String, sampleId: String) {
        context.dataStore.edit {
            it[clientIdKey] = clientId
            it[sampleIdKey] = sampleId
        }
    }

    private suspend fun sync(context: Context) {
        TiebaApi.getInstance()
            .syncFlow(clientId)
            .catch { it.printStackTrace() }
            .collect {
                clientId = it.client.clientId
                sampleId = it.wlConfig.sampleId
                save(context, it.client.clientId, it.wlConfig.sampleId)
            }
    }
}