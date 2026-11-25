package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BrandCrestStyle
import com.example.ui.theme.EditorialHeadingStyle
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.Ivory
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.ScriptWelcomeStyle
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.92f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = tween(550))
        delay(950)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightNavy, MidnightNavyDeep)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Surface(
                shape = CircleShape,
                color = Ivory,
                border = BorderStroke(2.dp, SoftGold),
                modifier = Modifier.size(96.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_fg_art),
                    contentDescription = "Emblème Sèmè Express",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "SÈMÈ EXPRESS",
                style = BrandCrestStyle,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hospitalité & Livraison 2027",
                style = ScriptWelcomeStyle,
                color = SoftGold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = PureWhite.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "SITE DU PÈLERINAGE • SÈMÈ, BÉNIN",
                    style = SmallCapsBadgeStyle,
                    color = Ivory,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private data class OnboardingPage(
    val eyebrow: String,
    val title: String,
    val scriptLine: String,
    val description: String,
    val icon: ImageVector,
    val imageRes: Int
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                eyebrow = "ÉTAPE 1 • COMMANDER EN QUELQUES SECONDES",
                title = "Repas, boissons et articles essentiels",
                scriptLine = "Simplicité & Fraîcheur",
                description = "Parcourez les restaurants et vendeurs indépendants du site de Sèmè avec des prix clairs en FCFA et une disponibilité immédiate.",
                icon = Icons.Default.RestaurantMenu,
                imageRes = R.drawable.img_hero_seme
            ),
            OnboardingPage(
                eyebrow = "ÉTAPE 2 • PRÉCISER VOTRE EMPLACEMENT",
                title = "Livraison directe à votre tente",
                scriptLine = "Repères précis",
                description = "Utilisez votre GPS ou indiquez simplement votre numéro de tente, votre rangée et un repère proche dans les zones autorisées.",
                icon = Icons.Default.LocationOn,
                imageRes = R.drawable.img_food_grill
            ),
            OnboardingPage(
                eyebrow = "ÉTAPE 3 • SUIVI EN TEMPS RÉEL",
                title = "Suivez votre commande jusqu'à la remise",
                scriptLine = "Sérénité totale",
                description = "Suivez chaque étape de préparation et de livraison, et réglez à la livraison ou par Mobile Money selon votre préférence.",
                icon = Icons.Default.Explore,
                imageRes = R.drawable.img_drinks_fresh
            )
        )
    }

    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SÈMÈ EXPRESS",
                    style = SmallCapsBadgeStyle,
                    color = MidnightNavy
                )
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text("Passer", color = SlateText)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = page.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = MidnightNavy.copy(alpha = 0.88f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = page.eyebrow,
                        style = SmallCapsBadgeStyle,
                        color = MidnightNavy,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.scriptLine,
                    style = ScriptWelcomeStyle,
                    color = TurquoiseGreen,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = page.title,
                    style = EditorialHeadingStyle,
                    color = MidnightNavy,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SlateText,
                    textAlign = TextAlign.Center
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { idx, _ ->
                        val active = idx == currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (active) 28.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (active) MidnightNavy else SoftGold.copy(alpha = 0.4f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidnightNavy,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentPage < pages.lastIndex) "Suivant" else "Commencer",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
