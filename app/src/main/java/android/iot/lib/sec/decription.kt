package android.iot.lib.sec

import java.security.Key
import java.util.Base64
import javax.crypto.Cipher

fun decrypt(input: String, key: Key): String {
    val cipher = Cipher.getInstance("ChaCha20-NONE")
    cipher.init(Cipher.DECRYPT_MODE, key)
    val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(input))
    return String(decryptedBytes)
}