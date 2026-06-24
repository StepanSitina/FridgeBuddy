package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.security.SecureRandom

// --- Cryptography Helpers for Safe Password Hashing ---
object HashUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = input + salt
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

// --- Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String, // ensures max 1 account per email naturally in SQLite table constraint
    val nickname: String,
    val passwordHash: String, // securely hashed password
    val salt: String,
    val pairingCode: String? = null
)

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantityHint: String,
    val category: String, // "Fridge", "Freezer", "Pantry"
    val expirationTimestamp: Long, // Epoch millis
    val addedTimestamp: Long = System.currentTimeMillis(),
    val approxPrice: Double = 0.0,
    val currency: String = "CZK"
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealName: String,
    val loggedAt: Long = System.currentTimeMillis(),
    val calories: Int = 350,
    val carbs: Int = 40,
    val protein: Int = 15,
    val fat: Int = 12,
    val timeOfDayHabit: String = "Snídaně" // "Snídaně", "Oběd", "Svačina", "Večeře"
)

@Entity(tableName = "configs")
data class ConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val neededQty: String = "1 ks",
    val isPurchased: Boolean = false,
    val targetStore: String = "Lidl", // Albert, Kaufland, Tesco, Rohlík.cz
    val priceEstimate: Double = 0.0,
    val currency: String = "CZK"
)

@Entity(tableName = "scan_history")
data class ScanHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val barcode: String,
    val category: String,
    val approxPrice: Double = 0.0,
    val currency: String = "CZK",
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface FridgeBuddyDao {
    @Query("SELECT * FROM pantry_items ORDER BY expirationTimestamp ASC")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deletePantryItemById(id: Int)

    @Query("SELECT * FROM meal_logs ORDER BY loggedAt DESC")
    fun getAllMealLogs(): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLog)

    @Query("SELECT * FROM configs WHERE key = :key LIMIT 1")
    suspend fun getConfig(key: String): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: ConfigEntity)

    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScanHistory(): Flow<List<ScanHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistoryItem(item: ScanHistoryItem)

    @Delete
    suspend fun deleteScanHistoryItem(item: ScanHistoryItem)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanHistoryItemById(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun clearScanHistory()

    @Query("DELETE FROM pantry_items")
    suspend fun clearPantryItems()

    @Query("DELETE FROM meal_logs")
    suspend fun clearMealLogs()

    @Query("DELETE FROM shopping_items")
    suspend fun clearShoppingItems()

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

// --- Database Wrapper & Room Initialization with Prepopulation callbacks ---

@Database(
    entities = [PantryItem::class, MealLog::class, ConfigEntity::class, ShoppingItem::class, UserEntity::class, ScanHistoryItem::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): FridgeBuddyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fridgebuddy_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repositories ---

class FridgeBuddyRepository(private val dao: FridgeBuddyDao) {
    val allPantryItems: Flow<List<PantryItem>> = dao.getAllPantryItems()
    val allMealLogs: Flow<List<MealLog>> = dao.getAllMealLogs()
    val allShoppingItems: Flow<List<ShoppingItem>> = dao.getAllShoppingItems()
    val allScanHistory: Flow<List<ScanHistoryItem>> = dao.getAllScanHistory()

    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun addPantryItem(item: PantryItem) {
        dao.insertPantryItem(item)
    }

    suspend fun removePantryItem(item: PantryItem) {
        dao.deletePantryItem(item)
    }

    suspend fun removePantryItemById(id: Int) {
        dao.deletePantryItemById(id)
    }

    suspend fun logMeal(log: MealLog) {
        dao.insertMealLog(log)
    }

    suspend fun getConfigVal(key: String): String? {
        return dao.getConfig(key)?.value
    }

    suspend fun setConfigVal(key: String, value: String) {
        dao.setConfig(ConfigEntity(key, value))
    }

    suspend fun addShoppingItem(item: ShoppingItem) {
        dao.insertShoppingItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        dao.updateShoppingItem(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        dao.deleteShoppingItem(item)
    }

    suspend fun addScanHistoryItem(item: ScanHistoryItem) {
        dao.insertScanHistoryItem(item)
    }

    suspend fun removeScanHistoryItem(item: ScanHistoryItem) {
        dao.deleteScanHistoryItem(item)
    }

    suspend fun removeScanHistoryItemById(id: Int) {
        dao.deleteScanHistoryItemById(id)
    }

    suspend fun clearAllData() {
        dao.clearPantryItems()
        dao.clearMealLogs()
        dao.clearShoppingItems()
        dao.clearScanHistory()
    }
}
