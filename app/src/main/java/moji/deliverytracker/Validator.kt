package moji.deliverytracker

object Validator {
    /**
     * Iranian national ID: exactly 10 digits, or empty (optional field).
     * Also validates the check digit algorithm.
     */
    fun isValidNationalId(id: String): Boolean {
        if (id.isEmpty()) return true
        if (id.length != 10 || !id.all { it.isDigit() }) return false
        // Check for all-same-digit IDs (e.g., 1111111111)
        if (id.all { it == id[0] }) return false
        // Validate check digit
        val check = id[9].digitToInt()
        val sum = (0..8).sumOf { id[it].digitToInt() * (10 - it) }
        val remainder = sum % 11
        return if (remainder < 2) check == remainder else check == 11 - remainder
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isEmpty() || (phone.length == 11 && phone.startsWith("09") && phone.all { it.isDigit() })
    }

    fun isValidPlate(plate: String): Boolean {
        return plate.isEmpty() || plate.length in 7..9
    }
}
