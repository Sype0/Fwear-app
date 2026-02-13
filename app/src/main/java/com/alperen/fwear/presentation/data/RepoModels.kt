package com.alperen.fwear.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class FDroidIndex(
    @SerializedName("repo") val repo: JsonElement?,
    @SerializedName("apps") val apps: List<FDroidAppDto>
)

data class FDroidAppDto(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("name") val nameElement: JsonElement?,
    @SerializedName("summary") val summaryElement: JsonElement?,
    @SerializedName("description") val descriptionElement: JsonElement?,

    @SerializedName("icon") val iconName: String?,
    @SerializedName("lastUpdated") val lastUpdated: Long,
    @SerializedName("license") val license: String = "Unknown",
    @SerializedName("suggestedVersionCode") val suggestedVersionCode: Long?
) {
    fun getBestName(): String {
        val extractedName = extractTextFrom(nameElement)
        if (!extractedName.isNullOrBlank()) {
            return extractedName
        }
        return packageName
    }

    fun getBestSummary(): String {
        val summary = extractTextFrom(summaryElement)
        if (!summary.isNullOrBlank()) return summary

        val desc = extractTextFrom(descriptionElement)
        if (!desc.isNullOrBlank()) return desc.take(100) + "..."

        return ""
    }
    fun getFullIconUrl(repoUrl: String): String {
        if (iconName.isNullOrEmpty()) return ""
        val cleanBase = repoUrl.trimEnd('/')
        return "$cleanBase/$iconName"
    }

    fun getApkUrl(repoUrl: String): String {
        val code = suggestedVersionCode ?: ""
        val cleanBase = repoUrl.trimEnd('/')
        return "$cleanBase/${packageName}_$code.apk"
    }

    private fun extractTextFrom(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null

        try {
            if (element.isJsonPrimitive) {
                return element.asString
            }

            if (element.isJsonObject) {
                val obj = element.asJsonObject

                val languageKeys = listOf("tr", "tr-TR", "en-US", "en", "default")
                for (key in languageKeys) {
                    if (obj.has(key)) {
                        val value = obj.get(key)
                        if (value.isJsonPrimitive) return value.asString
                    }
                }

                for (entry in obj.entrySet()) {
                    val value = entry.value
                    if (value.isJsonPrimitive) {
                        return value.asString
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}