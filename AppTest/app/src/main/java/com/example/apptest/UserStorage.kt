package com.example.apptest

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "user_prefs")
private val USER_KEY = stringPreferencesKey("user_data")

object UserStorage {

    fun saveUser(context: Context, user: User) {
        runBlocking {
            val json = Json.encodeToString(user)
            context.dataStore.edit { prefs ->
                prefs[USER_KEY] = json
            }
        }
    }

    fun loadUser(context: Context): User? {
        return runBlocking {
            val prefs = context.dataStore.data.first()
            val json = prefs[USER_KEY] ?: return@runBlocking null
            return@runBlocking Json.decodeFromString<User>(json)
        }
    }
}
