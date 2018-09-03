package ru.kuchanov.scpquiz.utils.security

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
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
        Timber.d("init keyStore")
        try {
            val keyStore = KeyStore.getInstance(KEY_STORE_NAME)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateNewKey()
            }

            return@lazy keyStore
        } catch (e: KeyStoreException) {
            Timber.e(e)
        } catch (e: IOException) {
            Timber.e(e)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: CertificateException) {
            Timber.e(e)
        } catch (e: InvalidAlgorithmParameterException) {
            Timber.e(e)
        }
        null
    }

    //todo up to 500ms loading
    //need to do it in background
    /**
     * use it to generate key pairs
     */
    private val keyPairGenerator: KeyPairGenerator? by lazy {
        Timber.d("init keyPairGenerator")
        try {
            return@lazy KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_NAME)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: NoSuchProviderException) {
            Timber.e(e)
        }
        null
    }

    /**
     * use it for encoding data
     */
    private val cipherForEncoding: Cipher? by lazy {
        Timber.d("init cipherForEncoding")
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val publicKey = keyStore?.getCertificate(KEY_ALIAS)?.publicKey
            publicKey?.let {
                val unrestricted = KeyFactory.getInstance(it.algorithm).generatePublic(X509EncodedKeySpec(it.encoded))
                //we need to pass spec to Cipher#init.
                //See https://stackoverflow.com/a/36021145/3212712 and https://habr.com/company/e-Legion/blog/317706/
                val spec = OAEPParameterSpec(
                    MD_NAME,
                    MGF_NAME,
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
                )
                cipher.init(Cipher.ENCRYPT_MODE, unrestricted, spec)
                return@lazy cipher
            }
        } catch (exception: KeyPermanentlyInvalidatedException) {
            deleteInvalidKey()
        } catch (e: KeyStoreException) {
            Timber.e(e)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: NoSuchPaddingException) {
            Timber.e(e)
        } catch (e: InvalidKeyException) {
            Timber.e(e)
        } catch (e: InvalidKeySpecException) {
            Timber.e(e)
        } catch (e: InvalidAlgorithmParameterException) {
            Timber.e(e)
        }
        null
    }

    /**
     * use it for decoding encoded data.
     *
     * WARNING! Will raise exception if cipher isn't authenticated by user via fingerprintManager
     */
    private val cipherForDecoding: Cipher? by lazy {
        Timber.d("init cipherForDecoding")
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val privateKey = keyStore?.getKey(KEY_ALIAS, null) as? PrivateKey
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            return@lazy cipher
        } catch (exception: KeyPermanentlyInvalidatedException) {
            deleteInvalidKey()
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: NoSuchPaddingException) {
            Timber.e(e)
        } catch (e: KeyStoreException) {
            Timber.e(e)
        } catch (e: UnrecoverableKeyException) {
            Timber.e(e)
        } catch (e: InvalidKeyException) {
            Timber.e(e)
        }
        null
    }

    /**
     * generates new key pair witch requires user authentication for decoding
     */
    private fun generateNewKey() {
        Timber.d("generateNewKey")
        keyPairGenerator?.let {
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
        }
    }

    private fun deleteInvalidKey() {
        Timber.d("deleteInvalidKey")
        keyStore?.let {
            try {
                it.deleteEntry(KEY_ALIAS)
            } catch (e: KeyStoreException) {
                Timber.e(e)
            }
        }
    }

    private fun cryptoUtilsIsReady() = keyStore != null
            && keyStore!!.containsAlias(KEY_ALIAS)
            && cipherForEncoding != null
            && cipherForDecoding != null


    /**
     * encodes given string with public part of key
     *
     * @return encoded string or null in case of some error
     */
    fun encode(inputString: String): String? {
        try {
            if (cryptoUtilsIsReady()) {
                val bytes = cipherForEncoding?.doFinal(inputString.toByteArray())
                return bytes?.let { String(Base64.encode(bytes, Base64.NO_WRAP)) }
            }
        } catch (e: IllegalBlockSizeException) {
            Timber.e(e)
        } catch (e: BadPaddingException) {
            Timber.e(e)
        }

        return null
    }

    /**
     * @param encodedString String to decode with private key
     * @param cipherDecrypter authenticated cipher inited with DECRYPT_MODE
     */
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

    /**
     * wrapper for decoding cipher used by fingerprint manager to authenticate it
     *
     * and use for decoding data with private key
     */
    fun getCryptoObject(): FingerprintManager.CryptoObject? {
        return if (cryptoUtilsIsReady()) {
            cipherForDecoding?.let { FingerprintManager.CryptoObject(it) }
        } else null
    }

    /**
     * @return encoded 6 signs Int generated by SecureRandom
     */
    fun generateUserPassword(): String? {
        val min = 100000
        val max = 999999
        val password = (SecureRandom().nextInt(max - min + 1) + min).toString()
        Timber.d("passwordRaw: $password")
        return CryptoUtils.encode(password)
    }
}