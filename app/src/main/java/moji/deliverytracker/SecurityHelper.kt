package moji.deliverytracker

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

/**
 * Secure password hashing using PBKDF2.
 * Session is ephemeral -- cleared on each app cold start.
 */
object SecurityHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_PASS_HASH = "pass_hash_v2"
    private const val KEY_PASS_SALT = "pass_salt"
    private const val KEY_PASS_NEEDS_CHANGE = "pass_needs_change"
    private const val KEY_FAILED_COUNT = "auth_failed_count"
    private const val KEY_LOCKOUT_UNTIL = "auth_lockout_until"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val HASH_KEY_LENGTH = 256
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_MS = 5 * 60 * 1000L

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
            .putBoolean(KEY_PASS_NEEDS_CHANGE, false)
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

    fun needsPasswordChange(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PASS_NEEDS_CHANGE, false)
    }

    fun markPasswordChanged(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_PASS_NEEDS_CHANGE, false).apply()
    }

    fun registerFailedAttempt(context: Context) {
        val prefs = getPrefs(context)
        val attempts = prefs.getInt(KEY_FAILED_COUNT, 0) + 1
        val editor = prefs.edit().putInt(KEY_FAILED_COUNT, attempts)
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            editor.putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + LOCKOUT_MS)
        }
        editor.apply()
    }

    fun clearFailedAttempts(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_FAILED_COUNT)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    fun isLockedOut(context: Context): Boolean {
        val prefs = getPrefs(context)
        val until = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (until == 0L) return false
        if (System.currentTimeMillis() >= until) {
            clearFailedAttempts(context)
            return false
        }
        return true
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
            // User must change password on next login
            setDefaultPassword(context)
            prefs.edit().remove("pass_hash").apply()
        } else {
            // First time -- set default password and require change
            setDefaultPassword(context)
        }
    }

    private fun setDefaultPassword(context: Context) {
        val prefs = getPrefs(context)
        val salt = generateSalt()
        val hash = hashPassword("1234", salt)
        prefs.edit()
            .putString(KEY_PASS_HASH, hash)
            .putString(KEY_PASS_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putBoolean(KEY_PASS_NEEDS_CHANGE, true)
            .apply()
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
