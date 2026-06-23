package com.tcssol.expensetracker.Utils

import org.json.JSONArray
import org.json.JSONObject

class WorkwithJSONStrings(private var jsonString: String) {
    private var jsonObject: JSONObject = try {
        JSONObject(jsonString)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

    fun getList(name: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val jsonArray = jsonObject.getJSONArray(name)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // Return empty list if not found
        }
        return list
    }

    fun updateElementList(id: String, newList: List<String>) {
        try {
            val jsonArray = JSONArray(newList)
            jsonObject.put(id, jsonArray)
            jsonString = jsonObject.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun removeKey(key: String) {
        jsonObject.remove(key)
        jsonString = jsonObject.toString()
    }

    fun getJSONString(): String {
        return jsonObject.toString()
    }
}
