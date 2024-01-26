package android.iot.secret

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class Encryption {
    companion object {
        private val key: ByteArray = arrayOf(
            0x2B.toByte(),
            0x7E.toByte(),
            0x15.toByte(),
            0x16.toByte(),
            0x28.toByte(),
            0xAE.toByte(),
            0xD2.toByte(),
            0xA6.toByte(),
            0xAB.toByte(),
            0xF7.toByte(),
            0x15.toByte(),
            0x88.toByte(),
            0x09.toByte(),
            0xCF.toByte(),
            0x4F.toByte(),
            0x3C.toByte()
        ).toByteArray()
        private val iv: ByteArray = arrayOf(
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte(),
            0xAA.toByte()
        ).toByteArray()

        public fun encrypt(data: String): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")	//	Get correct cipher
            val secretKey = SecretKeySpec(key, "AES")						//	Pass password
            val ivParameterSpec = IvParameterSpec(iv)						        //	Pass IV

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)	        //	Init cipher
            val decryptedData = cipher.doFinal(data.toByteArray())			        //	Encrypt data to binary (not human readable)

            return String(Base64.getEncoder().encode(decryptedData))		        //	Encode binary to Base64 (human readable)
        }


        public fun decrypt(data: String): String {
            val decodedData = Base64.getDecoder().decode(data.replace("\r\n", ""))                      //	Decode Base64 to binary (not human readable)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")    //	Get correct cipher
            val secretKey = SecretKeySpec(key, "AES")                      //	Pass password
            val ivParameterSpec = IvParameterSpec(iv)                               //	Pass IV

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)            //	Init cipher
            val decryptedData = cipher.doFinal(decodedData)                         //	Decrypt data to plain text (human readable)

            return String(decryptedData)
        }
    }
}