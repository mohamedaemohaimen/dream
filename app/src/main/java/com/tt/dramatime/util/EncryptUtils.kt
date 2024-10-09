package com.tt.dramatime.util

import android.annotation.SuppressLint
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.EncryptUtils
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/7 下午4:16
 *   Desc : 加解密工具类
 * </pre>
 */
object EncryptUtils {

    // 公私钥 非对称算法的公私钥 RSA
    private const val PUBLIC_KEY =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCiDq3RYS0Ltvb1izvDcSx1MmimNn2LqmJHAwZT5G4Lctcp9OJGus+l7oZXLsya2jMLyyIK9ylXC7Vm/o0zLgiHhXPEkL+Z5lWgnBFCDCFGU9wW10pRdBa+4Gh6/Rhl/Kx+qhkBcB3pyiaPvY/qAgeX/fhl1MvFiVavl7ceOj+eLQIDAQAB"
    private const val PRIVATE_KEY =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKIOrdFhLQu29vWLO8NxLHUyaKY2fYuqYkcDBlPkbgty1yn04ka6z6XuhlcuzJraMwvLIgr3KVcLtWb+jTMuCIeFc8SQv5nmVaCcEUIMIUZT3BbXSlF0Fr7gaHr9GGX8rH6qGQFwHenKJo+9j+oCB5f9+GXUy8WJVq+Xtx46P54tAgMBAAECf1nIyC8OJ7wrIlKSprR21KrEbYuo+jrnAIwB9lTZuh5IV0JnlPXwNMXki4lsju88Sgu+qfMT/Ki9U+48GalsAkzbn70WjNLcam5cXjfDEwJcbffK6aTL6Tw9jjAILH09hn2cJuSTNu5I5QxAZQ8VbnwHXSd6iNYROuybylxJBi8CQQDZeRBxl4Ip3bpkLOOxklxEyy5IPmu+cdGpEviBIPwJptiBWLcggWvHGnZrSZDMxT6Kz+AMVA6ay6y+Nt1pNQEDAkEAvsRhnUgjoPZRtuaiGtu8c1m3H6gjmiujPbpqQsJuUNo26VIvIMo06crSXTU+LEBr3yBe9dod7h9U9uUVqYqFDwJBAI22WELNKeLCO/2fStihuUKS7BvjoS4+2RLF6+2pYtOfR6Ix/1NNQsBBvDz5eUQWnES0ZRljhQjWBEEHW4x9H6UCQQCM2WpJOm4z9io3JOojcoIdmOwAT6CZwAZzGSRDzlKtF7DbbHaneyS+SaYwc5NIEPiUcc4rL7RRkIinElWALGo5AkBGpPujMZHGGGiOgL316cJ6M99Gz+bswnZNUn4OkUYEeCum4aTMlVd8Q+O9225E9YjVWD+KNGBwf/08uUkjPaFH"

    /**RSA 解密*/
    fun decryptRSA(encryptedText: String?): ByteArray {
        //Logger.d("decryptRSA: $encryptedText")
        try {
            val decryptedText = EncryptUtils.decryptRSA(
                EncodeUtils.base64Decode(encryptedText),
                EncodeUtils.base64Decode(PRIVATE_KEY),
                1024,
                "RSA/ECB/PKCS1Padding"
            )
            return EncodeUtils.base64Decode(String(decryptedText))
        } catch (e: Exception) {
            e.printStackTrace()
            return "decryptRSA Failed: ${e.message}".toByteArray()
        }
    }

    /**RSA 加密*/
    fun encryptRSA(randomKey: ByteArray): ByteArray {
        //Logger.d("encryptRSA.randomKey: ${String(randomKey)}")
        try {
            val encryptText = EncryptUtils.encryptRSA(
                randomKey, EncodeUtils.base64Decode(PUBLIC_KEY), 1024, "RSA/ECB/PKCS1Padding"
            )
            //Logger.d("encryptRSA.encryptText: ${String(EncodeUtils.base64Encode(encryptText))}")
            return EncodeUtils.base64Encode(encryptText)
        } catch (e: Exception) {
            e.printStackTrace()
            return "encryptRSA Failed: ${e.message}".toByteArray()
        }
    }

    /**AES 解密*/
    fun decryptAES(data: ByteArray, key: ByteArray): String {
        //Logger.d("decryptAES.data: ${String(data)} decryptAES.key: ${String(key)}")
        try {
            val decryptedText = EncryptUtils.decryptAES(
                EncodeUtils.base64Decode(data), key, "AES/ECB/PKCS5Padding", null
            )
            // 将解密后的数据转换为字符串
            return String(decryptedText)
        } catch (e: Exception) {
            //Logger.e("AES解密失败: ${e.message}")
            e.printStackTrace()
            return "decryptAES Failed: ${e.message}"
        }
    }


    /**AES 加密*/
    @SuppressLint("GetInstance")
    fun encryptAES(data: String?, key: String): String {
        try {
            //Logger.e("encryptAES.data：$data key：${key}" )

            val keySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedText = cipher.doFinal(data?.toByteArray(Charsets.UTF_8))

            //Logger.d("encryptAES.encryptedText：${EncodeUtils.base64Encode(encryptedText)}" )
            // 将加密后的数据转换为字符串
            return String(EncodeUtils.base64Encode(encryptedText))
        } catch (e: Exception) {
            e.printStackTrace()
            return "encryptAES Failed: ${e.message}"
        }
    }

    /**生成随机数*/
    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            val randomIndex = random.nextInt(chars.length)
            sb.append(chars[randomIndex])
        }
        return sb.toString()
    }

}