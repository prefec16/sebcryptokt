package prefec16.seb.crypto

import javax.crypto.SecretKey

//taken from https://stackoverflow.com/questions/56969153/can-the-key-be-empty-while-generating-hmacsha256-using-java
object EmptyKey : SecretKey {
    override fun getAlgorithm(): String {
        return "HMAC"
    }

    override fun getFormat(): String {
        return "RAW"
    }

    override fun getEncoded(): ByteArray {
        // return empty key data
        return ByteArray(0)
    }
}