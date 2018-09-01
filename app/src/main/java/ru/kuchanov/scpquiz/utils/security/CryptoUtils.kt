package ru.kuchanov.scpquiz.utils.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.util.Base64
import timber.log.Timber
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource


@RequiresApi(Build.VERSION_CODES.M)
object CryptoUtils {

    private const val KEY_STORE_NAME = "AndroidKeyStore"
    private const val KEY_ALIAS = "SCP_QUIZ_KEY_ALIAS"
    private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val MD_NAME = "SHA-256"
    private const val MGF_NAME = "MGF1"

    private val keyStore: KeyStore? by lazy {
        try {
            val keyStore = KeyStore.getInstance(KEY_STORE_NAME)
            keyStore.load(null)
            keyStore
        } catch (e: KeyStoreException) {
            Timber.e(e)
            null
        } catch (e: IOException) {
            Timber.e(e)
            null
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
            null
        } catch (e: CertificateException) {
            Timber.e(e)
            null
        }
    }

    private val keyPairGenerator: KeyPairGenerator? by lazy {
        try {
            KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                KEY_STORE_NAME)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
            null
        } catch (e: NoSuchProviderException) {
            Timber.e(e)
            null
        }
    }

    private val cipher: Cipher? by lazy {
        try {
            Cipher.getInstance(TRANSFORMATION)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
            null
        } catch (e: NoSuchPaddingException) {
            Timber.e(e)
            null
        }
    }

    private fun generateNewKey(): Boolean {
        keyPairGenerator?.let {
            try {
                it.initialize(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setUserAuthenticationRequired(true)
                            .build())
                it.generateKeyPair()
                return true
            } catch (e: InvalidAlgorithmParameterException) {
                Timber.e(e)
            }
        }
        return false
    }

    private fun isKeyReady(): Boolean {
        keyStore?.let {
            try {
                return it.containsAlias(KEY_ALIAS) || generateNewKey()
            } catch (e: KeyStoreException) {
                Timber.e(e)
            }
        }
        return false
    }

    private fun initCipher(mode: Int): Boolean {
        keyStore?.let {
            try {
                it.load(null)
                when (mode) {
                    Cipher.ENCRYPT_MODE -> initEncodeCipher(mode)
                    Cipher.DECRYPT_MODE -> initDecodeCipher(mode)
                    else -> return false //this cipher is only for encode\decode
                }
                return true
            } catch (exception: KeyPermanentlyInvalidatedException) {
                deleteInvalidKey()
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: UnrecoverableKeyException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
        }

        return false
    }

    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class,
        InvalidKeyException::class)
    private fun initDecodeCipher(mode: Int) {
        val key = keyStore?.getKey(KEY_ALIAS, null) as PrivateKey
        cipher?.init(mode, key)

    }

    @Throws(
        KeyStoreException::class,
        InvalidKeySpecException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class)
    private fun initEncodeCipher(mode: Int) {
        val key = keyStore?.getCertificate(KEY_ALIAS)?.publicKey
        key?.let {
            val unrestricted = KeyFactory.getInstance(key.algorithm).generatePublic(X509EncodedKeySpec(key.encoded))
            val spec = OAEPParameterSpec(
                MD_NAME,
                MGF_NAME, MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
            cipher?.init(mode, unrestricted, spec)
        }
    }

    private fun deleteInvalidKey() {
        keyStore?.let {
            try {
                it.deleteEntry(KEY_ALIAS)
            } catch (e: KeyStoreException) {
                Timber.e(e)
            }
        }
    }

    private fun prepare(): Boolean {
        return keyStore != null && cipher != null && isKeyReady()
    }

    fun encode(inputString: String): String? {
        try {
            if (prepare() && initCipher(
                        Cipher.ENCRYPT_MODE)) {
                val bytes = cipher?.doFinal(inputString.toByteArray())
                return bytes?.let { Base64.encodeToString(bytes, Base64.NO_WRAP) }
            }
        } catch (e: IllegalBlockSizeException) {
            Timber.e(e)
        } catch (e: BadPaddingException) {
            Timber.e(e)
        }

        return null
    }

    fun decode(encodedString: String, cipherDecrypter: Cipher): String? {
        try {
            val bytes = Base64.decode(encodedString, Base64.NO_WRAP)
            return String(cipherDecrypter.doFinal(bytes))
        } catch (e: IllegalBlockSizeException) {
            Timber.e(e)
        } catch (e: BadPaddingException) {
            Timber.e(e)
        }

        return null
    }

    fun getCryptoObject() = if (prepare() && initCipher(Cipher.DECRYPT_MODE)) {
        cipher?.let { FingerprintManagerCompat.CryptoObject(it) }
    } else null
}