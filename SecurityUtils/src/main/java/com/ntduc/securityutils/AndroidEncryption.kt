package com.ntduc.securityutils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.security.cert.CertificateException

@RequiresApi(Build.VERSION_CODES.M)
object AndroidEncryption {
    private var keyStore: KeyStore? = null

    private var keyAlias = "DEFAULT_ALIAS"
    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val FIXED_IV = "abcdxyz"                                //(Lớp mã hoá bổ sung)
    private val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES         //Thuật toán mã hoá AES
    private val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC           //Chế độ mã hoá CBC
    private val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    fun setKeyAlias(keyAlias: String) {
        this.keyAlias = keyAlias
    }

    fun isKeyAliasExists(): Boolean {
        return try {
            if (keyStore == null) {
                keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                keyStore!!.load(null)
            }
            keyStore!!.containsAlias(keyAlias)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generateKey(context: Context) {
        try {
            if (keyStore == null) {
                keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                keyStore!!.load(null)
            }

            if (!isKeyAliasExists()) {
                Log.d("ntduc_debug", "generateKey: ")
                val keyGenerator =
                    KeyGenerator.getInstance(ALGORITHM, ANDROID_KEY_STORE)
                val build = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setRandomizedEncryptionRequired(false)
                    .build()
                keyGenerator.init(build)
                keyGenerator.generateKey()
            }
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encrypt(text: String): String {
        try {
            return encryptString(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun encrypt(input: String, outputStream: OutputStream): String? {
        try {
            return encryptStream(input, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun decrypt(text: String): String {
        try {
            val bytes: ByteArray? = decryptString(text)
            return bytes?.let { String(it, Charsets.UTF_8) }.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun decrypt(inputStream: InputStream): String? {
        try {
            val bytes: ByteArray? = decryptStream(inputStream)
            return bytes?.let { String(it, Charsets.UTF_8) }.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(Exception::class)
    private fun getSecretKey(): Key {
        return keyStore!!.getKey(keyAlias, null)
    }

    private fun encryptString(input: String): String {
        val encryptCipher: Cipher?
        try {
            encryptCipher = Cipher.getInstance(TRANSFORMATION)
            encryptCipher!!.init(Cipher.ENCRYPT_MODE, getSecretKey(), IvParameterSpec(FIXED_IV.toByteArray()))
            val encodedBytes = encryptCipher.doFinal(input.toByteArray())
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun decryptString(encrypted: String): ByteArray? {
        val decryptCipher: Cipher?
        try {
            decryptCipher = Cipher.getInstance(ALGORITHM)
            decryptCipher!!.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(FIXED_IV.toByteArray()))
            val barr = Base64.decode(encrypted, Base64.DEFAULT)
            return decryptCipher.doFinal(barr)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun encryptStream(input: String, outputStream: OutputStream): String? {
        val encryptCipher: Cipher?
        try {
            encryptCipher = Cipher.getInstance(TRANSFORMATION)
            encryptCipher!!.init(
                Cipher.ENCRYPT_MODE,
                getSecretKey(),
                IvParameterSpec(FIXED_IV.toByteArray())
            )
            val encodedBytes = encryptCipher.doFinal(input.toByteArray())
            outputStream.use {
                it.write(encryptCipher.iv.size)
                it.write(encryptCipher.iv)
                it.write(encodedBytes.size)
                it.write(encodedBytes)
            }
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun decryptStream(inputStream: InputStream): ByteArray? {
        val decryptCipher: Cipher?
        try {
            decryptCipher = Cipher.getInstance(ALGORITHM)
            decryptCipher!!.init(
                Cipher.DECRYPT_MODE,
                getSecretKey(),
                IvParameterSpec(FIXED_IV.toByteArray())
            )
            inputStream.use {
                val ivSize = it.read()
                val iv = ByteArray(ivSize)
                it.read(iv)

                val encryptedBytesSize = it.read()
                val encryptedBytes = ByteArray(encryptedBytesSize)
                it.read(encryptedBytes)

                val barr = Base64.decode(encryptedBytes, Base64.DEFAULT)
                return decryptCipher.doFinal(barr)
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}