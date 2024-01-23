package android.iot.secret

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class Encryption {
    private val key: ByteArray = ByteArray(16) // Replace with your key
    private val iv: ByteArray = ByteArray(16)

    public fun encrypt(data: String = "dWlo35QN9eYSk5wEzUQ9fQ=="): String {
        iv[0] = 0xAA.toByte()
        iv[1] = 0xAA.toByte()
        iv[2] = 0xAA.toByte()
        iv[3] = 0xAA.toByte()
        iv[4] = 0xAA.toByte()
        iv[5] = 0xAA.toByte()
        iv[6] = 0xAA.toByte()
        iv[7] = 0xAA.toByte()
        iv[8] = 0xAA.toByte()
        iv[9] = 0xAA.toByte()
        iv[10] = 0xAA.toByte()
        iv[11] = 0xAA.toByte()
        iv[12] = 0xAA.toByte()
        iv[13] = 0xAA.toByte()
        iv[14] = 0xAA.toByte()
        iv[15] = 0xAA.toByte()

        key[0] = 0x2B.toByte()
        key[1] = 0x7E.toByte()
        key[2] = 0x15.toByte()
        key[3] = 0x16.toByte()
        key[4] = 0x28.toByte()
        key[5] = 0xAE.toByte()
        key[6] = 0xD2.toByte()
        key[7] = 0xA6.toByte()
        key[8] = 0xAB.toByte()
        key[9] = 0xF7.toByte()
        key[10] = 0x15.toByte()
        key[11] = 0x88.toByte()
        key[12] = 0x09.toByte()
        key[13] = 0xCF.toByte()
        key[14] = 0x4F.toByte()
        key[15] = 0x3C.toByte()

        val decodedData = Base64.getDecoder().decode(data)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val decryptedData = cipher.doFinal(decodedData)
        return String(decryptedData)
    }

    public fun decrypt(data: String): String {
        return data
    }
}