package moji.deliverytracker

object Validator {
    fun isValidNationalId(id: String): Boolean {
        return id.isEmpty() || (id.length == 10 && id.all { it.isDigit() })
    }
    
    fun isValidPhone(phone: String): Boolean {
        return phone.isEmpty() || (phone.length == 11 && phone.startsWith("09") && phone.all { it.isDigit() })
    }
    
    fun isValidPlate(plate: String): Boolean {
        return plate.isEmpty() || plate.length in 7..9
    }
}
