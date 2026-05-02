package com.mpcorporation.snapeffect.data.preset

import android.content.Context
import com.mpcorporation.snapeffect.domain.model.UserPreset
import com.mpcorporation.snapeffect.domain.repository.PresetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PresetRepository {

    override suspend fun load(): List<UserPreset> {
        val json = prefs().getString(KEY_PRESETS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        } catch (_: JSONException) {
            emptyList()
        }
    }

    override suspend fun add(preset: UserPreset) {
        val current = load().toMutableList()
        current.add(0, preset)
        save(current)
    }

    override suspend fun delete(index: Int) {
        val current = load().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            save(current)
        }
    }

    private fun save(presets: List<UserPreset>) {
        val arr = JSONArray()
        try {
            presets.take(MAX_PRESETS).forEach { arr.put(toJson(it)) }
        } catch (_: JSONException) {
        }
        prefs().edit().putString(KEY_PRESETS, arr.toString()).apply()
    }

    private fun prefs() =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun toJson(p: UserPreset) = JSONObject().apply {
        put("name", p.name)
        put("brightness", p.brightness.toDouble())
        put("contrast", p.contrast.toDouble())
        put("saturation", p.saturation.toDouble())
        put("hue", p.hue.toDouble())
        put("exposure", p.exposure.toDouble())
        put("whiteBalance", p.whiteBalance.toDouble())
        put("gamma", p.gamma.toDouble())
    }

    private fun fromJson(o: JSONObject) = UserPreset(
        name = o.getString("name"),
        brightness = o.optDouble("brightness", 0.0).toFloat(),
        contrast = o.optDouble("contrast", 1.0).toFloat(),
        saturation = o.optDouble("saturation", 1.0).toFloat(),
        hue = o.optDouble("hue", 0.0).toFloat(),
        exposure = o.optDouble("exposure", 0.0).toFloat(),
        whiteBalance = o.optDouble("whiteBalance", 5000.0).toFloat(),
        gamma = o.optDouble("gamma", 1.0).toFloat()
    )

    private companion object {
        const val PREFS_NAME = "user_presets"
        const val KEY_PRESETS = "presets_json"
        const val MAX_PRESETS = 20
    }
}
