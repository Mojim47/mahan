package moji.deliverytracker

/**
 * Shared helper for resolving entity IDs from names.
 * Prevents duplicate ensure* methods across activities.
 * Does NOT auto-create entities — returns null if not found.
 */
object EntityHelper {

    suspend fun getCustomerId(db: AppDatabase, name: String): Int? {
        return db.customerDao().getIdByName(name)
    }

    suspend fun getDriverId(db: AppDatabase, name: String): Int? {
        return db.driverDao().getIdByName(name)
    }

    suspend fun getNeighborhoodId(db: AppDatabase, name: String): Int? {
        return db.neighborhoodDao().getIdByName(name)
    }

    /**
     * Resolve all three entity IDs. Returns null if any entity doesn't exist.
     */
    suspend fun resolveOrderEntities(
        db: AppDatabase,
        customerName: String,
        driverName: String,
        neighborhoodName: String
    ): Triple<Int, Int, Int>? {
        val customerId = getCustomerId(db, customerName) ?: return null
        val driverId = getDriverId(db, driverName) ?: return null
        val neighborhoodId = getNeighborhoodId(db, neighborhoodName) ?: return null
        return Triple(customerId, driverId, neighborhoodId)
    }

    /**
     * Check if a driver has any orders referencing them.
     */
    suspend fun driverHasOrders(db: AppDatabase, driverId: Int): Boolean {
        return db.orderDao().countByDriver(driverId) > 0
    }

    /**
     * Check if a driver has any payments referencing them.
     */
    suspend fun driverHasPayments(db: AppDatabase, driverId: Int): Boolean {
        return db.paymentDao().countByDriver(driverId) > 0
    }

    /**
     * Check if a customer has any orders referencing them.
     */
    suspend fun customerHasOrders(db: AppDatabase, customerId: Int): Boolean {
        return db.orderDao().countByCustomer(customerId) > 0
    }

    /**
     * Check if a neighborhood has any orders referencing them.
     */
    suspend fun neighborhoodHasOrders(db: AppDatabase, neighborhoodId: Int): Boolean {
        return db.orderDao().countByNeighborhood(neighborhoodId) > 0
    }
}
