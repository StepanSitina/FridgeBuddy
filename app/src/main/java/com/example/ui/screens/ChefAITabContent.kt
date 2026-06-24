package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PantryItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel

@Composable
fun ChefAITabContent(
    isAiLoading: Boolean,
    aiRecipeMarkdown: String,
    pantryItems: List<PantryItem>,
    viewModel: FridgeBuddyViewModel,
    isSlovak: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isAiLoading && aiRecipeMarkdown.isBlank()) {
            // Empty state - Ready to generate
            Spacer(modifier = Modifier.height(32.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = FreshGreenPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSlovak) "Generátor receptov z chladničky" else "Generátor receptů z lednice",
                color = CreamText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSlovak) "AI vymyslí ideálny recept zo surovín, ktoré máte momentálne k dispozícii." else "AI vymyslí ideální recept ze surovin, které máte momentálně k dispozici.",
                color = CaptionTextNatural,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    viewModel.runAiRecipeGeneration("GENERATE_RECIPE") 
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSlovak) "Vygenerovať recept" else "Vygenerovat recept",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (isAiLoading) {
            // Loading state - Skeleton Loader
            RecipeSkeletonLoader()
        } else {
            // Result state - Markdown
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = aiRecipeMarkdown,
                        color = CreamText,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Action Buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.runAiRecipeGeneration("PRESET_ACTION_OVEN_INFO") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FreshGreenPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSlovak) "Info k rúre" else "Info k troubě",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                OutlinedButton(
                    onClick = { viewModel.runAiRecipeGeneration("PRESET_ACTION_SUBSTITUTIONS") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FreshGreenPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSlovak) "Náhrady surovín" else "Náhrady surovin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    viewModel.runAiRecipeGeneration("GENERATE_RECIPE") 
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CreamText),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = FreshGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSlovak) "Vygenerovať iný recept" else "Vygenerovat jiný recept",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RecipeSkeletonLoader() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(28.dp)
                .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(8.dp))
        )
        // Preparation time
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(16.dp)
                .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Ingredients "Title"
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp)
                .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(4.dp))
        )
        
        // Ingredients list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(4.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Instructions "Title"
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(4.dp))
        )
        // Instructions list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(60.dp)
                        .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(8.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Table Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(120.dp)
                .background(Color.Gray.copy(alpha = alpha), RoundedCornerShape(8.dp))
        )
    }
}
