package moji.deliverytracker

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

/**
 * Secure password hashing using PBKDF2.
 * Session is ephemeral — cleared on each app cold start.
 */
object SecurityHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_PASS_HASH = "pass_hash_v2"
    private const val KEY_PASS_SALT = "pass_salt"
    private const val KEY_AUTH = "auth_session"
    private const val PBKDF2_ITERATIONS = 10_000
    private const val HASH_KEY_LENGTH = 256

    /**
     * In-memory session flag. Resets on process death (app restart).
     * Not stored in SharedPreferences to prevent bypass via adb/root.
     */
    @Volatile
    private var sessionAuthenticated = false

    fun isAuthenticated(context: Context): Boolean {
        return sessionAuthenticated
    }

    fun setAuthenticated(context: Context, value: Boolean) {
        sessionAuthenticated = value
    }

    fun hasPassword(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.contains(KEY_PASS_HASH)
    }

    fun setPassword(context: Context, password: String) {
        val prefs = getPrefs(context)
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        prefs.edit()
            .putString(KEY_PASS_HASH, hash)
            .putString(KEY_PASS_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
    }

    fun verifyPassword(context: Context, password: String): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(KEY_PASS_HASH, null) ?: return false
        val saltStr = prefs.getString(KEY_PASS_SALT, null) ?: return false
        val salt = Base64.decode(saltStr, Base64.NO_WRAP)
        val inputHash = hashPassword(password, salt)
        return constantTimeEquals(storedHash, inputHash)
    }

    /**
     * Migrate from old insecure hashCode()-based password to new PBKDF2.
     * Returns true if migration happened or no migration needed.
     */
    fun migrateIfNeeded(context: Context) {
        val prefs = getPrefs(context)
        // Already migrated
        if (prefs.contains(KEY_PASS_HASH)) return
        // Check for old-style hash
        val oldHash = prefs.getString("pass_hash", null)
        if (oldHash != null) {
            // Can't reverse the old hash, so reset to default "1234" with proper hashing
            // User should be prompted to change password
            setPassword(context, "1234")
            prefs.edit().remove("pass_hash").apply()
        } else {
            // First time — set default password
            setPassword(context, "1234")
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            HASH_KEY_LENGTH
        )
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        return salt
    }

    /** Constant-time comparison to prevent timing attacks. Does not leak length info. */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        return java.security.MessageDigest.isEqual(aBytes, bBytes)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
