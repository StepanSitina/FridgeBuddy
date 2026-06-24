package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import java.text.SimpleDateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class FridgeBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FridgeBuddyRepository(db.dao())

    private val prefs = application.getSharedPreferences("fridge_buddy_prefs", android.content.Context.MODE_PRIVATE)

    // Language setting ("CZ" or "EN" or "SK")
    private val _languageCode = MutableStateFlow(prefs.getString("language_code", "CZ") ?: "CZ")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    val isEnglish = _languageCode.map { it == "EN" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Onboarding completed indicator
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isTutorialCompleted = MutableStateFlow(prefs.getBoolean("tutorial_completed", false))
    val isTutorialCompleted: StateFlow<Boolean> = _isTutorialCompleted.asStateFlow()

    fun completeTutorial() {
        prefs.edit().putBoolean("tutorial_completed", true).apply()
        _isTutorialCompleted.value = true
    }

    // Email for synchronization
    private val _userEmail = MutableStateFlow(prefs.getString("user_email", null))
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // User nickname for personalization
    private val _userNickname = MutableStateFlow(prefs.getString("user_nickname", "") ?: "")
    val userNickname: StateFlow<String> = _userNickname.asStateFlow()

    // User unique 6-character/digit invitation/pairing code
    private val _userPairingCode = MutableStateFlow(prefs.getString("user_pairing_code", null))
    val userPairingCode: StateFlow<String?> = _userPairingCode.asStateFlow()

    // Password for secure login
    private val _userPassword = MutableStateFlow(prefs.getString("user_password", null))
    val userPassword: StateFlow<String?> = _userPassword.asStateFlow()

    // Language setting (false = Czech/CZK, true = Slovak/EUR)
    private val _isSlovak = MutableStateFlow(prefs.getString("language_code", "CZ") == "SK")
    val isSlovak: StateFlow<Boolean> = _isSlovak.asStateFlow()

    // Household / Family group ID (persistent in SharedPreferences)
    private val _householdId = MutableStateFlow(prefs.getString("household_id", "NK-FAMILY-DEMO123") ?: "NK-FAMILY-DEMO123")
    val householdId: StateFlow<String> = _householdId.asStateFlow()

    fun updateHouseholdId(newId: String) {
        prefs.edit().putString("household_id", newId).apply()
        _householdId.value = newId
    }

    // Daily Caloric Goal limit (customizable)
    private val _calorieGoal = MutableStateFlow(2100)
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    // Cloud Backup Simulate Status
    private val _backupStatus = MutableStateFlow("Ok - Zálohováno v cloudu / Zálohované")
    val backupStatus: StateFlow<String> = _backupStatus.asStateFlow()

    // Health Connect Sync State (Android successor to HealthKit)
    private val _isHealthConnectSynced = MutableStateFlow(true)
    val isHealthConnectSynced: StateFlow<Boolean> = _isHealthConnectSynced.asStateFlow()

    // AI States
    private val _aiRecipeMarkdown = MutableStateFlow<String>("")
    val aiRecipeMarkdown: StateFlow<String> = _aiRecipeMarkdown.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Reactive lists from database
    val pantryItems: StateFlow<List<PantryItem>> = repository.allPantryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealLogs: StateFlow<List<MealLog>> = repository.allMealLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanHistory: StateFlow<List<ScanHistoryItem>> = repository.allScanHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Caloric Progress Stats (derived state of today's logs)
    val caloriesConsumedToday = mealLogs.map { logs ->
        // Direct sum of today's calorie counts
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        logs.filter { it.loggedAt >= today }.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val carbsToday = mealLogs.map { logs ->
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        logs.filter { it.loggedAt >= today }.sumOf { it.carbs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val proteinToday = mealLogs.map { logs ->
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        logs.filter { it.loggedAt >= today }.sumOf { it.protein }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val fatToday = mealLogs.map { logs ->
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        logs.filter { it.loggedAt >= today }.sumOf { it.fat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Standard preset recipes list showing typical CZ/SK versions & international
    val customRecipeMatches = combine(pantryItems, _isSlovak, _languageCode) { items, isSl, langCode ->
        computeRecipeList(items.map { it.name.lowercase() }, isSl)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Clear all database tables once for this build so we guarantee starting from zero!
        if (!prefs.getBoolean("db_wipe_v4_completed", false)) {
            viewModelScope.launch {
                try {
                    repository.clearAllData()
                    _backupStatus.value = "Ok - Databáze vyčištěna"
                    prefs.edit()
                        .putBoolean("db_wipe_v4_completed", true)
                        .putBoolean("onboarding_completed", false)
                        .remove("user_email")
                        .remove("user_password")
                        .remove("user_nickname")
                        .apply()
                    _isOnboardingCompleted.value = false
                    _userEmail.value = null
                    _userPassword.value = null
                    _userNickname.value = ""
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun registerUser(nickname: String, email: String, pword: String, lang: String): String? {
        val trimmedEmail = email.trim().lowercase()
        val existing = repository.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return if (lang == "SK") "Účet s týmto e-mailom už existuje." else if (lang == "EN") "Account with this email already exists." else "Účet s tímto e-mailem již existuje."
        }
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.sha256(pword, salt)
        val generatedDigits = (100000..999999).random().toString()
        val generatedCode = "NK-$generatedDigits"
        val user = UserEntity(
            email = trimmedEmail,
            nickname = nickname.trim(),
            passwordHash = hash,
            salt = salt,
            pairingCode = generatedCode
        )
        return try {
            repository.registerUser(user)
            // Log in immediately after successful registration
            _languageCode.value = lang
            _isSlovak.value = (lang == "SK")
            _userEmail.value = user.email
            _userNickname.value = user.nickname
            _userPairingCode.value = user.pairingCode
            _isOnboardingCompleted.value = true
            prefs.edit()
                .putString("language_code", lang)
                .putString("user_email", user.email)
                .putString("user_nickname", user.nickname)
                .putString("user_pairing_code", user.pairingCode)
                .putBoolean("onboarding_completed", true)
                .apply()
            null
        } catch (e: Exception) {
            "Chyba při ukládání: ${e.message}"
        }
    }

    suspend fun loginUser(email: String, pword: String, lang: String): String? {
        val trimmedEmail = email.trim().lowercase()
        val user = repository.getUserByEmail(trimmedEmail)
        if (user == null) {
            return if (lang == "SK") "Účet s týmto e-mailom neexistuje." else if (lang == "EN") "Account with this email does not exist." else "Účet s tímto e-mailem neexistuje."
        }
        val hash = HashUtils.sha256(pword, user.salt)
        if (hash == user.passwordHash) {
            // Success! Save session state
            var finalPairingCode = user.pairingCode
            if (finalPairingCode.isNullOrBlank()) {
                val generatedDigits = (100000..999999).random().toString()
                finalPairingCode = "NK-$generatedDigits"
                val updatedUser = user.copy(pairingCode = finalPairingCode)
                repository.registerUser(updatedUser)
            }

            _languageCode.value = lang
            _isSlovak.value = (lang == "SK")
            _userEmail.value = user.email
            _userNickname.value = user.nickname
            _userPairingCode.value = finalPairingCode
            _isOnboardingCompleted.value = true
            prefs.edit()
                .putString("language_code", lang)
                .putString("user_email", user.email)
                .putString("user_nickname", user.nickname)
                .putString("user_pairing_code", finalPairingCode)
                .putBoolean("onboarding_completed", true)
                .apply()
            return null
        } else {
            return if (lang == "SK") "Nesprávne heslo." else if (lang == "EN") "Invalid password." else "Nesprávné heslo."
        }
    }

    fun completeOnboarding(lang: String, email: String?, password: String?) {
        viewModelScope.launch {
            _languageCode.value = lang
            _userEmail.value = email
            _userPassword.value = password
            _isOnboardingCompleted.value = true
            _isSlovak.value = (lang == "SK")
            prefs.edit()
                .putString("language_code", lang)
                .putString("user_email", email)
                .putString("user_password", password)
                .putBoolean("onboarding_completed", true)
                .apply()
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            _userEmail.value = null
            _userPassword.value = null
            _userNickname.value = ""
            _userPairingCode.value = null
            _isOnboardingCompleted.value = false
            try {
                repository.clearAllData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            prefs.edit()
                .remove("user_email")
                .remove("user_password")
                .remove("user_nickname")
                .remove("user_pairing_code")
                .putBoolean("onboarding_completed", false)
                .apply()
        }
    }

    // Toggle Language
    fun toggleLanguage() {
        viewModelScope.launch {
            val currLang = _languageCode.value
            val nextLang = if (currLang == "CZ") "EN" else if (currLang == "EN") "SK" else "CZ"
            _languageCode.value = nextLang
            _isSlovak.value = (nextLang == "SK")
            prefs.edit().putString("language_code", nextLang).apply()

            // Automatically switch currencies of shopping items
            val isSk = (nextLang == "SK")
            val currency = if (isSk) "EUR" else "CZK"
            shoppingItems.value.forEach { item ->
                val convertedPrice = if (isSk && item.currency == "CZK") {
                    (item.priceEstimate / 25.0)
                } else if (!isSk && item.currency == "EUR") {
                    (item.priceEstimate * 25.0)
                } else {
                    item.priceEstimate
                }
                repository.updateShoppingItem(item.copy(
                    currency = currency,
                    priceEstimate = Math.round(convertedPrice * 100.0) / 100.0
                ))
            }
        }
    }

    // Goal set value
    fun setDailyCalorieGoal(target: Int) {
        _calorieGoal.value = target
    }

    // Sync state
    fun syncWithHealthConnect() {
        viewModelScope.launch {
            _isHealthConnectSynced.value = true
            _backupStatus.value = "Synchronizováno v: " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }

    suspend fun fetchBarcodeProduct(barcode: String): OpenFoodFactsProduct? {
            return StepInTechAiService.fetchOpenFoodFactsProduct(barcode, _isSlovak.value)
    }

    fun addScanHistoryItem(name: String, barcode: String, category: String, price: Double) {
        viewModelScope.launch {
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            repository.addScanHistoryItem(
                ScanHistoryItem(
                    name = name,
                    barcode = barcode,
                    category = category,
                    approxPrice = price,
                    currency = currency,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun removeScanHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.removeScanHistoryItemById(id)
        }
    }

    fun addPantryItemTwice(name: String, category: String, approxPrice: Double) {
        viewModelScope.launch {
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            // expirace 7 dnu standardně
            val expirationTimestamp = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
            repeat(2) {
                repository.addPantryItem(
                    PantryItem(
                        name = name,
                        quantityHint = "1 ks",
                        category = category,
                        expirationTimestamp = expirationTimestamp,
                        approxPrice = approxPrice,
                        currency = currency
                    )
                )
            }
        }
    }

    // Pantry Management
    fun addManualPantryItem(name: String, quantity: String, category: String, expirationDays: Int, price: Double) {
        viewModelScope.launch {
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            val expirationTimestamp = System.currentTimeMillis() + (expirationDays * 24 * 60 * 60 * 1000L)
            repository.addPantryItem(
                PantryItem(
                    name = name,
                    quantityHint = quantity,
                    category = category,
                    expirationTimestamp = expirationTimestamp,
                    approxPrice = price,
                    currency = currency
                )
            )
        }
    }

    fun addAllCzechStaples() {
        viewModelScope.launch {
            val isSk = _isSlovak.value
            val currency = if (isSk) "EUR" else "CZK"
            val priceFactor = if (isSk) 25.0 else 1.0

            val staples = listOf(
                // Trvanlivé potraviny a přílohy (Pantry)
                StapleItem("Hladká mouka Babiččina volba", "1 kg", "Pantry", 14, 28.0),
                StapleItem("Lidl Pšeničná Mouka Hladká T450", "1 kg", "Pantry", 14, 18.0),
                StapleItem("Cukr krupice Albert", "1 kg", "Pantry", 30, 24.0),
                StapleItem("Těstoviny Špagety Panzani", "500 g", "Pantry", 20, 49.0),
                StapleItem("Rýže basmati Lagris", "1 kg", "Pantry", 20, 59.0),
                StapleItem("Ovesné vločky Emco", "500 g", "Pantry", 15, 29.0),
                StapleItem("Dětská krupice dehydrovaná", "500 g", "Pantry", 20, 22.0),
                StapleItem("Bramborový škrob Solamyl", "250 g", "Pantry", 30, 39.0),
                StapleItem("Sušené droždí Dr. Oetker", "1 ks", "Pantry", 15, 12.0),

                // Mléčné výrobky a tuky (Fridge)
                StapleItem("Jihočeské Máslo 82% Madeta", "250 g", "Fridge", 10, 54.0),
                StapleItem("Mléko polotučné 1,5% Kunín", "1 l", "Fridge", 7, 22.0),
                StapleItem("Mléko čerstvé plnotučné Tatra", "1 l", "Fridge", 6, 26.0),
                StapleItem("Jihočeský Eidam plátky 30% Madeta", "100 g", "Fridge", 7, 29.0),
                StapleItem("Pribináček vanilkový Pribina", "125 g", "Fridge", 6, 23.0),
                StapleItem("Lipánek vanilkový Madeta", "130 g", "Fridge", 6, 21.0),
                StapleItem("Florian Jogurt jahoda Olma", "150 g", "Fridge", 8, 15.0),
                StapleItem("Skyr bílý vysokoproteinový Milko", "140 g", "Fridge", 8, 19.0),
                StapleItem("Acidofilní Mléko Kunín", "950 g", "Fridge", 7, 34.0),
                StapleItem("Termix kakaový Kunín", "90 g", "Fridge", 7, 12.0),
                StapleItem("Kefírové mléko Kunín", "400 g", "Fridge", 7, 19.0),
                StapleItem("Lučina nadýchaná Savencia", "120 g", "Fridge", 6, 28.0),
                StapleItem("Tvaroháček vanilka Milko", "90 g", "Fridge", 6, 17.0),
                StapleItem("Hermelín Král Sýrů Pribina", "120 g", "Fridge", 8, 35.0),
                StapleItem("Bílý řecký jogurt Milko", "150 g", "Fridge", 8, 18.0),
                StapleItem("Activia bílá Danone", "120 g", "Fridge", 8, 16.0),
                StapleItem("Vajíčka čerstvá L Albert", "10 ks", "Fridge", 14, 45.0),
                StapleItem("Zakysaná smetana Kunín", "200 g", "Fridge", 5, 22.0),

                // Sladkosti, Pochutiny a Nápoje (Pantry)
                StapleItem("Horalky Arašídové Sedita", "50 g", "Pantry", 30, 12.0),
                StapleItem("Kakaové rezy Sedita", "50 g", "Pantry", 30, 11.0),
                StapleItem("Brumík Čokoládový Opavia", "30 g", "Pantry", 20, 11.0),
                StapleItem("Lentilky krabička Orion", "38 g", "Pantry", 45, 14.0),
                StapleItem("Studentská pečeť mléčná Orion", "170 g", "Pantry", 45, 49.0),
                StapleItem("Kinder Bueno Ferrero", "43 g", "Pantry", 30, 24.0),
                StapleItem("Kinder čokoláda Ferrero", "100 g", "Pantry", 40, 32.0),
                StapleItem("Merci Finest Selection Storck", "250 g", "Pantry", 60, 119.0),
                StapleItem("Zlaté polomáčené Opavia", "100 g", "Pantry", 30, 19.0),
                StapleItem("Kofola Originál dvoulitrová", "2 l", "Pantry", 30, 32.0),
                StapleItem("Přírodní pramenitá voda Rajec", "1.5 l", "Pantry", 45, 16.0),
                StapleItem("Acidofilní napoj jahoda", "500 ml", "Fridge", 7, 24.0),
                StapleItem("Birell Světlý plechovka", "0.5 l", "Pantry", 60, 21.0),
                StapleItem("Hanácká kyselka citron", "1.5 l", "Pantry", 40, 14.0),

                // Maso, Pomazánky, Cany a Ryby
                StapleItem("Šunka nejvyšší jakosti Albert", "100 g", "Fridge", 5, 29.0),
                StapleItem("Májka Paštika tavená Hamé", "120 g", "Pantry", 60, 24.0),
                StapleItem("Treska v majonéze Ryba Žilina", "140 g", "Fridge", 6, 28.0),
                StapleItem("Tuniaková nátierka Giana", "120 g", "Pantry", 45, 34.0),
                StapleItem("Salám Vysočina Kmotr", "300 g", "Pantry", 20, 59.0),
                StapleItem("Vídeňské párky Kostelecké uzeniny", "200 g", "Fridge", 6, 45.0),
                StapleItem("Kuřecí prsa čerstvá Albert", "500 g", "Fridge", 4, 89.0),
                StapleItem("Mleté maso mix Albert", "500 g", "Fridge", 3, 79.0),

                // Zelenina, Ovoce a Oleje
                StapleItem("Brambory varný typ B", "2 kg", "Pantry", 14, 38.0),
                StapleItem("Cibule kuchyňská žlutá", "1 kg", "Pantry", 14, 22.0),
                StapleItem("Česnek český hlavičky", "100 g", "Pantry", 20, 29.0),
                StapleItem("Řepkový olej Mňam", "1 l", "Pantry", 60, 45.0),
                StapleItem("Rajčata keříková volná", "500 g", "Fridge", 5, 39.0),
                StapleItem("Okurka hadovka", "1 ks", "Fridge", 5, 19.0),
                StapleItem("Banány Chiquita", "1 kg", "Pantry", 7, 34.0),
                StapleItem("Jablka Gala Albert", "1 kg", "Pantry", 14, 35.0),

                // Pečivo, koření aj.
                StapleItem("Český chléb Šumava Penam", "1 ks", "Pantry", 4, 39.0),
                StapleItem("Rohlík bílý pšeničný", "1 ks", "Pantry", 1, 2.9),
                StapleItem("Toastový chléb Penam", "500 g", "Pantry", 5, 32.0),
                StapleItem("Kečup jemný Otma", "500 g", "Pantry", 45, 42.0),
                StapleItem("Tatarka Hellmann's", "250 ml", "Fridge", 15, 45.0),
                StapleItem("Granko Orion", "250 g", "Pantry", 60, 49.0)
            )

            for (s in staples) {
                val expTime = System.currentTimeMillis() + (s.expDays * 24 * 60 * 60 * 1000L)
                val price = s.basePrice / priceFactor
                repository.addPantryItem(
                    PantryItem(
                        name = s.name,
                        quantityHint = s.qty,
                        category = s.cat,
                        expirationTimestamp = expTime,
                        approxPrice = Math.round(price * 100.0) / 100.0,
                        currency = currency
                    )
                )
            }
        }
    }

    private data class StapleItem(
        val name: String,
        val qty: String,
        val cat: String,
        val expDays: Int,
        val basePrice: Double
    )

    fun deleteItem(item: PantryItem) {
        viewModelScope.launch {
            repository.removePantryItem(item)
        }
    }

    // Shopping List Management
    fun addShoppingItem(name: String, quantity: String, store: String, price: Double) {
        viewModelScope.launch {
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            repository.addShoppingItem(
                ShoppingItem(
                    name = name,
                    neededQty = quantity,
                    targetStore = store,
                    priceEstimate = price,
                    currency = currency
                )
            )
        }
    }

    fun toggleShoppingItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    // Meal Logging (reduces pantry stock or logs user food intake directly)
    fun logMealDirect(name: String, cal: Int, carbs: Int, prot: Int, fat: Int, categoryName: String) {
        viewModelScope.launch {
            repository.logMeal(
                MealLog(
                    mealName = name,
                    calories = cal,
                    carbs = carbs,
                    protein = prot,
                    fat = fat,
                    timeOfDayHabit = categoryName
                )
            )
            // Visual check: did we consume a fridge item of that name? If so, auto remove to avoid extra friction
            val matchingPantry = pantryItems.value.firstOrNull {
                it.name.lowercase().contains(name.lowercase()) || name.lowercase().contains(it.name.lowercase())
            }
            if (matchingPantry != null) {
                repository.removePantryItem(matchingPantry)
            }
        }
    }

    // Receipt OCR Parsing through StepInTechAiService or Fallback local
    fun processReceiptOcr(text: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val parsedList = StepInTechAiService.parseReceipt(text, _isSlovak.value)
                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L
                parsedList.forEach { i ->
                    repository.addPantryItem(
                        PantryItem(
                            name = i.name,
                            quantityHint = i.quantityHint,
                            category = i.category,
                            expirationTimestamp = now + (i.expirationDays * oneDay),
                            approxPrice = i.approxPrice,
                            currency = if (_isSlovak.value) "EUR" else "CZK"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("OcrProcess", "Failure parsing receipt OCR", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Dictated Voice input Parsing
    fun processVoiceDictation(spokenText: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val parsedList = StepInTechAiService.parseVoiceInput(spokenText, _isSlovak.value)
                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L
                parsedList.forEach { i ->
                    repository.addPantryItem(
                        PantryItem(
                            name = i.name,
                            quantityHint = i.quantityHint,
                            category = i.category,
                            expirationTimestamp = now + (i.expirationDays * oneDay),
                            approxPrice = i.approxPrice,
                            currency = if (_isSlovak.value) "EUR" else "CZK"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("VoiceDictation", "Failed voice", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Automated food recognition from simulated photo
    fun simulatePhotoUploadAndRecognize(foodName: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expirationTimestamp = now + (6 * 24 * 60 * 60 * 1000L)
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            val price = if (_isSlovak.value) 2.40 else 55.0
            
            repository.addPantryItem(
                PantryItem(
                    name = foodName.capitalize(),
                    quantityHint = "1 ks",
                    category = "Fridge",
                    expirationTimestamp = expirationTimestamp,
                    approxPrice = price,
                    currency = currency
                )
            )
            _isAiLoading.value = false
        }
    }

    // Run recipe engine against current stock
    fun runAiRecipeGeneration(customQuery: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val names = pantryItems.value.map { "${it.name} (${it.quantityHint})" }
                // TODO: Přidat skutečné alergeny profilu a ID domácnosti (zde zatím mock)
                val userAllergies = listOf("lepek", "laktóza") 
                val householdId = _householdId.value
                val userId = "user123"

                val rec = StepInTechAiService.generateAiRecipes(
                    householdId = householdId,
                    userId = userId,
                    availableItems = names,
                    userAllergies = userAllergies,
                    customRequest = customQuery,
                    isSlovak = _isSlovak.value
                )
                _aiRecipeMarkdown.value = rec
            } catch (e: Exception) {
                _aiRecipeMarkdown.value = "Chyba při komunikaci s kuchařem / Chyba kulinárskeho serveru."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun addMissingToShoppingList(neededItemName: String, store: String, estPrice: Double) {
        viewModelScope.launch {
            val currency = if (_isSlovak.value) "EUR" else "CZK"
            repository.addShoppingItem(
                ShoppingItem(
                    name = neededItemName,
                    neededQty = "1 ks",
                    isPurchased = false,
                    targetStore = store,
                    priceEstimate = estPrice,
                    currency = currency
                )
            )
        }
    }

    // Locally mapped standard localized recipe database computed on demand if StepInTech AI offline logic triggers
    private fun computeRecipeList(itemNames: List<String>, isSlovak: Boolean): List<LocalRecipe> {
        val list = mutableListOf<LocalRecipe>()
        val lang = _languageCode.value

        // Helper to add specialized recipes with localized details, dynamic missing/matching calculation, and precise stats
        fun spec(
            czTitle: String, skTitle: String, enTitle: String,
            time: String, cal: Int, prot: Int, carbs: Int, fat: Int,
            czIngs: List<String>, skIngs: List<String>, enIngs: List<String>,
            czInstructions: String, skInstructions: String, enInstructions: String
        ) {
            val title = if (lang == "EN") enTitle else if (isSlovak) skTitle else czTitle
            val ings = if (lang == "EN") enIngs else if (isSlovak) skIngs else czIngs
            val instructions = if (lang == "EN") enInstructions else if (isSlovak) skInstructions else czInstructions
            
            val matching = mutableListOf<String>()
            val missing = mutableListOf<String>()
            
            for (ing in ings) {
                val matched = itemNames.any { it.lowercase().contains(ing.lowercase()) || ing.lowercase().contains(it.lowercase()) }
                if (matched) {
                    matching.add(ing)
                } else {
                    missing.add(ing)
                }
            }
            
            val missingPrice = missing.size * (if (isSlovak) 1.50 else if (lang == "EN") 1.80 else 35.0)
            
            list.add(LocalRecipe(
                title = title,
                time = time,
                calories = cal,
                protein = prot,
                carbs = carbs,
                fat = fat,
                matchingIngredients = matching,
                missingIngredients = missing,
                missingPrice = if (missingPrice == 0.0) 0.0 else missingPrice,
                instructions = instructions
            ))
        }

        // --- soups (polévky) ---
        spec(
            "Kuřecí polévka s kapáním", "Kuracia polievka s kvapkaním", "Chicken Soup with Drop Batter",
            "25 min", 180, 14, 18, 6,
            listOf("kuřecí", "vejce", "mouka"), listOf("kuracie", "vajcia", "múka"), listOf("chicken", "eggs", "flour"),
            "1. Uvařte kuřecí maso se zeleninou. 2. Rozšlehejte vejce s krupicí či moukou a pomalu vlévejte do polévky, aby vzniklo kapání. 3. Krátce povařte.",
            "1. Uvarte kuracie mäso so zeleninou. 2. Rozšľahajte vajce s krupicou alebo múkou a pomaly vlievajte do polievky, aby vzniklo kvapkanie. 3. Krátko povarte.",
            "1. Boil the chicken with soup vegetables. 2. Whisk eggs with a bit of flour or semolina, then slowly pour into the boiling soup to form shreds. 3. Simmer briefly."
        )

        spec(
            "Koprová polévka s bramborem", "Kôprová polievka so zemiakmi", "Dill Soup with Potatoes",
            "20 min", 220, 5, 24, 12,
            listOf("brambory", "mléko", "máslo"), listOf("zemiaky", "mlieko", "maslo"), listOf("potatoes", "milk", "butter"),
            "1. Brambory uvařte s kmínem. 2. Přidejte mléko/smetanu, čerstvý kopr a zahuštěte máslovou jíškou. 3. Dochuťte octem a solí.",
            "1. Zemiaky uvarte s rascou. 2. Pridajte mlieko/smotanu, čerstvý kôpor a zahustite maslovou zápražkou. 3. Dochuťte octom a soľou.",
            "1. Boil potatoes with caraway seeds. 2. Add milk or cream, fresh dill, and thicken with roux. 3. Season with vinegar and salt."
        )

        spec(
            "Zelná polévka s uzeninou", "Kapustnica s klobásou", "Cabbage Soup with Sausage",
            "35 min", 340, 12, 18, 22,
            listOf("zelí", "slanina", "brambory"), listOf("kapusta", "slanina", "zemiaky"), listOf("cabbage", "bacon", "potatoes"),
            "1. Duste nakrájené zelí s bramborami. 2. Na pánvi opečte slaninu či uzeninu s cibulí a paprikou. 3. Vše spojte a zjemněte smetanou.",
            "1. Duste nakrájanú kapustu so zemiakmi. 2. Na panvici opečte slaninu alebo klobásu s cibuľou a paprikou. 3. Všetko spojte a zjemnite smetanou.",
            "1. Simmer sliced cabbage with potatoes. 2. Fry bacon or sausage with onions and paprika in a pan. 3. Combine and enrich with some sour cream."
        )

        spec(
            "Hráškový krém se smetanou", "Hráškový krém so smotanou", "Creamy Green Pea Soup",
            "15 min", 210, 8, 26, 9,
            listOf("brambory", "smetana"), listOf("zemiaky", "smotana"), listOf("potatoes", "cream"),
            "1. Hrášek povařte společně s nakrájeným bramborem. 2. Rozmixujte ponorným mixérem do hladkého krému. 3. Zjemněte smetanou a dochuťte.",
            "1. Hrášok povarte spolu s nakrájaným zemiakom. 2. Rozmixujte ponorným mixérom do hladkého krému. 3. Zjemnite smotanou a dochuťte.",
            "1. Simmer peas and diced potato. 2. Blend with a hand blender until fully smooth. 3. Stir in cream, season to taste and serve."
        )

        spec(
            "Cibulová polévka se sýrovými krutony", "Cibuľová polievka so syrovými krutónmi", "Onion Soup with Cheese Croutons",
            "20 min", 260, 9, 22, 15,
            listOf("cibule", "chléb", "sýr"), listOf("cibuľa", "chlieb", "syr"), listOf("onion", "bread", "cheese"),
            "1. Nakrájenou cibuli orestujte na másle zcela dozlatova. 2. Zalijte vodou a povařte. 3. Podávejte s opečeným chlebem posypaným sýrem.",
            "1. Nakrájanú cibuľu orestujte na masle úplne dozlatista. 2. Zalejte vodou a povarte. 3. Podávajte s opečeným chlebom posypaným syrom.",
            "1. Sauté sliced onions in butter until beautifully caramelized. 2. Pour in broth or water and simmer. 3. Serve topped with toasted cheese bread."
        )

        spec(
            "Rajská polévka s rýží", "Rajčiaková polievka s ryžou", "Sweet Tomato Soup with Rice",
            "15 min", 190, 4, 34, 4,
            listOf("protlak", "rýže"), listOf("pretlak", "ryža"), listOf("paste", "rice"),
            "1. Rajčatový protlak osmahněte na másle s cukrem. 2. Zalijte vodou, přidejte hřebíček/bobkový list a povařte. 3. Podávejte s vařenou rýží.",
            "1. Paradajkový pretlak osmahnite na masle s cukrom. 2. Zalejte vodou, pridajte klinčeky/bobkový list a povarte. 3. Podávajte s uvarenou ryžou.",
            "1. Sauté tomato paste with butter and sugar. 2. Add water, bay leaf, cloves and simmer. 3. Serve with boiled rice or alphabet pasta."
        )

        spec(
            "Kmínová polévka s vejcem", "Rascová polievka s vajcom", "Caraway Soup with Egg",
            "12 min", 140, 6, 12, 8,
            listOf("kmín", "vejce", "máslo"), listOf("rasca", "vajcia", "maslo"), listOf("caraway", "eggs", "butter"),
            "1. Na másle orestujte celý kmín s moukou na světlou jíšku. 2. Zalijte osolenou vodou a povařte 10 minut. 3. Vmíchejte rozšlehané vejce.",
            "1. Na masle orestujte celú rascu s múkou na svetlú zápražku. 2. Zalejte osolenou vodou a povarte 10 minút. 3. Vmiešajte rozšľahané vajce.",
            "1. Toast caraway seeds in butter, add flour to make a light roux. 2. Pour in salted water and simmer for 10 minutes. 3. Stir in a beaten egg."
        )

        // --- meat dishes (hlavní jídla z masa a uzenin) ---
        spec(
            "Vepřový řízek s vařeným bramborem", "Bravčový rezeň s varenými zemiakmi", "Crispy Pork Schnitzel with Potatoes",
            "25 min", 520, 32, 42, 24,
            listOf("vepřové", "brambory", "vejce", "strouhanka"), listOf("bravčové", "zemiaky", "vajcia", "strúhanka"), listOf("pork", "potatoes", "eggs", "crumbs"),
            "1. Vepřové plátky naklepejte a osolte. 2. Obalte v hladké mouce, rozšlehaném vejci a strouhance. 3. Smažte na oleji dozlatova, podávejte s vařeným bramborem.",
            "1. Bravčové plátky naklepte a osoľte. 2. Obaľte v hladkej múke, rozšľahanom vajci a strúhanke. 3. Smažte na oleji dozlatista, podávajte s varenými zemiakmi.",
            "1. Pound pork loin flat and season with salt. 2. Dredge in flour, beaten egg, and breadcrumbs. 3. Fry in oil until beautifully golden, serve with boiled potatoes."
        )

        spec(
            "Kuřecí řízek s bramborovou kaší", "Kurací rezeň s zemiakovou kašou", "Chicken Schnitzel with Mashed Potatoes",
            "25 min", 490, 34, 48, 18,
            listOf("kuřecí", "brambory", "vejce", "strouhanka", "máslo"), listOf("kuracie", "zemiaky", "vajcia", "strúhanka", "maslo"), listOf("chicken", "potatoes", "eggs", "crumbs", "butter"),
            "1. Kuřecí prsa rozklepejte a obalte v klasickém trojobalu. 2. Brambory uvařte, sceďte, přidejte teplé mléko a máslo a vyšlehejte kaši. 3. Řízek usmažte dozlatova.",
            "1. Kuracie prsia rozklepte a obaľte v klasickom trojobale. 2. Zemiaky uvarte, sceďte, pridajte teplé mlieko a maslo a vyšľhajte kašu. 3. Rezeň usmažte dozlatista.",
            "1. Pound chicken breast and coat in flour, egg, and breadcrumbs. 2. Boil potatoes, then mash with warm milk and butter until creamy. 3. Fry chicken and serve."
        )

        spec(
            "Kuřecí směs Čína s rýží", "Čínska kuracia zmes s ryžou", "Chicken Stir-Fry with Rice",
            "20 min", 410, 31, 52, 9,
            listOf("kuřecí", "paprika", "rýže"), listOf("kuracie", "paprika", "ryža"), listOf("chicken", "pepper", "rice"),
            "1. Kuřecí nudličky orestujte na oleji se zeleninou (paprika, cibule, mrkev). 2. Zakápněte sójovou omáčkou a krátce poduste. 3. Podávejte s rýží.",
            "1. Kuracie rezance orestujte na oleji so zeleninou (paprika, cibuľa, mrkva). 2. Pokvapkajte sójovou omáčkou a krátko poduste. 3. Podávajte s ryžou.",
            "1. Stir-fry chicken strips with vegetables (bell pepper, onion, carrot). 2. Drizzle with soy sauce and simmer briefly. 3. Serve hot with fluffy rice."
        )

        spec(
            "Kuřecí plátek s broskví a sýrem", "Kurací plátok s broskyňou a syrom", "Sautéed Peach & Cheese Chicken",
            "18 min", 390, 35, 12, 21,
            listOf("kuřecí", "broskve", "sýr", "máslo"), listOf("kuracie", "broskyne", "syr", "maslo"), listOf("chicken", "peach", "cheese", "butter"),
            "1. Kuřecí prsní plátek osolte a opečte na másle. 2. Poklaďte plátkem broskve a překryjte plátkem sýra. 3. Pod pokličkou zapečte do rozehřátí sýra.",
            "1. Kurací prsný plátok osoľte a opečte na masle. 2. Poklaďte plátkom broskyne a prekryte plátkom syra. 3. Pod pokrievkou zapečte kým sa syr neroztopí.",
            "1. Season chicken breast and pan-sear in butter. 2. Top with a canned peach half and cover with a slice of cheese. 3. Melt cheese under lid."
        )

        spec(
            "Zapečené těstoviny Šunkofleky", "Zapečené cestoviny Šunkofleky", "Baked Pasta with Ham (Šunkofleky)",
            "30 min", 460, 22, 48, 20,
            listOf("těstoviny", "šunka", "vejce"), listOf("cestoviny", "šunka", "vajcia"), listOf("pasta", "ham", "eggs"),
            "1. Uvařte nudle či těstoviny. 2. Smíchejte je s nakrájenou šunkou/slaninou a vložte do pekáče. 3. Zalijte vejci rozšlehanými s mlékem a zapečte dozlatova.",
            "1. Uvarte rezance alebo cestoviny. 2. Zmiešajte ich s nakrájanou šunkou/slaninou a vložte do pekáča. 3. Zalejte vajcami rozšľahanými s mliekom a zapečte.",
            "1. Boil pasta or square noodles. 2. Toss with chopped ham and crisp bacon in a greased baking pan. 3. Pour in beaten eggs with milk and bake until golden."
        )

        spec(
            "Segedínský guláš z vepřového", "Segedínsky guláš s kapustou", "Szeged Goulash",
            "45 min", 510, 26, 14, 38,
            listOf("vepřové", "zelí", "smetana"), listOf("bravčové", "kapusta", "smotana"), listOf("pork", "cabbage", "cream"),
            "1. Vepřové kostky osmahněte na cibuli s paprikou. 2. Přidejte zelí, podlijte vodou a duste doměkka. 3. Na závěr vmícháte kysanou smetanu.",
            "1. Bravčové kocky osmahnite na cibuli s paprikou. 2. Pridajte kapustu, podlejte vodou a duste domäkka. 3. Na záver vmiešajte kyslú smotanu.",
            "1. Sauté pork cubes with onion and sweet paprika. 2. Stir in sour cabbage, add water and braise until soft. 3. Finish with sour cream."
        )

        spec(
            "Katův šleh s kečupem", "Kaťák / Katov šľah", "Spicy Pork 'Katuv Sleh'",
            "20 min", 380, 28, 16, 22,
            listOf("vepřové", "cibule", "kečup"), listOf("bravčové", "cibuľa", "kečup"), listOf("pork", "onion", "ketchup"),
            "1. Maso opečte zprudka s cibulí, česnekem a paprikou. 2. Přidejte lžíci kečupu pro hustou omáčku. 3. Podávejte pálivé s bramboráčky.",
            "1. Mäso opečte sprudka s cibuľou, cesnakom a paprikou. 2. Pridajte lyžicu kečupu pre hustú pikantnú omáčku. 3. Podávajte s hranolkami alebo plackami.",
            "1. Pan-sear meat strips with onions, garlic and chillies. 2. Add ketchup and a splash of soy sauce to make a high-flavor glossy glaze. 3. Serve hot."
        )

        spec(
            "Kuřecí kapsa šunka-sýr", "Kuracia kapsa šunka-syr", "Ham & Cheese Stuffed Chicken Pocket",
            "25 min", 420, 36, 2, 28,
            listOf("kuřecí", "šunka", "sýr"), listOf("kuracie", "šunka", "syr"), listOf("chicken", "ham", "cheese"),
            "1. Do kuřecích prsou udělejte zářez. 2. Vložte plátek šunky a sýra, sepněte. 3. Opečte na pánvi na másle nebo upečte v troubě.",
            "1. Do kuracích pŕs urobte zárez. 2. Vložte plátok šunky a syra, zopnite. 3. Opečte na panvici na masle alebo upečte v rúre.",
            "1. Cut a pocket into chicken breasts. 2. Stuff with a slice of ham and a thick slab of cheese, seal with toothpicks. 3. Pan-sear in butter."
        )

        spec(
            "Pečené kuře na kmíně s rýží", "Pečené kura na rasci s ryžou", "Roasted Caraway Chicken with Rice",
            "50 min", 460, 32, 44, 16,
            listOf("kuřecí", "máslo", "kmín"), listOf("kuracie", "maslo", "rasca"), listOf("chicken", "butter", "caraway"),
            "1. Kuřecí porce důkladně osolte a bohatě pokmínujte. 2. Pečte v troubě s plátky másla a občasným podléváním. 3. Podávejte s dušenou rýží.",
            "1. Kuracie porcie dôkladne osoľte a bohato pokmínujte. 2. Pečte v rúre s plátkami masla a občasným podlievaním. 3. Podávajte s dusenou ryžou.",
            "1. Season chicken parts with salt and plenty of caraway. 2. Roast in oven with butter pads, basting regularly. 3. Serve with steamed rice."
        )

        spec(
            "Slaný koláč Quiche se slaninou", "Slaný koláč Quiche so slaninou", "Classic Bacon & Cheese Quiche",
            "40 min", 480, 15, 34, 32,
            listOf("mouka", "máslo", "slanina", "sýr"), listOf("múka", "maslo", "slanina", "syr"), listOf("flour", "butter", "bacon", "cheese"),
            "1. Z mouky, másla a špetky soli vypracujte těsto, vtlačte do formy. 2. Poklaďte osmaženou slaninou a sýrem. 3. Zalijte smetanou s vejci a upečte.",
            "1. Z múky, masla a štipky soli vypracujte cesto, vtlačte do formy. 2. Poklaďte osmaženou slaninou a syrom. 3. Zalejte smotanou s vajcami a upečte.",
            "1. Knead flour, butter and salt into shortcrust pastry, line a pie dish. 2. Fill with crisp bacon and cheese. 3. Pour heavy cream and eggs, bake."
        )

        spec(
            "Těstoviny s kuřecím a špenátem", "Cestoviny s kuracím a špenátom", "Chicken & Creamy Spinach Pasta",
            "18 min", 450, 32, 48, 14,
            listOf("těstoviny", "kuřecí", "špenát", "smetana"), listOf("cestoviny", "kuracie", "špenát", "smotana"), listOf("pasta", "chicken", "spinach", "cream"),
            "1. Kuřecí maso orestujte. 2. Přidejte špenát, zalijte smetanou a nechte lehce provařit. 3. Smíchejte s uvařenými těstovinami.",
            "1. Kuracie mäso orestujte. 2. Pridajte špenát, zalejte smotanou a nechajte ľahko prevariť. 3. Zmiešajte s uvarenými cestovinami.",
            "1. Sauté chicken cubes until tender. 2. Add spinach, pour in cream, and simmer until thick. 3. Combine with fresh cooked pasta and serve."
        )

        spec(
            "Vepřové na kmíně s rýží", "Bravčové na rasci s ryžou", "Pork with Caraway Seeds & Rice",
            "45 min", 490, 28, 48, 20,
            listOf("vepřové", "kmín", "rýže"), listOf("bravčové", "rasca", "ryža"), listOf("pork", "caraway", "rice"),
            "1. Vepřové plátky osmahněte na cibuli, zasypejte kmínem. 2. Podlijte vodou a duste doměkka. 3. Podávejte s nadýchanou rýží.",
            "1. Bravčové plátky osmahnite na cibuli, zasypte rascou. 2. Podlejte vodou a duste domäkka. 3. Podávajte s nadýchanou ryžou.",
            "1. Sear pork chunks on onions, season heavily with caraway seeds. 2. Add broth or water and braise until tender. 3. Serve with white rice."
        )

        spec(
            "Kuře na smetaně s těstovinami", "Kura na smotane s cestovinou", "Chicken in Cream Sauce with Pasta",
            "35 min", 470, 31, 52, 15,
            listOf("kuřecí", "smetana", "mrkev"), listOf("kuracie", "smotana", "mrkva"), listOf("chicken", "cream", "carrot"),
            "1. Kuřecí maso poduste na zeleninovém základu (mrkev, celer). 2. Vyjměte maso, zeleninu rozmixujte se smetanou a moukou na hladkou omáčku. 3. Podávejte s těstovinami.",
            "1. Kuracie mäso poduste na zeleninovom základe (mrkva, zeler). 2. Vyberte mäso, zeleninu rozmixujte so smotanou a múkou na hladkú omáčku. 3. Podávajte s cestovinami.",
            "1. Simmer chicken pieces with root vegetables. 2. Remove chicken, blend the vegetables with heavy cream to make a thick gravy. 3. Serve with pasta."
        )

        spec(
            "Smažené rybí prsty s kaší", "Smažené rybie prsty s kašou", "Crispy Fish Fingers with Mash",
            "15 min", 390, 16, 42, 17,
            listOf("brambory", "mléko", "máslo"), listOf("zemiaky", "mlieko", "maslo"), listOf("potatoes", "milk", "butter"),
            "1. Rybí prsty usmažte na oleji dozlatova. 2. Uvařené brambory rozmačkejte, přidejte horké mléko a máslo a vyšlehejte hladkou kaši. 3. Podávejte společně.",
            "1. Rybie prsty usmažte na oleji dozlatista. 2. Uvarené zemiaky roztlačte, pridajte horúce mlieko a maslo a vyšľahajte kašu. 3. Podávajte spoločne.",
            "1. Shallow-fry pre-packed fish fingers until crisp. 2. Combine boiled potatoes with warm milk and butter to make side mash. 3. Serve hot."
        )

        spec(
            "Pečený losos na másle", "Pečený losos na masle s zemiakmi", "Pan-seared Salmon on Butter",
            "20 min", 440, 28, 24, 28,
            listOf("brambory", "máslo"), listOf("zemiaky", "maslo"), listOf("potatoes", "butter"),
            "1. Filet z lososa osolte a opečte na másle kůží dolů zhruba 5-7 minut. 2. Otočte a dopečte. 3. Podávejte s vařenými brambory.",
            "1. Filet z lososa osoľte a opečte na masle kožou nadol zhruba 5-7 minút. 2. Otočte a dopečte. 3. Podávajte s varenými zemiakmi.",
            "1. Season salmon fillet with salt. 2. Sear skin-side down in butter for 6 minutes, flip and cook through. 3. Serve alongside boiled potatoes."
        )

        // --- vegetarian & cheese dishes (bezmasá jídla a jídla ze sýra) ---
        spec(
            "Smažený Hermelín s tatarkou", "Smažený Hermelín s tatárskou omáčkou", "Fried Breaded Hermelin (Camembert)",
            "15 min", 610, 22, 28, 45,
            listOf("hermelín", "vejce", "strouhanka", "majonéza"), listOf("hermelín", "vajcia", "strúhanka", "majonéza"), listOf("hermelín", "eggs", "crumbs", "mayonnaise"),
            "1. Hermelín obalte dvakrát v trojobalu (mouka, vejce, strouhanka), aby sýr nevytekl. 2. Usmažte v horkém oleji. 3. Podávejte s tatarskou omáčkou.",
            "1. Hermelín obaľte dvakrát v trojobale (múka, vajce, strúhanka), aby syr nevytiekol. 2. Usmažte v horúcom oleji. 3. Podávajte s tatárskou omáčkou.",
            "1. Double-coat Hermelin (or Camembert) in flour, egg, and breadcrumbs to prevent leaking. 2. Deep-fry until crispy. 3. Serve with tartar sauce."
        )

        spec(
            "Gratinované brambory se smetanou", "Gratinované zemiaky so smotanou", "Scalloped Creamy Gratin Potatoes",
            "35 min", 390, 11, 38, 22,
            listOf("brambory", "sýr", "smetana"), listOf("zemiaky", "syr", "smotana"), listOf("potatoes", "cheese", "cream"),
            "1. Brambory nakrájejte na tenké plátky. 2. Vrstvěte do pekáčku, osolte a zalijte smetanou. 3. Posypte strouhaným sýrem a zapečte dozlatova.",
            "1. Zemiaky nakrájajte na tenké plátky. 2. Vrstvite do pekáča, osoľte a zalejte smotanou. 3. Posypte strúhaným syrom a zapečte dozlatista.",
            "1. Slice potatoes incredibly thin. 2. Layer in a baking dish, season, and pour high-fat cream over them. 3. Top with grated cheese and bake."
        )

        spec(
            "Těstoviny s rajskou omáčkou", "Cestoviny s paradajkovou omáčkou", "Simple Sweet Tomato Pasta with Cheese",
            "15 min", 310, 12, 54, 6,
            listOf("těstoviny", "protlak", "sýr"), listOf("cestoviny", "pretlak", "syr"), listOf("pasta", "paste", "cheese"),
            "1. Rajčatový protlak poduste s trochou cibule, soli, pepře a cukru. 2. Uvařte těstoviny. 3. Vše spojte a zasypejte stlačeným sýrem.",
            "1. Paradajkový pretlak poduste s trochou cibule, soli, korenia a cukru. 2. Uvarte cestoviny. 3. Všetko spojte a zasypte strúhaným syrom.",
            "1. Simmer tomato paste with garlic, sugar, and salt. 2. Boil pasta. 3. Combine pasta and sauce, then top generously with shredded cheese."
        )

        spec(
            "Květákový mozeček s bramborem", "Karfiolový mozoček s zemiakmi", "Cauliflower Egg Scramble (Mozeček)",
            "20 min", 290, 14, 26, 15,
            listOf("květák", "vejce", "brambory"), listOf("karfiol", "vajcia", "zemiaky"), listOf("cauliflower", "eggs", "potatoes"),
            "1. Květák uvařte doměkka a nasekejte. 2. Orestujte na másle a cibulce. 3. Vmíchejte syrová vejce a míchejte do ztuhnutí jako míchaná vejce.",
            "1. Karfiol uvarte domäkka a nasekajte. 2. Orestujte na masle a cibuľke. 3. Vmiešajte surové vajcia a miešajte do stuhnutia.",
            "1. Boil cauliflower until soft, then chop. 2. Saute minced onion in butter, add cauliflower. 3. Stir in eggs and scramble like breakfast eggs."
        )

        spec(
            "Smažený květák v trojobalu", "Smažený karfiol v trojobale", "Crispy Fried Cauliflower florets",
            "25 min", 360, 10, 31, 20,
            listOf("květák", "vejce", "strouhanka"), listOf("karfiol", "vajcia", "strúhanka"), listOf("cauliflower", "eggs", "crumbs"),
            "1. Květák nasekejte na růžičky a krátce povařte v osolené vodě. 2. Obalte v mouce, vejci a strouhance. 3. Usmažte dokřupava.",
            "1. Karfiol nasekajte na ružičky a krátko povarte. 2. Obaľte v múke, vajci a strúhanke. 3. Usmažte dokrumpava.",
            "1. Breakdown cauliflower into florets and blanch. 2. Coat in flour, beaten egg, and seasoned breadcrumbs. 3. Deep-fry until crispy golden."
        )

        spec(
            "Brokolicové placky se sýrem", "Brokolicové placky so syrom", "Broccoli & Cheese Patties",
            "22 min", 280, 14, 18, 16,
            listOf("brokolice", "vejce", "strouhanka"), listOf("brokolica", "vajcia", "strúhanka"), listOf("broccoli", "eggs", "crumbs"),
            "1. Uvařenou brokolici rozmačkejte vidličkou. 2. Smíchejte s vejcem, česnekem, strouhankou a sýrem. 3. Smažte jako placičky na oleji.",
            "1. Uvarenú brokolicu roztlačte vidličkou. 2. Zmiešajte s vajcom, cesnakom, strúhankou a syrom. 3. Smažte ako placky na oleji.",
            "1. Mash steamed broccoli. 2. Mix with egg, minced garlic, binder crumbs, and grated cheese. 3. Shape into patties and pan-fry."
        )

        spec(
            "Zapečená brokolice se sýrem", "Zapečená brokolica so syrom", "Cheesy Baked Cream Broccoli",
            "25 min", 340, 16, 12, 26,
            listOf("brokolice", "sýr", "smetana"), listOf("brokolica", "syr", "smotana"), listOf("broccoli", "cheese", "cream"),
            "1. Brokolici rozeberte na růžičky a povařte 3 minuty. 2. Vložte do zapékací misky, přelijte smetanou s rozšlehaným vejcem. 3. Posypejte sýrem a zapečte.",
            "1. Brokolicu rozoberte na ružičky a povarte 3 minúty. 2. Vložte do zapekacej misky, prelejte smotanou s rozšľahaným vajcom. 3. Posypte syrom a zapečte.",
            "1. Cut broccoli into florets and blanch. 2. Arrange in dish, pour in cream whisked with an egg. 3. Generously cover with cheese and bake."
        )

        spec(
            "Houbová smaženice se žampiony", "Hubová praženica so šampiňónmi", "Mushroom & Eggs Scramble (Smaženice)",
            "15 min", 240, 12, 6, 18,
            listOf("žampiony", "vejce", "chléb"), listOf("šampiňóny", "vajcia", "chlieb"), listOf("mushrooms", "eggs", "bread"),
            "1. Žampiony nakrájejte a orestujte na másle a cibulce s kmínem. 2. Jakmile povolí, přidejte rozšlehaná vejce a míchejte do ztuhnutí. 3. Podávejte s chlebem.",
            "1. Šampiňóny nakrájajte a orestujte na masle a cibuľke s rascou. 2. Pridajte rozšľahané vajcia a miešajte do stuhnutia. 3. Podávajte s chlebom.",
            "1. Sauté sliced mushrooms with onions and caraway in butter. 2. Once tender, stir in fresh eggs and scramble. 3. Serve over thick crusty bread."
        )

        spec(
            "Smažené žampiony v trojobalu", "Smažené šampiňóny v trojobale", "Crispy Breaded Fried Mushrooms",
            "18 min", 310, 8, 24, 18,
            listOf("žampiony", "vejce", "strouhanka"), listOf("šampiňóny", "vajcia", "strúhanka"), listOf("mushrooms", "eggs", "crumbs"),
            "1. Žampiony oloupejte a očistěte. 2. Obalte v mouce, vejci a strouhance. 3. Smažte v horkém oleji dozlatova.",
            "1. Šampiňóny oúpte a očistite. 2. Obaľte v múke, vajci a strúhanke. 3. Smažte v horúcom oleji dozlatista.",
            "1. Clean and peel medium mushrooms. 2. Coat them beautifully in flour, egg, and breadcrumbs. 3. Fry in hot oil until deeply golden."
        )

        spec(
            "Tradiční lečo s vejcem", "Tradičné lečo s vajcom", "Traditional Egg & Pepper 'Lečo'",
            "20 min", 290, 11, 14, 21,
            listOf("cibule", "paprika", "rajčata", "vejce"), listOf("cibuľa", "paprika", "paradajky", "vajcia"), listOf("onion", "pepper", "tomatoes", "eggs"),
            "1. Orestujte cibuli se slaninou či párkem. 2. Přidejte nakrájenou papriku a rajčata, duste doměkka. 3. Vmíchejte vajíčka a nechte lehce srazit.",
            "1. Orestujte cibuľu so slaninou alebo párkom. 2. Pridajte nakrájanú papriku a paradajky, duste domäkka. 3. Vmiešajte vajíčka a nechajte zraziť.",
            "1. Sauté onions with bacon or sausages. 2. Add sliced bell peppers and fresh tomatoes, simmer. 3. Pour in beaten eggs and stir till set."
        )

        spec(
            "Křupavé bramboráky s česnekem", "Chrumkavé zemiakové placky", "Crispy Garlic Potato Pancakes (Bramboráky)",
            "18 min", 340, 6, 42, 15,
            listOf("brambory", "mouka", "česnek", "majoránka"), listOf("zemiaky", "múka", "cesnak", "majoránka"), listOf("potatoes", "flour", "garlic", "marjoram"),
            "1. Brambory nastrouhejte, vymačkejte vodu. 2. Smíchejte s česnekem, majoránkou, vejcem a hladkou moukou. 3. Smažte tenké, velmi křupavé placky na oleji.",
            "1. Zemiaky nastrúhajte, vytlačte vodu. 2. Zmiešajte s cesnakom, majoránkou, vajcom a múkou. 3. Smažte tenké placky na oleji.",
            "1. Shred raw potatoes, drain starch water. 2. Mix with minced garlic, dried marjoram, egg and flour. 3. Fry thin, incredibly crispy griddle cakes."
        )

        spec(
            "Čočka na kyselo s volským okem", "Šošovica na kyslo s vajcom", "Traditional Sour Lentils with Fried Egg",
            "25 min", 410, 24, 48, 12,
            listOf("vejce", "cibule", "ocet"), listOf("vajcia", "cibuľa", "ocot"), listOf("eggs", "onion", "vinegar"),
            "1. Uvařenou čočku dochuťte octem, cukrem a solí. 2. Navrch opečte cibulku. 3. Podávejte s upečeným volským okem.",
            "1. Uvarenú šošovicu dochuťte octom, cukrom a soľou. 2. Na panvici orestujte cibuľku. 3. Podávajte s upečeným volským okem.",
            "1. Season boiled brown lentils with sugar, salt and plenty of vinegar. 2. Top with fried onions and a sunny-side-up egg."
        )

        // --- sweet meals & desserts (sladká jídla a dezerty) ---
        spec(
            "Sladký trhanec s borůvkami", "Sladký trhanec s čučoriedkami", "Austrian Blueberry Kaiserschmarrn",
            "20 min", 390, 11, 56, 12,
            listOf("mouka", "mléko", "vejce", "borůvky"), listOf("múka", "mlieko", "vajcia", "čučoriedky"), listOf("flour", "milk", "eggs", "blueberries"),
            "1. Z mléka, mouky a žloutků připravte těsto, vmíchejte sníh z bílků. 2. Nalijte na pánev, upečte a vidličkami natrhejte. 3. Promíchejte s borůvkami.",
            "1. Z mlieka, múky a žĺtkov pripravte cesto, vmiešajte sneh. 2. Nalejte na panvicu, upečte a vidličkami natrhajte. 3. Premiešajte s čučoriedkami.",
            "1. Whip pancake batter using egg whites for lift, pour to pan. 2. Tear apart with forks during cooking. 3. Toss with blueberries and sugar."
        )

        spec(
            "Omeleta na sladko s džemem", "Sladká omeleta s džemom", "Sweet Jam-filled Omelette",
            "8 min", 280, 12, 18, 16,
            listOf("vejce", "máslo", "marmeláda"), listOf("vajcia", "maslo", "džem"), listOf("eggs", "butter", "jam"),
            "1. Vejce vyšlehejte se lžící cukru a osmažte na másle. 2. Hotovou omeletu bohatě potřete meruňkovým džemem. 3. Přehněte napůl a pocukrujte.",
            "1. Vajcia vyšľahajte s lyžicou cukru a osmažte na masle. 2. Hotovú omeletu bohato potrite džemom. 3. Prehnite napoly a pocukrujte.",
            "1. Whisk eggs with salt and a touch of sugar, pan-fry with butter until fluffy. 2. Spread sweet jam, fold, dust with sugar and serve."
        )

        spec(
            "Tvarohové knedlíky s jahodami", "Tvarohové knedle s jahodami", "Sweet Cheese Dumplings with Strawberries",
            "30 min", 410, 14, 68, 8,
            listOf("tvaroh", "mouka", "vejce", "jahody"), listOf("tvaroh", "múka", "vajcia", "jahody"), listOf("tvaroh", "flour", "eggs", "strawberries"),
            "1. Tvaroh smíchejte se špetkou krupice, moukou a vejcem v hladké těsto. 2. Odebírejte těsto, zabalte do něj jahody a tvarujte knedlíky. 3. Vařte ve vroucí vodě 8 minut.",
            "1. Tvaroh zmiešajte s krupicou, múkou a vajcom v cesto. 2. Odoberajte cesto, zabaľte jahody a tvarujte knedle. 3. Varte vo vriacej vode 8 minút.",
            "1. Knead fresh curd cheese, egg, and flour into a soft pliable dough. 2. Portions wraps around strawberries. 3. Cook in boiling water until floats."
        )

        spec(
            "Bramborové šišky se strouhankou", "Zemiakové šúľance s strúhankou", "Potato Gnocchi with Sweet Breadcrumbs",
            "35 min", 450, 8, 78, 11,
            listOf("brambory", "mouka", "strouhanka", "máslo"), listOf("zemiaky", "múka", "strúhanka", "maslo"), listOf("potatoes", "flour", "crumbs", "butter"),
            "1. Uvařené brambory nastrouhejte, vypracujte těsto s moukou. 2. Vytvarujte válečky (šišky) a vařte ve vodě, dokud nevyplavou. 3. Obalte v osmažené strouhance s cukrem.",
            "1. Uvarené zemiaky nastrúhajte, vypracujte cesto s múkou. 2. Vytvarujte šúľance a varte vo vode. 3. Obaľte v opraženej strúhanke s cukrom.",
            "1. Mix grated cold potatoes with flour into dough. 2. Roll into thin ropes, cut into small cones and boil. 3. Roll in sweet butter-browned breadcrumbs."
        )

        spec(
            "Tradiční škubánky s cukrem", "Tradičné škubánky s cukrom", "Traditional Sweet Potato 'Skubanky'",
            "30 min", 390, 6, 72, 8,
            listOf("brambory", "mouka", "máslo"), listOf("zemiaky", "múka", "maslo"), listOf("potatoes", "flour", "butter"),
            "1. Brambory uvařte, slijte vodu napůl, zasypejte moukou a nechte 15 minut dojít. 2. Vypracujte hladké lepivé těsto. 3. Vykrajujte lžící namočenou v másle a sypte mákem či strouhankou s cukrem.",
            "1. Zemiaky uvarte, zlejte vodu napoly, zasypte múkou. Nechajte popariť. 2. Vypracujte hladké cesto. 3. Vykrajujte lyžicou.",
            "1. Cook potatoes, drain half water, cover with flour and let steam with lid. 2. Beat until smooth and sticky. 3. Scoop with buttered spoon, top with sugar."
        )

        spec(
            "Žemlovka s jablky a tvarohem", "Žemľovka s jablkami a tvarohom", "Baked Apple & Curd Bread Pudding (Žemlovka)",
            "35 min", 440, 12, 64, 14,
            listOf("rohlík", "jablka", "tvaroh", "vejce", "mléko"), listOf("rohlík", "jablká", "tvaroh", "vajcia", "mlieko"), listOf("bread", "apples", "tvaroh", "eggs", "milk"),
            "1. Rohlíky nakrájejte a namočte v mléce s rozšlehaným vejcem. 2. Do pekáčku vrstvěte rohlíky, oslazený tvaroh s jablky. 3. Zapečte v troubě dozlatova.",
            "1. Rohlíky nakrájajte a namočte v mlieku s vajcom. 2. Do pekáča vrstvite rohlíky, osladený tvaroh s jablkami. 3. Zapečte v rúre.",
            "1. Dip stale bread rolls in sweet egg-milk. 2. Layer in buttered dish with shredded cinnamon apples and sweet curd cheese. 3. Bake until golden crisp."
        )

        spec(
            "Bohatý rýžový nákyp s ovocem", "Bohatý ryžový nákyp s ovocím", "Sweet Rice Pudding with Fruit & Meringue",
            "40 min", 420, 9, 68, 11,
            listOf("rýže", "mléko", "broskve", "vejce"), listOf("ryža", "mlieko", "broskyne", "vajcia"), listOf("rice", "milk", "peach", "eggs"),
            "1. Uvařte rýži v mléce na kaši. 2. Vmíchejte žloutky s cukrem a ovoce. 3. Vyšlehejte z bílků tuhý sníh, naneste navrch nákypu a zapečte v troubě.",
            "1. Uvarte ryžu v mlieku na kašu. 2. Vmiešajte žĺtka s cukrom a ovocie. 3. Vyšľahajte z bielkov sneh, naneste navrch a zapečte.",
            "1. Cook short-grain rice in sweet milk. 2. Fold in egg yolks, butter, and chopped peach slices. 3. Top with dense whipped egg white meringue, bake."
        )

        spec(
            "Pudink s piškoty a banánem", "Puding s piškótami a banánom", "Sweet Custard with Biscuits & Bananas",
            "10 min", 310, 8, 54, 6,
            listOf("mléko", "piškoty", "banán"), listOf("mlieko", "piškóty", "banán"), listOf("milk", "biscuits", "bananas"),
            "1. Uvařte mléčný pudink s cukrem. 2. Na dno pohárů naskládejte piškoty a nakrájený banán. 3. Zalijte horkým pudinkem a nechte zchladnout v lednici.",
            "1. Uvarte mliečny puding s cukrom. 2. Na dno pohárov poukladajte piškóty a nakrájaný banán. 3. Zalejte teplým pudingom a nechajte vychladiť.",
            "1. Boil vanilla/chocolate custard pudding. 2. Layer small round biscuits and banana rounds in cups. 3. Cover with cooking custard, chill and serve."
        )

        spec(
            "Domácí jablečný závin", "Domáci jablkový závin", "Traditional Homemade Apple Strudel",
            "35 min", 340, 4, 52, 14,
            listOf("mouka", "máslo", "jablka"), listOf("múka", "maslo", "jablká"), listOf("flour", "butter", "apples"),
            "1. Z mouky, vody, másla vypracujte hladké tažené těsto. 2. Rozválejte, poklaďte nastrouhanými jablky se skořicí a cukrem. 3. Zarolujte a upečte dozlatova.",
            "1. Z múky, vody a masla vypracujte hladké cesto. 2. Rozvaľkajte, naskladajte nastrúhané jablká s cukrom a škoricou. 3. Zarolujte a upečte.",
            "1. Form a smooth elastic pastry dough from flour, water, and butter. 2. Pull thin, cover with shredded cinnamon apples. 3. Rolled up tight and bake."
        )

        spec(
            "Obrácený jablečný koláč", "Obrátený jablkový koláč", "French Tatin Upside-Down Apple Cake",
            "30 min", 360, 6, 58, 12,
            listOf("jablka", "mouka", "máslo", "vejce"), listOf("jablká", "múka", "maslo", "vajcia"), listOf("apples", "flour", "butter", "eggs"),
            "1. Do formy dejte karamel z másla a cukru, vyložte plátky jablek. 2. Přeneste litou směs těsta navrch. 3. Upečte v troubě a po mírném zchladnutí obraťte dnem vzhůru.",
            "1. Do formy dajte karamel z masla a cukru, plátky jabĺk. 2. Zalejte liatym koláčovým cestom. 3. Upečte a po vybratí obráťte dnom nahor.",
            "1. Caramelize sugar with butter directly in pie pan, layer sliced apples. 2. Cover with light sponge cake batter. 3. Bake and immediately invert."
        )

        spec(
            "Tvarohový koláč na plech", "Tvarohový koláč na plech (Prešívaná deka)", "Easy Traypane Curd Sponge Cake",
            "28 min", 380, 14, 48, 16,
            listOf("tvaroh", "mouka", "máslo", "vejce"), listOf("tvaroh", "múka", "maslo", "vajcia"), listOf("tvaroh", "flour", "butter", "eggs"),
            "1. Smíchejte máslo, cukr, vejce a mouku s kypřicím práškem v těsto, rozetřete na plech. 2. Oslazený měkký tvaroh naneste na těsto jako mřížku. 3. Upečte.",
            "1. Zmiešajte maslo, cukor, vajcia, múku s kypriacim práškom, dajte na plech. 2. Osladený tvaroh naneste ozdobne na cesto. 3. Upečte.",
            "1. Cream batter with flour, butter and eggs. 2. Roll onto baking tray. 3. Pipe sweetened smooth curd cheese in crosswise grids, bake."
        )

        spec(
            "Muffiny s borůvkami", "Muffiny s čučoriedkami", "Classic Bakery Blueberry Muffins",
            "20 min", 290, 5, 38, 12,
            listOf("mouka", "mléko", "vejce", "borůvky"), listOf("múka", "mlieko", "vajcia", "čučoriedky"), listOf("flour", "milk", "eggs", "blueberries"),
            "1. Smíchejte mouku s práškem do pečiva a cukrem. 2. Přidejte mléko, olej a vejce. 3. Lehce vmíchejte borůvky, rozdělte do košíčků a pečte na 180°C.",
            "1. Zmiešajte múku s práškom do pečiva a cukrom. 2. Pridajte mlieko, olej a vajcia. 3. Vmiešajte čučoriedky, rozdeľte do košíčkov a pečte na 180°C.",
            "1. Combine dry ingredients with baking powder. 2. Fold in milk, oil and eggs. 3. Carefully fold in fresh blueberries, bake in muffin cups."
        )

        spec(
            "Domácí koblihy s džemem", "Domáce šišky s džemom", "Traditional Fluffy Yeast Jelly Donuts",
            "40 min", 410, 8, 55, 18,
            listOf("mouka", "droždí", "mléko", "marmeláda"), listOf("múka", "droždie", "mlieko", "džem"), listOf("flour", "yeast", "milk", "jam"),
            "1. Vypracujte kynuté těsto, nechte hodinu vykynout. 2. Vykrajujte placky, plňte džemem a slepujte. 3. Smažte v horkém oleji dozlatova a pocukrujte.",
            "1. Vypracujte kysnuté cesto, nechajte hodinu vykysnúť. 2. Vykrajujte šišky, dajte džem. 3. Smažte v horúcom oliji dozlatista a pocukrujte.",
            "1. Yeast-rise a rich sweet flour-milk dough. 2. Punch down, cut out rounds, fill with sweet jam. 3. Shallow-fry in clean oil until puffy."
        )

        spec(
            "Krupicový flan s lesním ovocem", "Krupicový flan s ovocím", "Chilled Sweet Semolina Flan",
            "15 min", 280, 8, 38, 8,
            listOf("krupice", "mléko", "jahody"), listOf("krupica", "mlieko", "jahody"), listOf("semolina", "milk", "strawberries"),
            "1. Uvařte velmi hustou krupicovou kaši s cukrem a špetkou soli. 2. Nalijte do bábovičkových formiček a nechte v chladu zcela ztuhnout. 3. Vyklopte s ovocem.",
            "1. Uvarte hustú krupicovú kašu s cukrom. 2. Nalejte do formičiek a nechajte v chlade stuhnúť. 3. Vyklopte s čerstvým ovocím.",
            "1. Cook rich dense sweet semolina porridge. 2. Pour into decorative ramekins and chill in fridge until fully set. 3. Invert and serve with berries."
        )

        spec(
            "Skládaný palačinkový dort", "Palačinková torta s džemom", "Layered Sweet Pancake Crepe Cake",
            "30 min", 520, 16, 68, 20,
            listOf("mouka", "mléko", "vejce", "tvaroh", "marmeláda"), listOf("múka", "mlieko", "vajcia", "tvaroh", "džem"), listOf("flour", "milk", "eggs", "tvaroh", "jam"),
            "1. Upečte sérii tenkých sladkých palačinek. 2. Na talíř skládejte palačinky na sebe a střídavě mažte džemem a sladkým tvarohem. 3. Nechte zchladit a krájejte.",
            "1. Upečte sériu tenkých sladkých palaciniek. 2. Na tanier vrstvite palacinky a striedavo mažte džemom a sladkým tvarohom. 3. Nechajte vychladiť.",
            "1. Bake a stack of thin sweet golden crepes. 2. Layer them on plate like a cake, spreading curd cheese and red jam on alternating layers. 3. Slice."
        )

        // --- spreads, quick dinners & salads (rychlé večeře, pomazánky a saláty) ---
        spec(
            "Vajíčková pomazánka s cibulkou", "Vajíčková nátierka s cibuľkou", "Classic Scallion Egg Salad Spread",
            "10 min", 260, 12, 4, 22,
            listOf("vejce", "máslo", "hořčice", "cibule"), listOf("vajcia", "maslo", "horčica", "cibuľa"), listOf("eggs", "butter", "mustard", "onion"),
            "1. Vejce uvařte natvrdo, oloupejte a nastrouhejte najemno. 2. Změklé máslo utřete s lžící plnotučné hořčice, soli a pepře. 3. Vmíchejte vajíčka a cibuli.",
            "1. Vajcia uvarte natvrdo, ošúpte a nastrúhajte najemno. 2. Zmäknuté maslo vyšľahajte s lyžicou plnotučnej horčice. 3. Vmiešajte vajíčka a cibuľu.",
            "1. Hard-boil eggs, peel and grease fine. 2. Cream soft butter with mild mustard, salt and pepper. 3. Fold in eggs and chopped spring onions."
        )

        spec(
            "Budapešťská pomazánka", "Budapeštianska nátierka s tvarohom", "Budapest Red Paprika Curd Spread",
            "8 min", 190, 11, 6, 14,
            listOf("tvaroh", "máslo", "paprika", "cibule"), listOf("tvaroh", "maslo", "paprika", "cibuľa"), listOf("tvaroh", "butter", "paprika", "onion"),
            "1. Měkký tvaroh vyšlehejte se změklým máslem. 2. Přidejte dvě lžičky mleté sladké papriky a nadrobno nakrájenou cibulku. 3. Osolte a promíchejte.",
            "1. Mäkký tvaroh vyšľahajte so zmäknutým maslom. 2. Pridajte sladkú mletú papriku a nadrobno nakrájanú cibuľu. 3. Osoľte a premiešajte.",
            "1. Whip fresh curd skim cheese with softened butter. 2. Add sweet paprika powder and finely minced spring onion. 3. Salt and mix well."
        )

        spec(
            "Česneková pomazánka se sýrem", "Cesnaková nátierka so syrom", "Garlic & Cheese Party Spread",
            "8 min", 290, 14, 3, 26,
            listOf("sýr", "česnek", "majonéza"), listOf("syr", "cesnak", "majonéza"), listOf("cheese", "garlic", "mayonnaise"),
            "1. Sýr Eidam nastrouhejte najemno. 2. Přidejte prolisovaný česnek, majonézu (nebo zakysanou smetanu/tvaroh) a špetku soli. 3. Důkladně promíchejte.",
            "1. Syr Eidam nastrúhajte najemno. 2. Pridajte prelisovaný cesnak, majonézu alebo tvaroh pre zjemnenie chute a štipku soli. 3. Premiešajte.",
            "1. Finely grate cheddar or edam cheese. 2. Mix with pressed garlic cloves, mayonnaise (or yogurt/sour cream), and a pinch of salt. 3. Mix smooth."
        )

        spec(
            "Ševcovský mls z Vysočiny", "Ševcovský mls s cibuľou", "Cold Deli Sausage Salad ('Sevcovsky Mls')",
            "10 min", 340, 10, 11, 28,
            listOf("vysočina", "cibule", "okurka", "kečup", "majonéza"), listOf("poličan", "cibuľa", "okurka", "kečup", "majonéza"), listOf("sausage", "onion", "pickles", "ketchup", "mayonnaise"),
            "1. Trvanlivý salám nakrájejte na tenké nudličky. 2. Přidejte sterilované kyselé okurky a cibuli. 3. Spojte lžící majonézy, kečupu a hořčice.",
            "1. Trvanlivý salám nakrájajte na tenké rezance. 2. Pridajte nakrájanú cibuľu a sterilizované okurky. 3. Spojte lyžicou majonézy a kečupu.",
            "1. Cut salami into thin batons. 2. Mix with chopped pickled cucumbers and red onions. 3. Dress with mayonnaise, ketchup, and yellow mustard."
        )

        spec(
            "Čerstvý šopský salát", "Čerstvý šopský šalát", "Chilled Shopska Salad with Feta",
            "10 min", 180, 8, 12, 11,
            listOf("okurka", "rajčata", "paprika", "sýr"), listOf("okurka", "paradajky", "paprika", "syr"), listOf("cucumber", "tomatoes", "pepper", "cheese"),
            "1. Okurku, rajčata a papriku nakrájejte na stejně velké kostičky. 2. Zakápněte olivovým olejem a osolte. 3. Navrch nastrouhejte slaný sýr.",
            "1. Okurku, paradajky a papriku nakrájajte na rovnako veľké kocky. 2. Pokvapkajte olejom a osoľte. 3. Navrch nastrúhajte slaný syr.",
            "1. Chop cucumbers, vine tomatoes, and peppers. 2. Toss with olive oil, salt, and pepper. 3. Top with grated salty Balkan or feta cheese."
        )

        spec(
            "Rajčatový salát s cibulí", "Paradajkový šalát s cibuľou", "Sweet-Sour Tomato & Onion Salad",
            "8 min", 90, 2, 14, 3,
            listOf("rajčata", "cibule", "ocet"), listOf("paradajky", "cibuľa", "ocot"), listOf("tomatoes", "onion", "vinegar"),
            "1. Rajčata nakrájejte na plátky, cibuli najemno. 2. Připravte nálev ze studené vody, octa, cukru a soli. 3. Vše spojte a nechte uležet.",
            "1. Paradajky nakrájajte na plátky, cibuľu najemno. 2. Pripravte nálev zo studenej vody, octu, cukru a soli. 3. Všetko spojte a nechajte uležať.",
            "1. Slice fresh tomatoes, finely mince sweet onions. 2. Whisk cold water with white vinegar, sugar and mineral salt. 3. Toss together."
        )

        spec(
            "Okurkový salát se smetanou", "Uhorkový šalát so smotanou", "Creamy Cucumber Summer Salad",
            "8 min", 120, 3, 8, 9,
            listOf("okurka", "smetana"), listOf("okurka", "smotana"), listOf("cucumber", "cream"),
            "1. Okurku hadovku najemno nastrouhejte, osolte a vyčkejte než pustí vodu. Slijte ji. 2. Vmíchejte zakysanou smetanu (či bílý jogurt). 3. Opepřete.",
            "1. Okurku uhorku nastrúhajte, osoľte a vyčkajte kým pustí vodu. Zlejte. 2. Vmiešajte kyslú smotanu alebo biely jogurt. 3. Okoreňte mletým korením.",
            "1. Grate seedless cucumber, salt lightly, let sweat and squeeze dry. 2. Stir in rich sour cream or cold thick yogurt. 3. Season with black pepper."
        )

        spec(
            "Česnekové topinky na sádle", "Cesnakové hrianky na masti", "Lard-Fried Crusts with Garlic (Topinky)",
            "10 min", 290, 6, 28, 16,
            listOf("chléb", "sádlo", "česnek"), listOf("chlieb", "sadlo", "cesnak"), listOf("bread", "lard", "garlic"),
            "1. Krajíce českého chleba nakrájejte napůl. 2. Smažte na rozpáleném sádle do křupava. 3. Ještě žhavé vydatně potírejte čerstvými stroužky česneku.",
            "1. Krajce chleba okrojte a osmažte na masti do chrumkava. 2. Horúce hrianky vydatne pošúchajte čerstvým cesnakom zo všetkých strán.",
            "1. Cut traditional rustic sourdough bread into halves. 2. Fry in hot pork lard or butter card until crispy. 3. Scrub with garlic cloves."
        )

        spec(
            "Ztracené vejce na toastu", "Stratené vajce na toaste", "Poached Egg on Buttered Toast",
            "10 min", 210, 11, 14, 12,
            listOf("vejce", "toastový", "máslo"), listOf("vajcia", "toastový", "maslo"), listOf("eggs", "bread", "butter"),
            "1. Vodu s octem přiveďte k mírnému varu, vytvořte vír a vyklepněte vejce. Vařte 3 minuty. 2. Toastový chléb opečte, namažte máslem. 3. Podávejte zakápnuté žloutkem.",
            "1. Vodu s octom priveďte k varu, vytvorte vír a dajte vajce. Varte 3 minúty. 2. Toast opečte, namažte maslom. 3. Podávajte.",
            "1. Heat water with double vinegar to low simmer, whirlpool, slide egg in, poach 3 minutes. 2. Serve on hot buttered toast."
        )

        spec(
            "Chleb ve vajíčku s hořčicí", "Chlieb vo vajíčku s horčicou", "Traditional Savory Eggy Bread Spread",
            "8 min", 310, 12, 28, 14,
            listOf("chléb", "vejce", "hořčice", "cibule"), listOf("chlieb", "vajcia", "horčica", "cibuľa"), listOf("bread", "eggs", "mustard", "onion"),
            "1. Krajíce chleba obalte v osolených rozšlehaných vejcích. 2. Usmažte z obou stran na pánvi. 3. Namažte hořčicí a bohatě posypejte jarní cibulkou.",
            "1. Krajce chleba obaľte v rozšľahaných vajciach. 2. Usmažte na panvici. 3. Potrite plnotučnou horčicou a bohato posypte cibuľkou.",
            "1. Whisk eggs with salt. 2. Dip thick bread slices in eggs, fry in hot butter on pan. 3. Spread yellow mustard, garnish with raw onions."
        )

        spec(
            "Utopenci s bohatou cibulí", "Utopenci v octovom náleve", "Classic Sour Pickled Sausages ('Utopenci')",
            "15 min", 340, 14, 11, 26,
            listOf("špekáčky", "cibule", "ocet"), listOf("špekáčky", "cibuľa", "ocot"), listOf("sausage", "onion", "vinegar"),
            "1. Špekáčky oloupejte, podélně nařízněte, vymažte hořčicí a naplňte plátky cibule. 2. Naskládejte do sklenice. 3. Přelijte teplým svařeným octovým nálevem a uložte.",
            "1. Špekáčky ošúpte, narežte, naplňte cibuľou a dajte do pohára. 2. Zalejte prevareným octovým nálevom s korením. 3. Nechajte odstáť v chlade.",
            "1. Peel natural casing fat sausages, split, smear mustard inside, pack with raw onions. 2. Lay up in jar, cover with hot sweet pickled vinegar."
        )

        spec(
            "Hermelínová pomazánka na studeno", "Hermelínová nátierka s maslom", "Creamy Hermelin & Onion Cold Spread",
            "8 min", 360, 18, 4, 30,
            listOf("hermelín", "máslo", "cibule"), listOf("hermelín", "maslo", "cibuľa"), listOf("hermelín", "butter", "onion"),
            "1. Vyzrálý Hermelín nasekejte najemno nebo nastrouhejte. 2. Utřete se změklým máslem a lžičkou majonézy. 3. Vmíchejte nasekanou cibulku.",
            "1. Hermelín nastrúhajte najemno. 2. Vyšľahajte so zmäknutým maslom. 3. Vmiešajte nadrobno nakrájanú cibuľu a soľ.",
            "1. Grate or crumble soft ripe Camembert/Hermelin cheese. 2. Mash with soft butter and fine sweet paprika. 3. Fold in spring onion."
        )

        spec(
            "Rychlá pizza z toastového chleba", "Rýchla pizza z hrianok", "Quick Midnight Toast Pizza",
            "10 min", 350, 18, 22, 18,
            listOf("toastový", "kečup", "šunka", "sýr"), listOf("toastový", "kečup", "šunka", "syr"), listOf("bread", "ketchup", "ham", "cheese"),
            "1. Toastový chléb potřete štědře kečupem. 2. Poklaďte plátky šunky a navrch dejte bohatou vrstvu sýra Eidam. 3. Posypejte sušeným oreganem a zapečte o 5 min.",
            "1. Krajce toastu potrite kečupom. 2. Poukladajte šunku, sýr a posypte oreganom. 3. Zapečte v rúre.",
            "1. Spread tomato ketchup over toast slices. 2. Layer sweet ham and cover with cheese. 3. Dust with dry oregano, bake until bubbly melted."
        )

        spec(
            "Blesková tuňáková pomazánka", "Tuniaková nátierka s citrónom", "Quick Lemon Tuna Mousse Spread",
            "8 min", 220, 18, 2, 14,
            listOf("tuňák", "máslo", "cibule"), listOf("tuniak", "maslo", "cibuľa"), listOf("tuna", "butter", "onion"),
            "1. Tuňáka z konzervy částečně slijte. 2. Utřete se změklým máslem, lžičkou plnotučné hořčice a nadrobno nakrájenou cibulkou. 3. Přidejte pár kapek citronu.",
            "1. Tuniaka z konzervy zlejte a zmiešajte s maslom. 2. Vmiešajte najemno nakrájanú cibuľu. 3. Pokvapkajte citrónom.",
            "1. Soft-drain canned oil tuna. 2. Whisk with soft savory butter, minced onion, and yellow mustard. 3. Add a generous splash of fresh lemon."
        )

        spec(
            "Tuňákový salát s rýží a kukuřicí", "Tuniakový šalát s ryžou a kukuricou", "Mediterranean Tuna Rice Salad with Corn",
            "15 min", 380, 22, 45, 11,
            listOf("tuňák", "rýže", "kukuřice"), listOf("tuniak", "ryža", "kukurica"), listOf("tuna", "rice", "corn"),
            "1. Uvařte rýži a nechte ji zcela vychladnout. 2. Přidejte tuňáka z konzervy a sterilovanou kukuřici/hrášek. 3. Zakápněte olivovým olejem a osolte.",
            "1. Uvarte ryžu a nechajte ju vychladnúť. 2. Pridajte tuniaka a sterilizovanú kukuricu. 3. Pokvapkajte kvalitným olivovým olejom, osoľte.",
            "1. Boil white rice and let cool down. 2. Combine with canned solid tuna flakes and sweet canned corn. 3. Drizzle with extra virgin olive oil."
        )

        // --- Lunches / Dinners ---
        spec(
            "Svíčková na smetaně s hovězím masem", "Sviečková na smotane s hovädzím mäsom", "Beef Tenderloin in Cream Sauce",
            "50 min", 580, 28, 42, 34,
            listOf("hovězí", "mrkev", "celer", "petržel", "smetana", "citron"), listOf("hovädzie", "mrkva", "zeler", "petržlen", "smotana", "citrón"), listOf("beef", "carrot", "celery", "parsley", "cream", "lemon"),
            "1. Hovězí maso prošpikujte slaninou, osolte a opečte na cibuli. 2. Přidejte nakrájenou mrkev, celer, petržel a duste do měkka. 3. Maso vyjměte, zeleninu rozmixujte, zjemněte smetanou a dochuťte citronem a cukrem.",
            "1. Hovädzie mäso prešpikujte slaninou, osoľte a opečte na cibuli. 2. Pridajte mrkvu, zeler, petržlen a duste do mäkka. 3. Mäso vyberte, zeleninu rozmixujte, zjemnite smotanou a dochuťte citrónom.",
            "1. Lard the beef with bacon, salt it and sear on onions. 2. Add chopped veggies (carrot, celery, parsley) and simmer until tender. 3. Remove beef, blend the vegetables, smooth with cooking cream and season with lemon juice and pinch of sugar."
        )

        spec(
            "Rajská omáčka s hovězím masem a těstovinami", "Rajčiaková omáčka s hovädzím a cestovinou", "Classic Tomato Beef with Pasta",
            "45 min", 490, 26, 60, 16,
            listOf("hovězí", "protlak", "těstoviny", "cibule"), listOf("hovädzie", "pretlak", "cestoviny", "cibuľa"), listOf("beef", "paste", "pasta", "onion"),
            "1. Hovězí maso uvařte do měkka v osolené vodě. 2. Na másle orestujte cibuli, přidejte rajčatový protlak, cukr, skořici a zalijte vývarem. 3. Povařte, dochuťte a podávejte s vařenými těstovinami.",
            "1. Hovädzie mäso uvarte do mäkka. 2. Orestujte cibuľu na masle, pridajte pretlak, cukor, škoricu a podlejte vývarom. 3. Povarte a podávajte s varenými cestovinami.",
            "1. Boil the beef in salted water until tender. 2. Sauté chopped onion in butter, add tomato paste, sugar, sweet cinnamon and pour in broth. 3. Simmer, adjust taste and serve with boiled pasta."
        )

        spec(
            "Segedínský guláš s vepřovým masem", "Segedínsky guláš s bravčovým mäsom", "Segedin Pork Goulash with Sauerkraut",
            "40 min", 530, 24, 26, 38,
            listOf("vepřové", "zelí", "smetana", "cibule"), listOf("bravčové", "kapusta", "smotana", "cibuľa"), listOf("pork", "cabbage", "cream", "onion"),
            "1. Na cibulce a sladké paprice opečte kostky vepřového masa. 2. Přidejte kysané zelí, kmín, trochu vody a duste do měkka. 3. Zahustěte moukou rozmíchanou v zakysané smetaně a krátce povařte.",
            "1. Na cibuľke a sladkej paprike opečte bravčové kocky. 2. Pridajte kyslú kapustu, rascu a duste do mäkka. 3. Zahustite múkou rozmiešanou v kyslej smotane.",
            "1. Sauté pork shoulder cubes with onions and sweet paprika. 2. Add sauerkraut, caraway seeds, water and simmer until soft. 3. Whisk flour in sour cream, stir into the goulash and boil briefly."
        )

        spec(
            "Tradiční slovenské bryndzové halušky", "Tradičné slovenské bryndzové halušky", "Traditional Slovak Bryndza Dumplings",
            "30 min", 610, 18, 72, 28,
            listOf("brambory", "mouka", "bryndza", "slanina"), listOf("zemiaky", "múka", "bryndza", "slanina"), listOf("potatoes", "flour", "bryndza", "bacon"),
            "1. Syrové brambory nastrouhejte najemno, smíchejte s moukou a solí v těsto. 2. Přes haluškovač házejte halušky do vroucí vody, dokud nevyplavou. 3. Smíchejte s bryndzou a posypejte dokřupava vyškvařenou slaninou.",
            "1. Surové zemiaky nastrúhajte najjemno, zmiešajte s múkou a soľou. 2. Cez haluškár hádžte halušky do vriacej vody, kým nevyplávajú. 3. Zmiešajte s bryndzou a posypte chrumkavou vyškvarenou slaninou.",
            "1. Finely grate raw peeled potatoes, mix with flour and pinch of salt to form a sticky dough. 2. Drop small dough pieces into boiling water until they float. 3. Strain, toss with rich bryndza cheese, and top with crispy fried pork bacon fat."
        )

        spec(
            "Smažený sýr s hranolkami a tatarkou", "Vyprážaný syr s hranolkami a tatárskou", "Crispy Fried Cheese with Fries and Tartar Sauce",
            "20 min", 720, 28, 55, 42,
            listOf("sýr", "vejce", "strouhanka", "hranolky", "tatarská omáčka"), listOf("syr", "vajcia", "strúhanka", "hranolky", "tatarská omáčka"), listOf("cheese", "eggs", "crumbs", "chips", "tartar sauce"),
            "1. Plátek sýra obalte v mouce, rozšlehaném vejci a strouhance (raději dvakrát, aby nevytekl). 2. Smažte v rozpáleném oleji na pánvi. 3. Hranolky upečte v troubě a podávejte s tatarskou omáčkou.",
            "1. Plátok syra obaľte v múke, rozšľahanom vajci a strúhanke (najlepšie dvakrát). 2. Smažte v rozpálenom ojeli. 3. Hranolky upečte v rúre a podávajte s tatárskou omáčkou.",
            "1. Dredge cheese thick slice in flour, beaten egg, and breadcrumbs (repeat twice to prevent leaking). 2. Pan-fry in hot clean oil. 3. Oven-bake commercial potato fries and serve with original tartar sauce."
        )

        spec(
            "Tradiční kuřecí rizoto se sýrem", "Tradičné kuracie rizoto so syrom", "Czech Style Chicken Veggie Risotto",
            "25 min", 410, 24, 52, 11,
            listOf("kuřecí", "rýže", "sýr"), listOf("kuracie", "ryža", "syr"), listOf("chicken", "rice", "cheese"),
            "1. Kuřecí prsa nakrájejte na kostky a orestujte na cibuli se solí a pepřem. 2. Přidejte dušenou rýži, hrášek nebo mrkev a dobře promíchejte. 3. Podávejte posypané nastrouhaným sýrem.",
            "1. Kuracie prsia nakrájajte na kocky a orestujte na cibuli. 2. Pridajte dusenú ryžu a zeleninu, dobre premiešajte. 3. Podávajte posypané nastrúhaným syrom.",
            "1. Dice fresh chicken breast, sauté on buttered onions. 2. Fold in warm cooked rice, sweet green peas, and stir nicely. 3. Serve garnished with finely grated cheese."
        )

        spec(
            "Křupavé bramboráky s uzeným masem", "Chrumkavé zemiakové placky s údeným", "Crispy Garlic Potato Pancakes with Smoked Pork",
            "25 min", 460, 16, 44, 24,
            listOf("brambory", "vejce", "mouka", "česnek", "šunka"), listOf("zemiaky", "vajcia", "múka", "cesnak", "šunka"), listOf("potatoes", "eggs", "flour", "garlic", "ham"),
            "1. Brambory nastrouhete najemno, vymačkejte přebytečnou vodu. 2. Přidejte vejce, mouku, česnek, majoránku, sůl a nakrájenou šunku (uzené). 3. Na pánvi smažte tenké placky dozlatova.",
            "1. Zemiaky nastrúhajte najjemno a vyžmýkajte vodu. 2. Pridajte vajcia, trochu múky, veľa cesnaku, majoránku a nakrájané údené/šunku. 3. Smažte placky.",
            "1. Grate raw potatoes fine, squeeze out excess liquid. 2. Stir in fresh eggs, flour, pressed garlic cloves, dry sweet marjoram and diced smoked ham. 3. Shallow-fry thin round cakes until crisp golden."
        )

        spec(
            "Strapačky s kysaným zelím a slaninou", "Strapačky s kyslou kapustou a slaninou", "Potato Dumplings with Sauerkraut and Bacon",
            "35 min", 540, 14, 68, 22,
            listOf("brambory", "mouka", "zelí", "slanina"), listOf("zemiaky", "múka", "kapusta", "slanina"), listOf("potatoes", "flour", "cabbage", "bacon"),
            "1. Z nastrouhaných brambor, mouky a soli vypracujte těsto jako na halušky a naházejte je do vroucí vody. 2. Kysané zelí poduste na cibuli. 3. Uvařené halušky smíchejte se zelím a vyškvařenou slaninou.",
            "1. Zo zemiakov a múky vypracujte cesto, nahádžte halušky do vriacej vody. 2. Kyslú kapustu poduste na cibuľke. 3. Halušky zmiešajte s kapustou a slaninou.",
            "1. Prepare and boil fresh potato halušky from raw potatoes and flour. 2. Steam sauerkraut over sautéed sweet onions until soft. 3. Combine hot dumplings with cabbage and top with fried crisp bacon."
        )

        // --- Desserts / Cakes ---
        spec(
            "Krtkův dort na plech s banánem", "Krtkova torta na plech s banánom", "Banana Chocolate Mole Dream Cake",
            "30 min", 420, 8, 48, 22,
            listOf("mouka", "vejce", "kakao", "smetana", "banán"), listOf("múka", "vajcia", "kakao", "smotana", "banán"), listOf("flour", "eggs", "cocoa", "cream", "banana"),
            "1. Upečte kakaové piškotové těsto. 2. Vydlabte vnitřek korpusu a vytvořte z něj drobky. 3. Na korpus naskládejte podélně rozkrojené banány, navrstvěte vyšlehanou šlehačku s kousky čokolády a zasypejte drobky.",
            "1. Upečte kakaový korpus. 2. Vydlabte vnútro a rozdrobte ho. 3. Na korpus poukladajte banány, navrstvite vyšľahau šľahačku a zasypte rozdrobeným cestom.",
            "1. Bake a rich chocolate cocoa sponge cake batter. 2. Hollow out cake center slightly, crumble extracted cake piece with fingers. 3. Lay halved sweet bananas, spread chilled whipped heavy cream mixed with chocolate shavings, sprinkle cookie crumbs over."
        )

        spec(
            "Vláčná mramorová bábovka", "Vláčna mramorová bábovka", "Classic Fluffy Marble Gugelhupf Cake",
            "45 min", 320, 6, 44, 14,
            listOf("mouka", "máslo", "vejce", "mléko", "kakao"), listOf("múka", "maslo", "vajcia", "mlieko", "kakao"), listOf("flour", "butter", "eggs", "milk", "cocoa"),
            "1. Utřete máslo s cukrem a žloutky, přidejte mouku, mléko a sníh z bílků. 2. Do třetiny těsta vmíchejte kakao. 3. Střídavě lijte do vymazané bábovkové formy a upečte dozlatova na 180°C.",
            "1. Vyšľahajte maslo s cukrom a žĺtkami, pridajte mlieko, múku a sneh. 2. Do tretiny cesta vmiešajte kakao. 3. Striedavo vlejte do formy a upečte.",
            "1. Whisk butter with sugar, yolks, add flour, milk and whipped egg whites foam. 2. Fold dark cocoa powder into one third of batter. 3. Alternating light and dark layers inside oiled bundt form, bake at 180°C."
        )

        spec(
            "Tradiční bublanina s třešněmi", "Tradičná bublanina s čerešňami", "Fluffy Summer Cherry Sponge Cake",
            "25 min", 280, 5, 41, 10,
            listOf("mouka", "vejce", "máslo", "mléko", "třešně"), listOf("múka", "vajcia", "maslo", "mlieko", "čerešne"), listOf("flour", "eggs", "butter", "milk", "cherries"),
            "1. Vyšlehejte žloutky s máslem a cukrem, přidejte mléko, hladkou mouku a nakonec sníh z bílků. 2. Těsto nalijte na vymazaný plech. 3. Bohatě poklaďte třešněmi a pečte dozlatova.",
            "1. Vyšľahajte žĺtky s maslom a cukrom, pridajte mlieko, múku a sneh z bielkov. 2. Cesto dajte na plech, posypte čerešňami a upečte.",
            "1. Beat egg yolks with softened butter and sugar, fold in milk, fine flour and egg whites whipped peek. 2. Spread onto a baking tray. 3. Dot thickly with pitted cherries and bake gold."
        )

        spec(
            "Tradiční litý perník s džemem", "Tradičný liaty perník s džemom", "Soft Spiced Gingerbread Tray Cake",
            "20 min", 310, 5, 49, 11,
            listOf("mouka", "mléko", "vejce", "kakao", "marmeláda"), listOf("múka", "mlieko", "vajcia", "kakao", "džem"), listOf("flour", "milk", "eggs", "cocoa", "jam"),
            "1. Smíchejte mouku, kypřicí prášek do perníku, cukr a kakao. 2. Přidejte mléko, olej a vejce, metličkou vyšlehejte do tekutějšího těsta. 3. Upečte na plechu, potřete džemem a polijte čokoládou.",
            "1. Zmiešajte múku, prášok do perníka, cukor a kakao. 2. Vmiešajte mlieko, olej a vajcia. 3. Upečte, potrite džemom a polejte čokoládou.",
            "1. Toss flour with baking soda gingerbread spice, sugar and baking cocoa. 2. Whisk in cold milk, vegetable oil, eggs to form a fluid batter. 3. Pour on tray, bake, coat with red jam."
        )

        spec(
            "Palačinky s ochuceným tvarohem", "Palacinky s teplým tvarohom", "Sweet Crepes with Creamy Curd Filling",
            "20 min", 340, 12, 45, 12,
            listOf("mouka", "mléko", "vejce", "tvaroh"), listOf("múka", "mlieko", "vajcia", "tvaroh"), listOf("flour", "milk", "eggs", "tvaroh"),
            "1. Z mouky, mléka a vejce vyšlehejte těsto a smažte palačinky na pánvi. 2. Tvaroh osladte, smíchejte se zakysanou smetanou a natřete na palačinky. 3. Srolujte a posypte cukrem.",
            "1. Z múky, mlieka a vajcia vyšľahajte cesto a usmažte palacinky. 2. Tvaroh osladený cukrom natrite na palacinky. 3. Zviňte a podávajte.",
            "1. Whisk milk, eggs, flour and pinch of salt to a smooth fluid crepe mix. 2. Pour onto dry pan and bake on both sides. 3. Spread sweetened curd cheese, roll and serve."
        )

        // --- Drinks ---
        spec(
            "Čerstvý mátový čaj s citronem a medem", "Čerstvý mätový čaj s citrónom a medom", "Fresh Hot Mint Tea with Honey",
            "8 min", 45, 0, 11, 0,
            listOf("čaj", "máta", "citron", "med"), listOf("čaj", "mäta", "citrón", "med"), listOf("tea", "mint", "lemon", "honey"),
            "1. Čerstvé snítky máty opláchněte a vložte do konvice. 2. Zalijte vroucí vodou a nechte louhovat 5 minut. 3. Dochuťte čerstvě vymačkaným citronem a lžičkou medu.",
            "1. Mätu opláchnite, vložte do konvice a dajte vriacu vodu. Nechajte lúhovať 5 minút. 2. Podávajte ochutené citrónom a včelím medom.",
            "1. Rinse fresh mint stems and put in teapot. 2. Steep in boiling water for 5-7 minutes. 3. Stir in fresh squeezed lemon slice and organic run honey."
        )

        spec(
            "Ledová káva s vanilkovou zmrzlinou", "Ľadová káva s vanilkovou zmrzlinou", "Creamy Affogato Iced Coffee with Cream",
            "5 min", 220, 4, 18, 14,
            listOf("káva", "mléko", "zmrzlina"), listOf("káva", "mlieko", "zmrzlina"), listOf("coffee", "milk", "ice cream"),
            "1. Připravte si silnou kávu (espresso nebo instantní) a nechte ji zchladnout. 2. Do vysoké sklenice dejte dva kopečky vanilkové zmrzliny a zalijte studeným mlékem. 3. Přelijte připravenou kávou a ozdobte šlehačkou.",
            "1. Uvarte silnú kávu a nechajte ju vychladnúť. 2. Do pohára dajte vanilkovú zmrzlinu a zalejte mliekom. 3. Pomaly prelejte kávou a podávajte.",
            "1. Brew strong black espresso or instant coffee and let cool. 2. In a tall glass, deposit two scoops of vanilla bean ice cream and pour in cold whole milk. 3. Top with coffee float."
        )

        spec(
            "Hustá horká čokoláda se šlehačkou", "Hustá horúca čokoláda so šľahačkou", "Gourmet Hot Chocolate with Whipped Cream",
            "10 min", 380, 6, 28, 26,
            listOf("čokoláda", "mléko", "smetana"), listOf("čokoláda", "mlieko", "smotana"), listOf("chocolate", "milk", "cream"),
            "1. Do kastrůlku nalijte mléko a trošku smetany ke šlehání, zahřejte těsně pod bod varu. 2. Nalámejte kvalitní čokoládu na vaření a nechte ji zcela rozpustit za stálého míchání metličkou. 3. Podávejte s kopečkem šlehačky.",
            "1. V kastróliku zohrejte mlieko a smotanu. 2. Rozpustite polámanú čokoládu na varenie a metličkou vyšľahajte dohladka. 3. Podávajte so šľahačkou.",
            "1. Heat whole milk with a dash of heavy cream in a saucepan, bring close to boil. 2. Break baking chocolate bar and whisk until thick and fully dissolved. 3. Pour into a mug, top with whipped cream."
        )

        spec(
            "Osvěžující jahodový mléčný koktejl", "Osviežujúci jahodový mliečny kokteil", "Sweet Strawberry Milkshake",
            "5 min", 195, 5, 24, 7,
            listOf("jahody", "mléko"), listOf("jahody", "mlieko"), listOf("strawberries", "milk"),
            "1. Očistěte jahody a vložte je do mixéru. 2. Přidejte ledově vychlazené mléko (podle chuti lžíci cukru). 3. Rozmixujte zcela dohladka.",
            "1. Očistite jahody a dajte do mixéra. 2. Pridajte vychladené mlieko a rozmixujte do peny.",
            "1. Clean ripe red strawberries and drop into speed blender. 2. Pour ice-cold fresh milk. 3. Blitz at high power until creamy, frothy and smooth."
        )

        // Procedural Generation of 100 high-quality CZ / SK / EN distinct culinary recipes
        val bases: List<String>
        val baseIngs: List<String>
        val styles: List<String>
        val styleIngs: List<List<String>>
        val sides: List<String>
        val sideIngs: List<List<String>>

        if (lang == "EN") {
            bases = listOf("Chicken Breast", "Tender Beef", "Juicy Pork", "Smoked Tofu", "Fresh Salmon", "Roast Turkey", "Boiled Eggs", "Baked Trout", "Creamy Brie", "Pan-seared Cod")
            baseIngs = listOf("chicken breast", "beef", "pork", "tofu", "salmon", "turkey", "eggs", "trout", "brie cheese", "cod fillet")
            
            styles = listOf("with garlic butter", "in creamy sauce", "with aromatic herbs", "slow-cooked in red wine", "spiced with paprika", "under a cheese crust", "with fresh mushrooms", "glazed with honey mustard", "with fresh rosemary", "seasoned with black pepper")
            styleIngs = listOf(listOf("butter", "garlic"), listOf("heavy cream"), listOf("mixed herbs"), listOf("red wine"), listOf("sweet paprika"), listOf("cheddar cheese"), listOf("mushrooms"), listOf("honey", "mustard"), listOf("fresh rosemary"), listOf("black pepper"))
            
            sides = listOf("served with mashed potatoes", "with steamed white rice", "on a bed of pasta", "with roasted root vegetables", "with crusty garlic bread", "with fresh garden salad", "with sweet potato fries", "served with warm quinoa", "with seasoned bulgur", "with buttery couscous")
            sideIngs = listOf(listOf("potatoes", "milk"), listOf("white rice"), listOf("pasta"), listOf("root vegetables"), listOf("garlic bread"), listOf("garden salad"), listOf("sweet potatoes"), listOf("quinoa"), listOf("bulgur"), listOf("couscous"))
        } else if (isSlovak) {
            bases = listOf("Kuracie supreme", "Hovädzí plátok", "Bravčová panenka", "Údené tofu", "Čerstvý losos", "Pečená moriaka", "Pražené vajcia", "Pečený pstruh", "Grilovaný oštiepok", "Smažená treska")
            baseIngs = listOf("kuracie mäso", "hovädzie mäso", "bravčové mäso", "tofu", "čerstvý losos", "morčacie mäso", "vajcia", "čerstvý pstruh", "oštiepok", "treska")
            
            styles = listOf("na cesnakovom masle", "v jemnej smotanovej omáčke", "s voňavými bylinkami", "pomaly dusené na červenom víne", "na sladkej paprike", "zapečené pod syrovou perinou", "s lesnými hubami", "s medovo-horčicovým prelivom", "s čerstvým rozmarínom", "pikantné s drveným korením")
            styleIngs = listOf(listOf("maslo", "cesnak"), listOf("smotana na varenie"), listOf("bylinky"), listOf("červené víno"), listOf("sladká paprika"), listOf("syr eidam"), listOf("sušené huby"), listOf("med", "horčica"), listOf("rozmarín"), listOf("chilli"))
            
            sides = listOf("s nadýchanou zemiakovou kašou", "s dusenou jasmínovou ryžou", "s varenými cestovinami", "s pečenou koreňovou zeleninou", "s chrumkavým cesnakovým chlebom", "s čerstvým záhradným šalátom", "s pečenými batátovými hranolkami", "s teplou quinoou", "s ochuteným bulgurom", "s maslovým kuskusom")
            sideIngs = listOf(listOf("zemiaky", "mlieko"), listOf("ryža"), listOf("cestoviny"), listOf("koreňová zelenina"), listOf("chlieb"), listOf("listový šalát"), listOf("batáty"), listOf("quinoa"), listOf("bulgur"), listOf("kuskus"))
        } else {
            bases = listOf("Kuřecí supreme", "Hovězí plátky", "Vepřová panenka", "Uzené tofu", "Čerstvý losos", "Pečená krůta", "Smažená vejce", "Pečený pstruh", "Grilovaný hermelín", "Smažená treska")
            baseIngs = listOf("kuřecí maso", "hovězí maso", "vepřové maso", "tofu", "čerstvý losos", "krůtí maso", "vejce", "čerstvý pstruh", "hermelín", "treska")
            
            styles = listOf("na česnekovém másle", "v jemné smetanové omáčce", "s voňavými bylinkami", "pomalu dušené na červeném víně", "na sladké paprice", "zapečené pod sýrovou peřinkou", "s lesními houbami", "s medovo-hořčičným přelivem", "s čerstvým rozmarýnem", "pikantní s drceným pepřem")
            styleIngs = listOf(listOf("máslo", "česnek"), listOf("smetana na vaření"), listOf("bylinky"), listOf("červené víno"), listOf("sladká paprika"), listOf("eidam"), listOf("sušené houby"), listOf("med", "hořčice"), listOf("rozmarýn"), listOf("chilli"))
            
            sides = listOf("s nadýchanou bramborovou kaší", "s dušenou jasmínovou rýží", "s vařenými těstovinami", "s pečenou kořenovou zeleninou", "s křupavým česnekovým chlebem", "s čerstvým zahradním salátem", "s pečenými batátovými hranolky", "s teplou quinoou", "s ochuceným bulgurem", "s máslovým kuskusem")
            sideIngs = listOf(listOf("brambory", "mléko"), listOf("rýže"), listOf("těstoviny"), listOf("kořenová zelenina"), listOf("chléb"), listOf("listový salát"), listOf("batáty"), listOf("quinoa"), listOf("bulgur"), listOf("kuskus"))
        }

        for (b in 0 until 10) {
            for (s in 0 until 10) {
                val sideIndex = (b + s) % 10
                
                val title = "${bases[b]} ${styles[s]} ${sides[sideIndex]}"
                val baseIng = baseIngs[b]
                val styleIngList = styleIngs[s]
                val sideIngList = sideIngs[sideIndex]
                
                // Track matching & missing ingredients from user's current pantry
                val requiredAll = mutableListOf<String>()
                requiredAll.add(baseIng)
                requiredAll.addAll(styleIngList)
                requiredAll.addAll(sideIngList)
                
                val matching = mutableListOf<String>()
                val missing = mutableListOf<String>()
                
                for (ing in requiredAll) {
                    val isMatched = itemNames.any { it.contains(ing.lowercase()) || ing.lowercase().contains(it) }
                    if (isMatched) {
                        matching.add(ing)
                    } else {
                        missing.add(ing)
                    }
                }
                
                // Procedural calculation of dynamic statistics
                val missingPrice = missing.size * (if (isSlovak) 1.50 else if (lang == "EN") 1.80 else 35.0)
                val prepTime = "${15 + b * 2 + (s % 3) * 5} min"
                val calories = 250 + b * 45 + s * 12
                val protein = 12 + b * 3 + (s % 3) * 2
                val carbs = 8 + s * 6 + (b % 4) * 3
                val fat = 6 + b * 2 + s * 2
                
                val instructions = when (lang) {
                    "EN" -> "1. Prepare the $baseIng and clean it well.\n2. Cook the main element following the '${styles[s]}' preparation. Adjust salt and seasoning to taste.\n3. Serve warm accompanied by '${sides[sideIndex]}'. Enjoy your meal!"
                    else -> if (isSlovak) {
                        "1. Pripravte si $baseIng a očistite ho.\n2. Následne pokrm upravte podľa postupu '${styles[s]}'. Dbajte na správnu teplotu a solenie.\n3. Podávajte teplé spolu s prílohou '${sides[sideIndex]}'. Dobrú chuť!"
                    } else {
                        "1. Připravte si $baseIng a očistěte ho.\n2. Následně pokrm upravte podle postupu '${styles[s]}'. Dbejte na správnou teplotu a solení.\n3. Podávejte teplé spolu s přílohou '${sides[sideIndex]}'. Dobrou chuť!"
                    }
                }
                
                list.add(LocalRecipe(
                    title = title,
                    time = prepTime,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    matchingIngredients = matching,
                    missingIngredients = missing,
                    missingPrice = if (missingPrice == 0.0) 0.0 else missingPrice,
                    instructions = instructions
                ))
            }
        }

        // Sort items by count of matching ingredients we present in stock to make perfect recommendations
        return list.sortedByDescending { recipe ->
            recipe.matchingIngredients.count { rName ->
                itemNames.any { it.contains(rName) }
            }
        }
    }
}

data class LocalRecipe(
    val title: String,
    val time: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val matchingIngredients: List<String>,
    val missingIngredients: List<String>,
    val missingPrice: Double,
    val instructions: String
)
