package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel

@Composable
fun RecipeScreen(
    viewModel: FridgeBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val isSlovak by viewModel.isSlovak.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiRecipeMarkdown by viewModel.aiRecipeMarkdown.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()

    var activeScreenTab by remember { mutableStateOf("obed") } // "obed", "vecere", "dezert", "piti", "ChefAI"
    var customQueryText by remember { mutableStateOf("") }
    var searchRecipeQuery by remember { mutableStateOf("") }

    // Advanced search states
    var includeIngredients by remember { mutableStateOf("") }
    var excludeIngredients by remember { mutableStateOf("") }

    var selectedMealCategory by remember { mutableStateOf("vse") } // "vse", "polevky", "hlavni", "snidane", "dezerty", "chutovky"
    var selectedOccasion by remember { mutableStateOf("vse") } // "vse", "rychly_obed", "slavnostni_vecere", "vanoce", "velikonoce", "grilovani"

    var filterGlutenFree by remember { mutableStateOf(false) }
    var filterVegetarian by remember { mutableStateOf(false) }
    var filterVegan by remember { mutableStateOf(false) }
    var filterLactoseFree by remember { mutableStateOf(false) }
    var filterLowCarb by remember { mutableStateOf(false) }
    var filterCanMakeFromPantry by remember { mutableStateOf(false) }

    var maxCaloriesLimit by remember { mutableStateOf<Int?>(null) } // null means no limit
    var filterHighProtein by remember { mutableStateOf(false) }

    var maxPrepTimeCategory by remember { mutableStateOf("vse") } // "vse", "15m", "30m", "60m", "long"
    var selectedDifficulty by remember { mutableStateOf("vse") } // "vse", "zacatecnik", "pokrocily", "masterchef"

    var selectedPrepMethod by remember { mutableStateOf("vse") } // "vse", "vareni", "peceni", "smazeni", "duseni", "grilovani", "studena"
    var selectedCuisine by remember { mutableStateOf("vse") } // "vse", "ceska", "regionalni", "asijska", "italska", "retro"

    // Drink-specific filter states
    var selectedAlcoholType by remember { mutableStateOf("vse") } // "vse", "Rum", "Tequila", "Gin", "Vodka", "Whisky", "Prosecco", "Likér"
    var selectedAlcoholStrength by remember { mutableStateOf("vse") } // "vse", "slaby", "stredni", "silny"
    var selectedDrinkCategory by remember { mutableStateOf("vse") } // "vse", "Klasické koktejly", "Letní drinky", "Long drinky", "Shoty", "Zimní drinky"

    val hasActiveFilters = includeIngredients.isNotBlank() || excludeIngredients.isNotBlank() ||
        filterCanMakeFromPantry ||
        (activeScreenTab != "alko" && (selectedMealCategory != "vse" || selectedOccasion != "vse" ||
        filterGlutenFree || filterVegetarian || filterVegan || filterLactoseFree || filterLowCarb ||
        maxCaloriesLimit != null || filterHighProtein ||
        selectedPrepMethod != "vse" || selectedCuisine != "vse")) ||
        (activeScreenTab == "alko" && (selectedAlcoholType != "vse" || selectedAlcoholStrength != "vse" || selectedDrinkCategory != "vse")) ||
        maxPrepTimeCategory != "vse" || selectedDifficulty != "vse"

    // Multi-Language Strings
    val screenTitleObj = if (isSlovak) "Inteligentný Kulinársky Generátor" else "Inteligentní Kulinářský Generátor"
    val subTitleObj = if (isSlovak) "Recepty navrhované presne podľa obsahu vašej chladničky." else "Recepty navrhované přesně podle obsahu vaší lednice."
    val fridgeStatusText = if (isSlovak) "🟢 Chladnička" else "🟢 Lednice"

    // Raw database of recipes supporting the requested categories loaded from our optimized provider class:
    val localRecipes = remember {
        LocalRecipesProvider.getRecipes()
    }

    val pantryNames = pantryItems.map { it.name.lowercase() }

    // Filter local recipe list based on category and search query selected
    val filteredRecipes = localRecipes.filter { r ->
        if (r.category != activeScreenTab) return@filter false

        // Ingredients
        val titleCZ = r.czTitle.lowercase()
        val titleSK = r.skTitle.lowercase()
        val descCZ = r.instructionsCZ.lowercase()
        val descSK = r.instructionsSK.lowercase()
        val ingsCZ = r.ingredientsCZ.map { it.lowercase() }
        val ingsSK = r.ingredientsSK.map { it.lowercase() }

        // "Mám doma" filter — zobrazí jen recepty kde pantry pokrývá alespoň 50% ingrediencí
        if (filterCanMakeFromPantry && pantryNames.isNotEmpty()) {
            val recipeIngs = (r.ingredientsCZ + r.ingredientsSK).map { it.lowercase() }
            if (recipeIngs.isNotEmpty()) {
                val covered = recipeIngs.count { ing ->
                    pantryNames.any { p -> ing.contains(p) || p.contains(ing.take(4)) }
                }
                if (covered.toFloat() / recipeIngs.size < 0.5f) return@filter false
            }
        }

        if (includeIngredients.isNotBlank()) {
            val incParts = includeIngredients.trim().lowercase().split(",".toRegex())
                .map { it.trim() }.filter { it.isNotEmpty() }
            if (incParts.isNotEmpty()) {
                val matchesAll = incParts.all { part ->
                    titleCZ.contains(part) || titleSK.contains(part) ||
                    ingsCZ.any { it.contains(part) } || ingsSK.any { it.contains(part) }
                }
                if (!matchesAll) return@filter false
            }
        }

        if (excludeIngredients.isNotBlank()) {
            val excParts = excludeIngredients.trim().lowercase().split(",".toRegex())
                .map { it.trim() }.filter { it.isNotEmpty() }
            if (excParts.isNotEmpty()) {
                val matchesAny = excParts.any { part ->
                    titleCZ.contains(part) || titleSK.contains(part) ||
                    ingsCZ.any { it.contains(part) } || ingsSK.any { it.contains(part) }
                }
                if (matchesAny) return@filter false
            }
        }

        // Meal Category
        if (selectedMealCategory != "vse") {
            val matchesCat = when (selectedMealCategory) {
                "polevky" -> {
                    titleCZ.contains("polévka") || titleCZ.contains("vývar") || titleCZ.contains("krém") || titleCZ.contains("zelňačka") || titleCZ.contains("kyselo") || titleCZ.contains("demikát") ||
                    titleSK.contains("polievka") || titleSK.contains("kapustnica")
                }
                "hlavni" -> {
                    (r.category == "obed" || r.category == "vecere") &&
                    !(titleCZ.contains("polévka") || titleCZ.contains("vývar") || titleCZ.contains("krém") || titleCZ.contains("zelňačka") || titleCZ.contains("kyselo") || titleCZ.contains("demikát") ||
                      titleSK.contains("polievka") || titleSK.contains("kapustnica")) &&
                    !(titleCZ.contains("salát") || titleCZ.contains("šalát") || titleCZ.contains("pomazán") || titleCZ.contains("nátier") || titleCZ.contains("chléb") || titleCZ.contains("chlieb") || titleCZ.contains("utopen") || titleCZ.contains("hermelín") || titleCZ.contains("tlačenka"))
                }
                "snidane" -> {
                    titleCZ.contains("kaše") || titleCZ.contains("kaša") || titleCZ.contains("lívanc") || titleCZ.contains("palačink") || titleCZ.contains("palacink") || titleCZ.contains("toast") || titleCZ.contains("pomazán") || titleCZ.contains("nátier") || titleCZ.contains("vajíč") || titleCZ.contains("vejce") || titleCZ.contains("chléb") || titleCZ.contains("chlieb") || titleCZ.contains("vafle") || titleCZ.contains("wafle")
                }
                "dezerty" -> {
                    r.category == "dezert" || titleCZ.contains("bábovka") || titleCZ.contains("závin") || titleCZ.contains("štrúdl") || titleCZ.contains("koláč") || titleCZ.contains("perník") || titleCZ.contains("buchty") || titleCZ.contains("dort") || titleCZ.contains("mák") || titleCZ.contains("mak ") || titleCZ.contains("sladk") || titleCZ.contains("větrník") || titleCZ.contains("laskonky") || titleCZ.contains("koláč")
                }
                "chutovky" -> {
                    titleCZ.contains("utopen") || titleCZ.contains("hermelín") || titleCZ.contains("tlačen") || titleCZ.contains("chlebíč") || titleCZ.contains("tatar") || titleCZ.contains("klobás") || titleCZ.contains("párk") || titleCZ.contains("bramborák") || titleCZ.contains("topink") || titleCZ.contains("chips") || titleCZ.contains("chipsy") || titleCZ.contains("slané") || titleCZ.contains("piv") || titleCZ.contains("vín")
                }
                else -> true
            }
            if (!matchesCat) return@filter false
        }

        // Occasion
        if (selectedOccasion != "vse") {
            val matchesOcc = when (selectedOccasion) {
                "rychly_obed" -> {
                    r.category == "obed" && (r.time.contains("5 min") || r.time.contains("10 min") || r.time.contains("15 min") || r.time.contains("20 min") || r.time.contains("25 min") || r.time.contains("30 min"))
                }
                "slavnostni_vecere" -> {
                    r.category == "vecere" && (titleCZ.contains("steak") || titleCZ.contains("tatar") || titleCZ.contains("kachna") || titleCZ.contains("husa") || titleCZ.contains("panenk") || titleCZ.contains("losos") || titleCZ.contains("pstruh") || titleCZ.contains("svíčkov") || r.basePrice > 50.0)
                }
                "vanoce" -> {
                    titleCZ.contains("kapr") || titleCZ.contains("salát") || titleCZ.contains("vánoč") || titleCZ.contains("cukrov") || titleCZ.contains("perníč") || titleCZ.contains("kuba") ||
                    titleSK.contains("vianoč") || titleSK.contains("kapustnica")
                }
                "velikonoce" -> {
                    titleCZ.contains("berán") || titleCZ.contains("nádiv") || titleCZ.contains("vejce") || titleCZ.contains("vajíč") ||
                    titleSK.contains("barán") || titleSK.contains("veľkon")
                }
                "grilovani" -> {
                    titleCZ.contains("gril") || titleCZ.contains("steak") || titleCZ.contains("špíz") || titleCZ.contains("hermelín") || titleCZ.contains("klobás") || titleCZ.contains("burger") || titleCZ.contains("panenk") || titleCZ.contains("pstruh")
                }
                else -> true
            }
            if (!matchesOcc) return@filter false
        }

        // Diets
        if (filterGlutenFree && !r.isGlutenFree) return@filter false
        if (filterVegetarian && !r.isVegetarian) return@filter false
        if (filterVegan && !r.isVegan) return@filter false

        if (filterLactoseFree) {
            val hasLactose = titleCZ.contains("mlék") || titleCZ.contains("másl") || titleCZ.contains("smetan") || titleCZ.contains("sýr") || titleCZ.contains("tvaroh") || titleCZ.contains("jogurt") || titleCZ.contains("bryndz") || titleCZ.contains("hermelín") ||
                             titleSK.contains("mliek") || titleSK.contains("masl") || titleSK.contains("smotan") || titleSK.contains("syr") ||
                             ingsCZ.any { it.contains("mlék") || it.contains("másl") || it.contains("smetan") || it.contains("sýr") || it.contains("tvaroh") || it.contains("jogurt") || it.contains("bryndz") || it.contains("hermelín") } ||
                             ingsSK.any { it.contains("mliek") || it.contains("masl") || it.contains("smotan") || it.contains("syr") }
            if (hasLactose) return@filter false
        }

        if (filterLowCarb) {
            val carbVal = r.carbs.filter { it.isDigit() }.toIntOrNull() ?: 100
            if (carbVal > 20) return@filter false
        }

        if (maxCaloriesLimit != null) {
            if (r.calories > maxCaloriesLimit!!) return@filter false
        }

        if (filterHighProtein) {
            val protVal = r.protein.filter { it.isDigit() }.toIntOrNull() ?: 0
            if (protVal < 20) return@filter false
        }

        // Time and Difficulty
        if (maxPrepTimeCategory != "vse") {
            val timeInt = r.time.filter { it.isDigit() }.toIntOrNull() ?: 45
            val matchesTime = when (maxPrepTimeCategory) {
                "15m" -> timeInt <= 15
                "30m" -> timeInt <= 30
                "60m" -> timeInt <= 60
                "long" -> timeInt > 120 || r.time.contains("hod") || r.time.contains("hodiny") || r.time.contains("hodin")
                else -> true
            }
            if (!matchesTime) return@filter false
        }

        if (selectedDifficulty != "vse") {
            if (r.category == "alko") {
                val matchesDiff = when (selectedDifficulty) {
                    "zacatecnik" -> r.difficultyCZ.lowercase() == "začátečník" || r.difficultySK.lowercase() == "začiatočník"
                    "pokrocily" -> r.difficultyCZ.lowercase() == "pokročilý" || r.difficultySK.lowercase() == "pokročilý"
                    "masterchef" -> r.difficultyCZ.lowercase().contains("master") || r.difficultySK.lowercase().contains("master")
                    else -> true
                }
                if (!matchesDiff) return@filter false
            } else {
                val matchesDiff = when (selectedDifficulty) {
                    "zacatecnik" -> {
                        r.instructionsCZ.length < 320 || titleCZ.contains("toust") || titleCZ.contains("toast") || titleCZ.contains("pomazánka") || titleCZ.contains("čaj") || titleCZ.contains("salát")
                    }
                    "pokrocily" -> {
                        (r.instructionsCZ.length in 320..600) || titleCZ.contains("polévka") || titleCZ.contains("koláč") || titleCZ.contains("guláš")
                    }
                    "masterchef" -> {
                        r.instructionsCZ.length > 600 || titleCZ.contains("svíčkov") || titleCZ.contains("kachna") || titleCZ.contains("steak") || titleCZ.contains("tatar") || titleCZ.contains("husa")
                    }
                    else -> true
                }
                if (!matchesDiff) return@filter false
            }
        }

        // Prep Technology (non-alko drinks/foods)
        if (activeScreenTab != "alko" && selectedPrepMethod != "vse") {
            val matchesTech = when (selectedPrepMethod) {
                "vareni" -> {
                    descCZ.contains("vař") || descCZ.contains("uvar") || descCZ.contains("vývar") || titleCZ.contains("polévka") || titleCZ.contains("krém") || descSK.contains("var")
                }
                "peceni" -> {
                    descCZ.contains("peč") || descCZ.contains("troub") || descCZ.contains("upec") || titleCZ.contains("bábovk") || titleCZ.contains("koláč") || descSK.contains("peč")
                }
                "smazeni" -> {
                    descCZ.contains("smaž") || descCZ.contains("pánev") || descCZ.contains("panvi") || titleCZ.contains("řízek") || titleCZ.contains("palačink") || titleCZ.contains("lívanc")
                }
                "duseni" -> {
                    descCZ.contains("dus") || descCZ.contains("podlej") || titleCZ.contains("guláš") || titleCZ.contains("soté")
                }
                "grilovani" -> {
                    descCZ.contains("gril") || titleCZ.contains("gril") || titleCZ.contains("špíz") || titleCZ.contains("steak")
                }
                "studena" -> {
                    descCZ.contains("studen") || titleCZ.contains("salát") || titleCZ.contains("tatar") || titleCZ.contains("pomazán") || titleCZ.contains("utopen") || titleCZ.contains("chlebíč")
                }
                else -> true
            }
            if (!matchesTech) return@filter false
        }

        // Cuisine style (non-alko drinks/foods)
        if (activeScreenTab != "alko" && selectedCuisine != "vse") {
            val matchesCuisine = when (selectedCuisine) {
                "ceska" -> {
                    titleCZ.contains("svíčkov") || titleCZ.contains("guláš") || titleCZ.contains("knedlí") || titleCZ.contains("řízek") || titleCZ.contains("kapr") || titleCZ.contains("bramborák") || titleCZ.contains("koláč") || titleCZ.contains("bábovk") || titleCZ.contains("koprov") || titleCZ.contains("rajsk") || titleCZ.contains("segedín")
                }
                "regionalni" -> {
                    titleCZ.contains("kuba") || titleCZ.contains("kulajda") || titleCZ.contains("kysel") || titleSK.contains("halušk") || titleSK.contains("piroh") || titleSK.contains("bryndz")
                }
                "asijska" -> {
                    titleCZ.contains("asij") || titleCZ.contains("sushi") || titleCZ.contains("curry") || titleCZ.contains("kari") || titleCZ.contains("nudle") || titleCZ.contains("wok") || titleCZ.contains("ramen") || titleCZ.contains("rýžov")
                }
                "italska" -> {
                    titleCZ.contains("těstovin") || titleCZ.contains("pasta") || titleCZ.contains("pizza") || titleCZ.contains("lasagn") || titleCZ.contains("risott") || titleCZ.contains("caprese") || titleCZ.contains("boloň") || titleCZ.contains("penne") || titleCZ.contains("gnocchi") || titleCZ.contains("špaget")
                }
                "retro" -> {
                    titleCZ.contains("utopen") || titleCZ.contains("vlašsk") || titleCZ.contains("tlačen") || titleCZ.contains("chlebíč") || titleCZ.contains("pomazán") || titleCZ.contains("hermelín")
                }
                else -> true
            }
            if (!matchesCuisine) return@filter false
        }

        // Drink specific filters
        if (activeScreenTab == "alko" && selectedAlcoholType != "vse") {
            if (!r.alcoholType.lowercase().contains(selectedAlcoholType.lowercase())) {
                return@filter false
            }
        }

        if (activeScreenTab == "alko" && selectedAlcoholStrength != "vse") {
            val isStrengthMatch = when (selectedAlcoholStrength) {
                "slaby" -> r.alcoholStrengthCZ.lowercase() == "slabý" || r.alcoholStrengthSK.lowercase() == "slabý"
                "stredni" -> r.alcoholStrengthCZ.lowercase() == "střední" || r.alcoholStrengthSK.lowercase() == "stredný"
                "silny" -> r.alcoholStrengthCZ.lowercase() == "silný" || r.alcoholStrengthSK.lowercase() == "silný"
                else -> true
            }
            if (!isStrengthMatch) return@filter false
        }

        if (activeScreenTab == "alko" && selectedDrinkCategory != "vse") {
            if (!r.drinkCategory.lowercase().contains(selectedDrinkCategory.lowercase())) {
                return@filter false
            }
        }

        // Standard text query search including drink-specific attributes too
        if (searchRecipeQuery.isNotBlank()) {
            val queryParts = searchRecipeQuery.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            if (queryParts.isNotEmpty()) {
                val matchAllParts = queryParts.all { part ->
                    titleCZ.contains(part) || 
                    titleSK.contains(part) || 
                    ingsCZ.any { it.contains(part) } || 
                    ingsSK.any { it.contains(part) } ||
                    // Drink attributes
                    (r.category == "alko" && (
                        r.descriptionCZ.lowercase().contains(part) ||
                        r.descriptionSK.lowercase().contains(part) ||
                        r.glassCZ.lowercase().contains(part) ||
                        r.glassSK.lowercase().contains(part) ||
                        r.difficultyCZ.lowercase().contains(part) ||
                        r.difficultySK.lowercase().contains(part) ||
                        r.alcoholStrengthCZ.lowercase().contains(part) ||
                        r.alcoholStrengthSK.lowercase().contains(part) ||
                        r.alcoholVol.lowercase().contains(part) ||
                        r.alcoholType.lowercase().contains(part) ||
                        r.drinkCategory.lowercase().contains(part) ||
                        r.garnishCZ.lowercase().contains(part) ||
                        r.garnishSK.lowercase().contains(part)
                    ))
                }
                if (!matchAllParts) return@filter false
            }
        }
        true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 1. HEADER AREA WITH BRANDING & FRIDGE PILL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = screenTitleObj,
                        color = FreshGreenPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subTitleObj,
                        color = CaptionTextNatural,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                
                // Status indicator button "🟢 Lednice"
                Button(
                    onClick = { /* Status action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftGreenGlow,
                        contentColor = FreshGreenPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = fridgeStatusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Category-focused navigation tabs (Obědy, Večeře, Dezerty a buchty, Nápoje, Drinky)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    Triple("obed", "Obědy 🍲", "Obedy 🍲"),
                    Triple("vecere", "Večeře 🌙", "Večera 🌙"),
                    Triple("dezert", "Dezerty a buchty 🍰", "Dezerty a buchty 🍰"),
                    Triple("piti", "Nápoje 🍹", "Nápoje 🍹"),
                    Triple("alko", "Drinky 🍸", "Drinky 🍸"),
                    Triple("ChefAI", "✨ Chef AI", "✨ Chef AI")
                )

                tabs.forEach { (tabKey, nameCZ, nameSK) ->
                    val label = if (isSlovak) nameSK else nameCZ
                    val isSelected = activeScreenTab == tabKey
                    Button(
                        onClick = { activeScreenTab = tabKey },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) FreshGreenPrimary else NavBgNatural,
                            contentColor = if (isSelected) Color.White else MutedTextNatural
                        ),
                        modifier = Modifier
                            .height(38.dp),
                        shape = RoundedCornerShape(19.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. MAIN SCROLLABLE WRAPPER
        var showAdvancedFilters by remember { mutableStateOf(false) }

        when (activeScreenTab) {
            "obed", "vecere", "dezert", "piti", "alko" -> {
                // Search Row with Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchRecipeQuery,
                        onValueChange = { searchRecipeQuery = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CaptionTextNatural
                            )
                        },
                        trailingIcon = {
                            if (searchRecipeQuery.isNotEmpty()) {
                                IconButton(onClick = { searchRecipeQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = CaptionTextNatural
                                    )
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (isSlovak) "Hľadať recept alebo surovinu..." else "Hledat recept nebo surovinu...",
                                color = CaptionTextNatural.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipe_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CreamText,
                            unfocusedTextColor = CreamText,
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = BorderNatural,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )

                    // Expanded filter button
                    IconButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (showAdvancedFilters || hasActiveFilters) FreshGreenPrimary else DarkSurface,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (showAdvancedFilters || hasActiveFilters) Color.White else CreamText
                        )
                    }
                }

                // "Mám doma" quick filter chip
                if (pantryNames.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterCanMakeFromPantry,
                            onClick = { filterCanMakeFromPantry = !filterCanMakeFromPantry },
                            label = {
                                Text(
                                    text = if (isSlovak) "🧺 Mám doma" else "🧺 Mám doma",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = if (filterCanMakeFromPantry) ({
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }) else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshGreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = DarkSurface,
                                labelColor = CreamText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterCanMakeFromPantry,
                                borderColor = BorderNatural,
                                selectedBorderColor = FreshGreenPrimary
                            )
                        )
                    }
                }

                // Collapsible Advanced Filters Panel
                AnimatedVisibility(visible = showAdvancedFilters) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = if (hasActiveFilters) BorderStroke(1.dp, FreshGreenPrimary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header: Title & Reset Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isSlovak) "Rozšírené filtre 🍳" else "Rozšířené filtry 🍳",
                                    color = CreamText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (hasActiveFilters) {
                                    TextButton(
                                        onClick = {
                                            includeIngredients = ""
                                            excludeIngredients = ""
                                            selectedMealCategory = "vse"
                                            selectedOccasion = "vse"
                                            filterGlutenFree = false
                                            filterVegetarian = false
                                            filterVegan = false
                                            filterLactoseFree = false
                                            filterLowCarb = false
                                            maxCaloriesLimit = null
                                            filterHighProtein = false
                                            maxPrepTimeCategory = "vse"
                                            selectedDifficulty = "vse"
                                            selectedPrepMethod = "vse"
                                            selectedCuisine = "vse"
                                            selectedAlcoholType = "vse"
                                            selectedAlcoholStrength = "vse"
                                            selectedDrinkCategory = "vse"
                                            filterCanMakeFromPantry = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.textButtonColors(contentColor = FreshGreenPrimary)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isSlovak) "Reset" else "Resetovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 1. Podle ingrediencí
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isSlovak) "1. Ingrediencie" else "1. Ingredience",
                                    color = FreshGreenPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = includeIngredients,
                                    onValueChange = { includeIngredients = it },
                                    label = { Text(if (isSlovak) "Hľadať recepty obsahujúce (napr. hovädzie, mrkva)" else "Hledat recepty obsahující (např. hovězí, mrkev)", fontSize = 11.sp) },
                                    placeholder = { Text(if (isSlovak) "Oddelené čiarkami" else "Oddělené čárkami", fontSize = 11.sp, color = CaptionTextNatural.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = CreamText,
                                        unfocusedTextColor = CreamText,
                                        focusedBorderColor = FreshGreenPrimary,
                                        unfocusedBorderColor = BorderNatural,
                                        focusedLabelColor = FreshGreenPrimary,
                                        unfocusedLabelColor = CaptionTextNatural
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = excludeIngredients,
                                    onValueChange = { excludeIngredients = it },
                                    label = { Text(if (isSlovak) "Vylúčiť / Alergény (napr. orechy, zeler)" else "Vyloučit / Alergeny (např. ořechy, celer)", fontSize = 11.sp) },
                                    placeholder = { Text(if (isSlovak) "Skryť recepty s týmito ingredienciami" else "Skrýt recepty s těmito ingrediencemi", fontSize = 11.sp, color = CaptionTextNatural.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = CreamText,
                                        unfocusedTextColor = CreamText,
                                        focusedBorderColor = Color.Red.copy(alpha = 0.6f),
                                        unfocusedBorderColor = BorderNatural,
                                        focusedLabelColor = Color.Red.copy(alpha = 0.6f),
                                        unfocusedLabelColor = CaptionTextNatural
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }

                            if (activeScreenTab != "alko") {
                                // 2. Chod a Příležitost
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "2. Kategórie & Príležitosti" else "2. Kategorie & Příležitosti",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(if (isSlovak) "Kategórie jedál:" else "Kategorie jídel:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val categories = listOf(
                                            Triple("vse", "Všechny 🍽️", "Všetky 🍽️"),
                                            Triple("polevky", "Polévky 🍜", "Polievky 🍜"),
                                            Triple("hlavni", "Hlavní jídla 🥩", "Hlavné jedlá 🥩"),
                                            Triple("snidane", "Snídaně / Svačiny ☕", "Raňajky / Desiate ☕"),
                                            Triple("dezerty", "Dezerty & Moučníky 🍰", "Dezerty 🍰"),
                                            Triple("chutovky", "Chuťovky k pivu/vínu 🧀", "Chuťovky k pivu/vínu 🧀")
                                        )
                                        categories.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedMealCategory == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedMealCategory = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }

                                    Text(if (isSlovak) "Príležitosť:" else "Příležitost:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val occasions = listOf(
                                            Triple("vse", "Jakákoliv 📅", "Akákoľvek 📅"),
                                            Triple("rychly_obed", "Rychlý oběd ⚡", "Rýchly obed ⚡"),
                                            Triple("slavnostni_vecere", "Slavnostní večeře ✨", "Slávnostná večera ✨"),
                                            Triple("vanoce", "Vánoce 🎄", "Vianoce 🎄"),
                                            Triple("velikonoce", "Velikonoce 🐣", "Veľká noc 🐣"),
                                            Triple("grilovani", "Grilování 🔥", "Grilovanie 🔥")
                                        )
                                        occasions.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedOccasion == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedOccasion = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 3. Diety a nutriční hodnoty
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "3. Diéty a nutričné hodnoty" else "3. Diety a nutriční hodnoty",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ElevatedFilterChip(
                                            selected = filterGlutenFree,
                                            onClick = { filterGlutenFree = !filterGlutenFree },
                                            label = { Text("Bezlepkové 🌾", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                        ElevatedFilterChip(
                                            selected = filterLactoseFree,
                                            onClick = { filterLactoseFree = !filterLactoseFree },
                                            label = { Text("Bez laktózy 🥛", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                        ElevatedFilterChip(
                                            selected = filterVegetarian,
                                            onClick = { filterVegetarian = !filterVegetarian },
                                            label = { Text("Vegetariánské 🥦", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                        ElevatedFilterChip(
                                            selected = filterVegan,
                                            onClick = { filterVegan = !filterVegan },
                                            label = { Text("Veganské 🌱", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                        ElevatedFilterChip(
                                            selected = filterLowCarb,
                                            onClick = { filterLowCarb = !filterLowCarb },
                                            label = { Text("Low-carb 🥑", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                        ElevatedFilterChip(
                                            selected = filterHighProtein,
                                            onClick = { filterHighProtein = !filterHighProtein },
                                            label = { Text("Vysoký protein 💪", fontSize = 10.sp) },
                                            shape = RoundedCornerShape(30.dp),
                                            colors = FilterChipDefaults.elevatedFilterChipColors(selectedContainerColor = SoftGreenGlow, selectedLabelColor = FreshGreenPrimary, containerColor = DarkBg)
                                        )
                                    }

                                    Text(if (isSlovak) "Max kalórií:" else "Max kalorií:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val calorieOptions = listOf(
                                            null to (if (isSlovak) "Lubovoľné" else "Libovolné"),
                                            300 to "Do 300 kcal",
                                            500 to "Do 500 kcal",
                                            800 to "Do 800 kcal"
                                        )
                                        calorieOptions.forEach { (limit, name) ->
                                            val isSelected = maxCaloriesLimit == limit
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { maxCaloriesLimit = limit },
                                                label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 4. Náročnost a čas přípravy
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "4. Náročnosť a čas" else "4. Náročnost a čas",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(if (isSlovak) "Doba prípravy:" else "Doba přípravy:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val times = listOf(
                                            "vse" to (if (isSlovak) "Akákoľvek" else "Jakákoliv"),
                                            "15m" to "Do 15 minut",
                                            "30m" to "Do 30 minut",
                                            "60m" to "Do hodiny",
                                            "long" to (if (isSlovak) "Dlhé pečenie (>2h)" else "Dlouhé pečení (>2h)")
                                        )
                                        times.forEach { (key, name) ->
                                            val isSelected = maxPrepTimeCategory == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { maxPrepTimeCategory = key },
                                                label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }

                                    Text(if (isSlovak) "Náročnosť:" else "Náročnost:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val levels = listOf(
                                            Triple("vse", "Jakákoliv ⚔️", "Akákoľvek ⚔️"),
                                            Triple("zacatecnik", "Začátečník 🌱", "Začiatočník 🌱"),
                                            Triple("pokrocily", "Pokročilý ⚙️", "Pokročilý ⚙️"),
                                            Triple("masterchef", "Masterchef 👑", "Masterchef 👑")
                                        )
                                        levels.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedDifficulty == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedDifficulty = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 5. Technologie
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "5. Spôsob prípravy (Technológia)" else "5. Způsob přípravy (Technologie)",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val methods = listOf(
                                            Triple("vse", "Jakýkoliv 🍽️", "Akýkoľvek 🍽️"),
                                            Triple("vareni", "Vaření 🍲", "Varenie 🍲"),
                                            Triple("peceni", "Pečení 🥧", "Pečenie 🥧"),
                                            Triple("smazeni", "Smažení 🍳", "Vyprážanie 🍳"),
                                            Triple("duseni", "Dušení 🥩", "Dusenie 🥩"),
                                            Triple("grilovani", "Grilování ♨️", "Grilovanie ♨️"),
                                            Triple("studena", "Studená kuchyně 🥗", "Studená kuchyňa 🥗")
                                        )
                                        methods.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedPrepMethod == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedPrepMethod = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 6. Kuchyně
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "6. Pôvod a štýl kuchyne" else "6. Původ a styl kuchyně",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val cuisines = listOf(
                                            Triple("vse", "Jakýkoliv 🗺️", "Akýkoľvek 🗺️"),
                                            Triple("ceska", "Tradiční česká 🇨🇿", "Tradičná česká 🇨🇿"),
                                            Triple("regionalni", "Regionální 🏔️", "Regionálna 🏔️"),
                                            Triple("asijska", "Asijská 🍜", "Ázijská 🍜"),
                                            Triple("italska", "Italská 🍕", "Talianska 🍕"),
                                            Triple("retro", "Retro klasika ⚓", "Retro klasika ⚓")
                                        )
                                        cuisines.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedCuisine == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedCuisine = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // --- DRINKS SPECIFIC FILTERS ---
                                // 2. Typ alkoholu / Báza
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "2. Alkoholová báza" else "2. Alkoholová báze",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val alcoholTypes = listOf(
                                            "vse" to (if (isSlovak) "Všetko 🍶" else "Všechno 🍶"),
                                            "Rum" to "Rum 🥃",
                                            "Tequila" to "Tequila 🌵",
                                            "Gin" to "Gin 🍸",
                                            "Vodka" to "Vodka 🧪",
                                            "Whisky" to "Whisky 🪵",
                                            "Prosecco" to "Prosecco 🥂",
                                            "Likér" to "Likér 🧉"
                                        )
                                        alcoholTypes.forEach { (key, label) ->
                                            val isSelected = selectedAlcoholType == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedAlcoholType = key },
                                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 3. Síla / Obsah alkoholu
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "3. Sila / Obsah alkoholu" else "3. Síla / Obsah alkoholu",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val strengths = listOf(
                                            Triple("vse", "Jakákoliv ⚖️", "Akákoľvek ⚖️"),
                                            Triple("slaby", "Slabý (do 10%) 🍷", "Slabý (do 10%) 🍷"),
                                            Triple("stredni", "Střední (10-20%) 🍹", "Stredný (10-20%) 🍹"),
                                            Triple("silny", "Silný (nad 20%) 🥃", "Silný (nad 20%) 🥃")
                                        )
                                        strengths.forEach { (key, czName, skName) ->
                                            val isSelected = selectedAlcoholStrength == key
                                            val label = if (isSlovak) skName else czName
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedAlcoholStrength = key },
                                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 4. Kategorie koktejlu
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "4. Kategória koktailov" else "4. Kategorie koktejlů",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val drinkCats = listOf(
                                            "vse" to (if (isSlovak) "Všetky 🌍" else "Všechny 🌍"),
                                            "Klasické koktejly" to "Klasické ⚜️",
                                            "Letní drinky" to "Letní ☀️",
                                            "Zimní drinky" to "Zimní ❄️",
                                            "Shoty" to "Shoty 🎯",
                                            "Moderní koktejly" to "Moderní 🚀"
                                        )
                                        drinkCats.forEach { (key, label) ->
                                            val isSelected = selectedDrinkCategory == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedDrinkCategory = key },
                                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }

                                // 5. Náročnost a čas (SHARED & PERFECTLY COMPATIBLE FOR DRINKS!)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isSlovak) "5. Náročnosť a čas prípravy" else "5. Náročnost a čas přípravy",
                                        color = FreshGreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(if (isSlovak) "Doba prípravy:" else "Doba přípravy:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val times = listOf(
                                            "vse" to (if (isSlovak) "Akákoľvek" else "Jakákoliv"),
                                            "15m" to "Do 15 minut",
                                            "30m" to "Do 30 minut",
                                            "60m" to "Do hodiny"
                                        )
                                        times.forEach { (key, name) ->
                                            val isSelected = maxPrepTimeCategory == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { maxPrepTimeCategory = key },
                                                label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }

                                    Text(if (isSlovak) "Náročnosť:" else "Náročnost:", color = CaptionTextNatural, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val levels = listOf(
                                            Triple("vse", "Jakákoliv ⚔️", "Akákoľvek ⚔️"),
                                            Triple("zacatecnik", "Začátečník 🌱", "Začiatočník 🌱"),
                                            Triple("pokrocily", "Pokročilý ⚙️", "Pokročilý ⚙️"),
                                            Triple("masterchef", "Masterchef 👑", "Masterchef 👑")
                                        )
                                        levels.forEach { (key, nameCZ, nameSK) ->
                                            val isSelected = selectedDifficulty == key
                                            ElevatedFilterChip(
                                                selected = isSelected,
                                                onClick = { selectedDifficulty = key },
                                                label = { Text(if (isSlovak) nameSK else nameCZ, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                shape = RoundedCornerShape(30.dp),
                                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                                    selectedContainerColor = SoftGreenGlow,
                                                    selectedLabelColor = FreshGreenPrimary,
                                                    containerColor = DarkBg,
                                                    labelColor = CreamText
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary status of active filters and matches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasActiveFilters) {
                            if (isSlovak) "Aktívne filtre 🛠️" else "Aktivní filtry 🛠️"
                        } else "",
                        color = FreshGreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = if (isSlovak) "Nájdených: ${filteredRecipes.size} receptov" else "Nalezeno: ${filteredRecipes.size} receptů",
                        color = CaptionTextNatural,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (filteredRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSlovak) "Žiadne recepty nezodpovedajú vašim filtrom." else "Žádné recepty neodpovídají vašim filtrům.",
                            color = CaptionTextNatural,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredRecipes) { recipe ->
                            FeaturedRecipeCard(
                                recipe = recipe,
                                isSlovak = isSlovak,
                                pantryNames = pantryNames,
                                onAddMissingToShoppingList = { missingItemName ->
                                    val defaultStore = if (isSlovak) "Lidl (Bratislava)" else "Albert (Praha)"
                                    viewModel.addMissingToShoppingList(
                                        missingItemName,
                                        defaultStore,
                                        recipe.basePrice
                                    )
                                }
                            )
                        }
                    }
                }
            }
            "ChefAI" -> {
                ChefAITabContent(
                    isAiLoading = isAiLoading,
                    aiRecipeMarkdown = aiRecipeMarkdown,
                    pantryItems = pantryItems,
                    viewModel = viewModel,
                    isSlovak = isSlovak
                )
            }
        }
    }
}

data class FeaturedRecipe(
    val czTitle: String,
    val skTitle: String,
    val time: String,
    val calories: Int,
    val protein: String,
    val carbs: String,
    val fat: String,
    val ingredientsCZ: List<String>,
    val ingredientsSK: List<String>,
    val isGlutenFree: Boolean,
    val isVegan: Boolean,
    val isVegetarian: Boolean,
    val instructionsCZ: String,
    val instructionsSK: String,
    val basePrice: Double,
    val category: String,
    val descriptionCZ: String = "",
    val descriptionSK: String = "",
    val glassCZ: String = "",
    val glassSK: String = "",
    val difficultyCZ: String = "",
    val difficultySK: String = "",
    val alcoholStrengthCZ: String = "",
    val alcoholStrengthSK: String = "",
    val alcoholVol: String = "",
    val alcoholType: String = "",
    val drinkCategory: String = "",
    val garnishCZ: String = "",
    val garnishSK: String = ""
)

@Composable
fun FeaturedRecipeCard(
    recipe: FeaturedRecipe,
    isSlovak: Boolean,
    pantryNames: List<String>,
    onAddMissingToShoppingList: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val title = if (isSlovak) recipe.skTitle else recipe.czTitle
    val ingredients = if (isSlovak) recipe.ingredientsSK else recipe.ingredientsCZ
    val instructions = if (isSlovak) recipe.instructionsSK else recipe.instructionsCZ

    // Calculate matched ingredients dynamically
    val matchingItems = ingredients.filter { ing ->
        pantryNames.any { pantryName ->
            pantryName.contains(ing.lowercase()) || ing.lowercase().contains(pantryName)
        }
    }
    val matchedCount = matchingItems.size
    val totalCount = ingredients.size

    val proteinLabel = if (isSlovak) "Bielkoviny" else "Proteiny"
    val carbsLabel = "Carbs"
    val fatsLabel = if (isSlovak) "Tuky" else "Fats"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // First Row: Name (bold left) and Badge (light green right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = CreamText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(SoftGreenGlow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Obsahuje $matchedCount/$totalCount",
                        color = FreshGreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Second Row: gray detail icons: [⏱️ Time] and [⚡ Calorie]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Prep time",
                        tint = CaptionTextNatural,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recipe.time,
                        color = CaptionTextNatural,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (recipe.category == "alko" && recipe.alcoholVol.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalBar,
                            contentDescription = "Alcohol content",
                            tint = CaptionTextNatural,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = recipe.alcoholVol,
                            color = CaptionTextNatural,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Calories",
                        tint = CaptionTextNatural,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.calories} kcal",
                        color = CaptionTextNatural,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Description and Tags for Alcoholic Beverages
            val description = if (isSlovak) recipe.descriptionSK else recipe.descriptionCZ
            if (recipe.category == "alko" && description.isNotEmpty()) {
                Text(
                    text = description,
                    color = CaptionTextNatural,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            val strength = if (isSlovak) recipe.alcoholStrengthSK else recipe.alcoholStrengthCZ
            if (recipe.category == "alko") {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (recipe.drinkCategory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(NavBgNatural, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = recipe.drinkCategory.split(",").first().trim(),
                                color = MutedTextNatural,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (recipe.alcoholType.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(SoftGreenGlow, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = recipe.alcoholType,
                                color = FreshGreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (strength.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF6F5), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strength,
                                color = TomatoRedTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Third Row: column based nutritional values. Tuky / Fats value is highlighted in soft red.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FBF8), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$proteinLabel: ${recipe.protein}",
                    fontSize = 12.sp,
                    color = CaptionTextNatural,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$carbsLabel: ${recipe.carbs}",
                    fontSize = 12.sp,
                    color = CaptionTextNatural,
                    fontWeight = FontWeight.SemiBold
                )

                // Highlight Fats in soft red
                Text(
                    text = "$fatsLabel: ${recipe.fat}",
                    fontSize = 12.sp,
                    color = TomatoRedTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expanded instructions and missing items list
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Divider(color = BorderNatural, modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = if (isSlovak) "Návod na prípravu:" else "Návod na přípravu:",
                        color = FreshGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = instructions,
                        color = CreamText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    if (recipe.category == "alko") {
                        val glass = if (isSlovak) recipe.glassSK else recipe.glassCZ
                        val difficulty = if (isSlovak) recipe.difficultySK else recipe.difficultyCZ
                        val garnish = if (isSlovak) recipe.garnishSK else recipe.garnishCZ
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isSlovak) "Detaily koktailu ✨" else "Detaily koktejlu ✨",
                                    color = FreshGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (glass.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(if (isSlovak) "Pohár:" else "Sklenice:", color = MutedTextNatural, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(glass, color = CreamText, fontSize = 11.sp)
                                        }
                                    }
                                    if (difficulty.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(if (isSlovak) "Náročnosť:" else "Náročnost:", color = MutedTextNatural, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(difficulty, color = CreamText, fontSize = 11.sp)
                                        }
                                    }
                                    if (strength.isNotEmpty() || recipe.alcoholVol.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(if (isSlovak) "Obsah alkoholu:" else "Obsah alkoholu:", color = MutedTextNatural, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("$strength (${recipe.alcoholVol})", color = CreamText, fontSize = 11.sp)
                                        }
                                    }
                                    if (recipe.alcoholType.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(if (isSlovak) "Základ alkoholu:" else "Základ alkoholu:", color = MutedTextNatural, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(recipe.alcoholType, color = CreamText, fontSize = 11.sp)
                                        }
                                    }
                                    if (garnish.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(if (isSlovak) "Ozdoba:" else "Ozdoba:", color = MutedTextNatural, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(garnish, color = CreamText, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Missing items with shopping cart addition trigger
                    val missingItems = ingredients.filter { !matchingItems.contains(it) }

                    if (ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSlovak) "Suroviny na prípravu:" else "Suroviny v receptu:",
                                color = CreamText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            if (missingItems.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(TomatoRedTertiary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            missingItems.forEach { item ->
                                                onAddMissingToShoppingList(item)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("add_all_missing_to_shopping_list_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription = "Add all to shopping list",
                                        tint = TomatoRedTertiary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSlovak) "+ Kúpiť chýbajúce" else "+ Koupit chybějící",
                                        color = TomatoRedTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ingredients.forEach { item ->
                                val hasItem = matchingItems.contains(item)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (hasItem) Color(0xFFF1F8EE) else Color(0xFFFFF6F5)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(!hasItem) { onAddMissingToShoppingList(item) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (hasItem) Icons.Default.CheckCircle else Icons.Default.AddShoppingCart,
                                                contentDescription = if (hasItem) "Available" else "Add missing",
                                                tint = if (hasItem) FreshGreenPrimary else TomatoRedTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.replaceFirstChar { it.uppercase() },
                                                color = if (hasItem) FreshGreenPrimary else CreamText,
                                                fontWeight = if (hasItem) FontWeight.Medium else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (!hasItem) {
                                            Text(
                                                text = if (isSlovak) "+ Chýba" else "+ Koupit",
                                                color = TomatoRedTertiary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Text(
                                                text = if (isSlovak) "Máš" else "Máš",
                                                color = FreshGreenPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = if (isSlovak) "▲ Kliknutím zavriete detaily" else "▲ Kliknutím zavřete detaily",
                        color = CaptionTextNatural.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
