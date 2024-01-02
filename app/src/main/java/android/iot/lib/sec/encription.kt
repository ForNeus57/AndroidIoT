package android.iot.lib.sec

import java.security.Key
import java.util.Base64
import javax.crypto.Cipher

fun encrypt(input: String, key: Key): String {
    val cipher = Cipher.getInstance("ChaCha20-NONE")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val encryptedBytes = cipher.doFinal(input.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedBytes)
}