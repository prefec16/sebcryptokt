package prefec16.seb.crypto

import com.google.gson.JsonObject
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object Crypto {
    private const val SEB_SIGNATURE = "E89799F0033C61C5366D1C8CB4EC5852A864A530"
    private const val SEB_VERSION = "3.1.1.250"
    private const val HMAC_SHA256 = "HmacSHA256"

    private val MD_SHA_256: MessageDigest = MessageDigest.getInstance("SHA-256")

    fun computeConfigurationKey(obj: JsonObject): String {
        return toHexString(MD_SHA_256.digest(obj.toString().toByteArray()))
    }

    fun computeConfigKeyHash(url: String, configurationKey: String): String {
        return toHexString(MD_SHA_256.digest((cleanUrl(url) + configurationKey).toByteArray()))
    }

    fun computeRequestHash(url: String, browserExamKey: String): String {
        return toHexString(MD_SHA_256.digest((cleanUrl(url) + browserExamKey).toByteArray()))
    }

    @ExperimentalUnsignedTypes
    fun computeBrowserExamKey(configurationKey: String, examKeySalt: String? = null, signature: String = SEB_SIGNATURE, version: String = SEB_VERSION): String {
        val salt = if (examKeySalt == null) UByteArray(0) else Base64.getDecoder().decode(examKeySalt).toUByteArray()
        return hmacSha256(SEB_SIGNATURE + SEB_VERSION + configurationKey, salt)
    }

    @ExperimentalUnsignedTypes
    fun hmacSha256(data: String, salt: UByteArray): String {
        val key = if (salt.isEmpty()) EmptyKey else SecretKeySpec(salt.toByteArray(), HMAC_SHA256)

        val mac = Mac.getInstance(HMAC_SHA256).apply {
            this.init(key)
        }

        return toHexString(mac.doFinal(data.toByteArray()))
    }

    private fun cleanUrl(url: String): String {
        return if (url.contains("#")) {
            url.substring(0, url.indexOf("#"))
        } else url
    }

    private fun toHexString(bytes: ByteArray): String {
        return buildString {
            bytes.forEach {
                this.append("%02x".format(it))
            }
        }
    }
}