package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: FridgeBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    var selectedLang by remember { mutableStateOf("CZ") }
    
    // Auth Mode: false = Login, true = Register
    var isRegisterMode by remember { mutableStateOf(false) }
    
    // Form States
    var nicknameState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var confirmPasswordState by remember { mutableStateOf("") }
    
    // UI Visual States
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Validation Errors
    var nicknameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Submission status loading or custom server error
    var isAuthenticating by remember { mutableStateOf(false) }
    var authServerError by remember { mutableStateOf<String?>(null) }

    // Translations
    val titleText = when (selectedLang) {
        "SK" -> if (isRegisterMode) "Nová Registrácia" else "Prihlásenie do Špajze"
        "EN" -> if (isRegisterMode) "Create Account" else "Sign in to Smart Pantry"
        else -> if (isRegisterMode) "Nová Registrace" else "Přihlášení do Spižírny"
    }
    
    val subtitleText = when (selectedLang) {
        "SK" -> "Váš inteligentný spoločník pre správu potravín, receptov a kalórií."
        "EN" -> "Your intelligent companion for tracking foods, recipes and calories."
        else -> "Váš inteligentní společník pro správu potravin, receptů a kalorií."
    }

    val tabLogin = when (selectedLang) {
        "SK" -> "Prihlásenie"
        "EN" -> "Log In"
        else -> "Přihlášení"
    }

    val tabRegister = when (selectedLang) {
        "SK" -> "Registrácia"
        "EN" -> "Sign Up"
        else -> "Registrace"
    }

    val labelNickname = when (selectedLang) {
        "SK" -> "Meno / Prezývka"
        "EN" -> "Nickname"
        else -> "Jméno / Přezdívka"
    }

    val descNickname = when (selectedLang) {
        "SK" -> "Ako vás máme v kuchyni oslovovať"
        "EN" -> "How should we address you"
        else -> "Jak vás máme v kuchyni oslovovat"
    }

    val labelEmail = when (selectedLang) {
        "SK" -> "Prihlasovací e-mail"
        "EN" -> "Email Address"
        else -> "Přihlašovací e-mail"
    }

    val placeholderEmail = when (selectedLang) {
        "SK" -> "napr. kuchyna@firma.sk"
        "EN" -> "e.g. chef@pantry.com"
        else -> "např. kuchyn@firma.cz"
    }

    val descEmail = when (selectedLang) {
        "SK" -> "Pre bezpečné uchovanie a synchronizáciu vašich zásob"
        "EN" -> "Used to securely preserve and sync your items"
        else -> "Pro bezpečné uchování a synchronizaci vašich zásob"
    }

    val labelPassword = when (selectedLang) {
        "SK" -> "Heslo"
        "EN" -> "Password"
        else -> "Heslo"
    }

    val placeholderPassword = when (selectedLang) {
        "SK" -> "Zadajte silné heslo"
        "EN" -> "Enter secure password"
        else -> "Zadejte silné heslo"
    }

    val descPassword = when (selectedLang) {
        "SK" -> "Heslo musí mať aspoň 8 znakov"
        "EN" -> "Must be at least 8 characters"
        else -> "Heslo musí mít alespoň 8 znaků"
    }

    val labelConfirmPassword = when (selectedLang) {
        "SK" -> "Potvrdiť heslo"
        "EN" -> "Confirm Password"
        else -> "Potvrdit heslo"
    }

    val placeholderConfirmPassword = when (selectedLang) {
        "SK" -> "Zopakujte heslo znova"
        "EN" -> "Repeat password again"
        else -> "Zopakujte heslo znovu"
    }

    val btnSubmit = when (selectedLang) {
        "SK" -> if (isRegisterMode) "Zaregistrovať sa" else "Prihlásiť sa"
        "EN" -> if (isRegisterMode) "Register" else "Login"
        else -> if (isRegisterMode) "Zaregistrovat se" else "Přihlásit se"
    }

    val nicknameRequiredErr = when (selectedLang) {
        "SK" -> "Meno je povinné"
        "EN" -> "Nickname is required"
        else -> "Jméno je povinné"
    }

    val emailRequiredErr = when (selectedLang) {
        "SK" -> "E-mail je povinný"
        "EN" -> "Email is required"
        else -> "E-mail je povinný"
    }

    val emailInvalidErr = when (selectedLang) {
        "SK" -> "Neplatný formát e-mailu"
        "EN" -> "Invalid email address format"
        else -> "Neplatný formát e-mailu"
    }

    val passwordRequiredErr = when (selectedLang) {
        "SK" -> "Heslo je povinné"
        "EN" -> "Password is required"
        else -> "Heslo je povinné"
    }

    val passwordShortErr = when (selectedLang) {
        "SK" -> "Heslo musí mať dĺžku aspoň 8 znakov"
        "EN" -> "Password must be at least 8 characters long"
        else -> "Heslo musí mít délku alespoň 8 znaků"
    }

    val passwordMismatchErr = when (selectedLang) {
        "SK" -> "Heslá sa nezhodujú"
        "EN" -> "Passwords do not match"
        else -> "Hesla se neshodují"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("onboarding_container"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = BorderStroke(1.dp, BorderNatural),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header lock/kitchen icon visual decoration
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SoftGreenGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.LockPerson,
                        contentDescription = "Lock Icon",
                        tint = FreshGreenPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = titleText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreenPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("onboarding_title")
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = subtitleText,
                    fontSize = 13.sp,
                    color = CaptionTextNatural,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Czech/Slovak Language chip elements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val langs = listOf(
                        "CZ" to "Čeština 🇨🇿",
                        "SK" to "Slovenčina 🇸🇰",
                        "EN" to "English 🇬🇧"
                    )
                    
                    langs.forEach { (code, label) ->
                        val isSelected = selectedLang == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SoftGreenGlow else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) FreshGreenPrimary else BorderNatural
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedLang = code }
                                .padding(vertical = 10.dp)
                                .testTag("lang_chip_$code"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FreshGreenPrimary else CreamText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Mode selector slider tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBg)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRegisterMode) FreshGreenPrimary else Color.Transparent)
                            .clickable { 
                                isRegisterMode = false
                                authServerError = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLogin,
                            fontWeight = FontWeight.Bold,
                            color = if (!isRegisterMode) Color.White else CreamText.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegisterMode) FreshGreenPrimary else Color.Transparent)
                            .clickable {
                                isRegisterMode = true
                                authServerError = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabRegister,
                            fontWeight = FontWeight.Bold,
                            color = if (isRegisterMode) Color.White else CreamText.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                if (authServerError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TomatoRedTertiary.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, TomatoRedTertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error icon", tint = TomatoRedTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authServerError!!,
                                color = TomatoRedTertiary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- FORM FIELDS ---

                // 1. Nickname Field (Registration Mode only)
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = nicknameState,
                        onValueChange = {
                            nicknameState = it
                            nicknameError = null
                        },
                        label = { Text(labelNickname, color = FreshGreenPrimary) },
                        placeholder = { Text(descNickname, color = CaptionTextNatural) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = FreshGreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_nickname_input"),
                        singleLine = true,
                        isError = nicknameError != null,
                        supportingText = {
                            if (nicknameError != null) {
                                Text(nicknameError!!, color = TomatoRedTertiary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CreamText,
                            unfocusedTextColor = CreamText,
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = BorderNatural,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. Email Field
                OutlinedTextField(
                    value = emailState,
                    onValueChange = {
                        emailState = it
                        emailError = null
                    },
                    label = { Text(labelEmail, color = FreshGreenPrimary) },
                    placeholder = { Text(placeholderEmail, color = CaptionTextNatural) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = FreshGreenPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_email_input"),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(emailError!!, color = TomatoRedTertiary)
                        } else {
                            Text(descEmail, fontSize = 11.sp, color = CaptionTextNatural)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Password Field
                OutlinedTextField(
                    value = passwordState,
                    onValueChange = {
                        passwordState = it
                        passwordError = null
                    },
                    label = { Text(labelPassword, color = FreshGreenPrimary) },
                    placeholder = { Text(placeholderPassword, color = CaptionTextNatural) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = FreshGreenPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            Icon(imageVector = icon, contentDescription = "Toggle password", tint = FreshGreenPrimary)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_password_input"),
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            Text(passwordError!!, color = TomatoRedTertiary)
                        } else {
                            Text(descPassword, fontSize = 11.sp, color = CaptionTextNatural)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                // 4. Confirm Password Field (Registration Mode only)
                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPasswordState,
                        onValueChange = {
                            confirmPasswordState = it
                            confirmPasswordError = null
                        },
                        label = { Text(labelConfirmPassword, color = FreshGreenPrimary) },
                        placeholder = { Text(placeholderConfirmPassword, color = CaptionTextNatural) },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = "Lock Reset", tint = FreshGreenPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                Icon(imageVector = icon, contentDescription = "Toggle password match", tint = FreshGreenPrimary)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_confirm_password_input"),
                        singleLine = true,
                        isError = confirmPasswordError != null,
                        supportingText = {
                            if (confirmPasswordError != null) {
                                Text(confirmPasswordError!!, color = TomatoRedTertiary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CreamText,
                            unfocusedTextColor = CreamText,
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = BorderNatural,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- CTAs AND SUBMIT ---
                if (isAuthenticating) {
                    CircularProgressIndicator(color = FreshGreenPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            authServerError = null
                            
                            val trimmedEmail = emailState.trim()
                            val trimmedPassword = passwordState.trim()
                            var formHasErrors = false

                            // Validation 1: Nickname (Register only)
                            if (isRegisterMode && nicknameState.trim().isEmpty()) {
                                nicknameError = nicknameRequiredErr
                                formHasErrors = true
                            } else {
                                nicknameError = null
                            }

                            // Validation 2: Email format and present
                            if (trimmedEmail.isEmpty()) {
                                emailError = emailRequiredErr
                                formHasErrors = true
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                emailError = emailInvalidErr
                                formHasErrors = true
                            } else {
                                emailError = null
                            }

                            // Validation 3: Password min length 8
                            if (trimmedPassword.isEmpty()) {
                                passwordError = passwordRequiredErr
                                formHasErrors = true
                            } else if (trimmedPassword.length < 8) {
                                passwordError = passwordShortErr
                                formHasErrors = true
                            } else {
                                passwordError = null
                            }

                            // Validation 4: Confirm password matches (Register only)
                            if (isRegisterMode) {
                                if (confirmPasswordState.trim().isEmpty()) {
                                    confirmPasswordError = passwordRequiredErr
                                    formHasErrors = true
                                } else if (trimmedPassword != confirmPasswordState.trim()) {
                                    confirmPasswordError = passwordMismatchErr
                                    formHasErrors = true
                                } else {
                                    confirmPasswordError = null
                                }
                            }

                            // If no clientside errors, launch secure VM auth processes!
                            if (!formHasErrors) {
                                isAuthenticating = true
                                coroutineScope.launch {
                                    val err = if (isRegisterMode) {
                                        viewModel.registerUser(
                                            nickname = nicknameState.trim(),
                                            email = trimmedEmail,
                                            pword = trimmedPassword,
                                            lang = selectedLang
                                        )
                                    } else {
                                        viewModel.loginUser(
                                            email = trimmedEmail,
                                            pword = trimmedPassword,
                                            lang = selectedLang
                                        )
                                    }
                                    isAuthenticating = false
                                    if (err != null) {
                                        authServerError = err
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_start_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreshGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = btnSubmit,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
