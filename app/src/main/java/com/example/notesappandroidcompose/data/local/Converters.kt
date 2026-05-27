package com.example.notesappandroidcompose.data.local

import androidx.room.TypeConverter
import com.example.notesappandroidcompose.domain.model.Attachment
import com.example.notesappandroidcompose.domain.model.AttachmentType
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromAttachmentList(value: List<Attachment>): String {
        val jsonArray = JSONArray()
        value.forEach { attachment ->
            val jsonObject = JSONObject()
            jsonObject.put("uri", attachment.uri)
            jsonObject.put("type", attachment.type.name)
            jsonObject.put("name", attachment.name)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toAttachmentList(value: String): List<Attachment> {
        val list = mutableListOf<Attachment>()
        val jsonArray = JSONArray(value)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(
                Attachment(
                    uri = jsonObject.getString("uri"),
                    type = AttachmentType.valueOf(jsonObject.getString("type")),
                    name = jsonObject.getString("name")
                )
            )
        }
        return list
    }
}
