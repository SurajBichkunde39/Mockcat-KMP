package com.mockcat.persistence

import androidx.room.TypeConverter
import com.mockcat.api.MockType
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class MockTypeConverters {
    @TypeConverter
    fun fromMockType(value: MockType): String = value.name

    @TypeConverter
    fun toMockType(value: String): MockType = MockType.valueOf(value)

    @TypeConverter
    fun fromStringMap(headers: Map<String, String>?): String? {
        if (headers == null) {
            return null
        }
        return Json.encodeToString(
            MapSerializer(
                String.serializer(),
                String.serializer(),
            ),
            headers,
        )
    }

    @TypeConverter
    fun toStringMap(jsonString: String?): Map<String, String>? {
        if (jsonString == null) {
            return null
        }
        return Json.decodeFromString(
            MapSerializer(
                String.serializer(),
                String.serializer(),
            ),
            jsonString,
        )
    }
}
