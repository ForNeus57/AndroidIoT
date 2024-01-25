package android.iot.secret

import java.security.MessageDigest

class SHA256 {

    companion object {
        public fun getHash(value: String): String {
            val bytes = value.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }.toString()
        }
    }
}