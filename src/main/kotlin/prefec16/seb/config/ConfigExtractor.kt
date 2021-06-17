package prefec16.seb.config

import com.dd.plist.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.ByteArrayInputStream
import java.io.InputStream


object ConfigExtractor {

    fun parse(inputStream: InputStream): JsonObject {
        val rootDict = inputStream.use {
             PropertyListParser.parse(it) as NSDictionary
        }

        val rootObj = JsonObject()
        rootDict.allKeys().sortedBy { it.toLowerCase() }.forEach { key ->
            rootDict[key]?.let { obj ->
                (parseObject(key, obj) as JsonObject).entrySet().forEach { (key, elem) ->
                    rootObj.add(key, elem)
                }
            }

        }

        return rootObj
    }

    fun parse(inStr: String): JsonObject {
        return parse(ByteArrayInputStream(inStr.toByteArray()))
    }

    fun addOrReturn(key: String?, jsonElement: JsonElement, addTo: JsonObject): JsonElement? {
        return if (key != null) {
            addTo.add(key, jsonElement)
            null
        } else jsonElement
    }

    private fun parseObject(key: String?, obj: NSObject): JsonElement {
        val jsonObject = JsonObject()
        when (obj) {
            is NSArray -> {
                val array = JsonArray()
                obj.array.forEach { childObj ->
                    array.add(parseObject(null, childObj))
                }

                addOrReturn(key, array, jsonObject)?.let {
                    return it
                }
            }
            is NSDictionary -> {
                val dictObj = JsonObject()
                obj.hashMap.toSortedMap(compareBy { it.toLowerCase() }).forEach { (key, childObj) ->
                    dictObj.add(key, parseObject(null, childObj))
                }

                addOrReturn(key, dictObj, jsonObject)?.let {
                    return it
                }
            }
            is NSString -> {
                addOrReturn(key, JsonPrimitive(obj.content), jsonObject)?.let {
                    return it
                }
            }
            is NSNumber -> {
                val prim = when (obj.type()) {
                    NSNumber.INTEGER -> {
                        JsonPrimitive(obj.intValue())
                    }
                    NSNumber.BOOLEAN -> {
                        JsonPrimitive(obj.boolValue())
                    }
                    else -> error("Type not implemented yet")
                }

                addOrReturn(key, prim, jsonObject)?.let {
                    return it
                }
            }

            is NSData -> {
                addOrReturn(key, JsonPrimitive(obj.base64EncodedData), jsonObject)?.let {
                    return it
                }
            }
        }

        return jsonObject
    }
}