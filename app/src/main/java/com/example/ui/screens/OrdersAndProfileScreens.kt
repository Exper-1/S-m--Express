package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogData
import com.example.data.model.DeliveryLocationDraft
import com.example.data.model.MobileMoneyOperator
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PilgrimOrder
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.ErrorStateBanner
import com.example.ui.components.SemeTopBar
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ItalianaNumericStyle
import com.example.ui.theme.Ivory
import com.example.ui.theme.LightGray
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.TurquoiseGreen
import com.example.ui.viewmodel.UiState

@Composable
fun OrdersScreen(
    ordersState: UiState<List<PilgrimOrder>>,
    onOrderClick: (PilgrimOrder) -> Unit,
    onReorder: (PilgrimOrder) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Mes commandes",
            subtitle = "Suivi en direct & historique"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = MidnightNavy
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("En cours") },
                modifier = Modifier.testTag("orders_tab_active")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Historique") },
                modifier = Modifier.testTag("orders_tab_history")
            )
        }

        when (ordersState) {
            UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MidnightNavy)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    ErrorStateBanner(message = ordersState.message)
                }
            }
            is UiState.Success -> {
                val filtered = remember(ordersState.data, selectedTab) {
                    if (selectedTab == 0) ordersState.data.filter { it.isActiveOrder }
                    else ordersState.data.filter { !it.isActiveOrder }
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStatePanel(
                            title = if (selectedTab == 0) "Aucune commande en cours" else "Historique vide",
                            subtitle = if (selectedTab == 0) {
                                "Passez une commande pour suivre sa préparation et sa livraison étape par étape."
                            } else {
                                "Vos commandes livrées apparaîtront ici pour être recommandées en un clic."
                            },
                            actionLabel = "Commander maintenant",
                            onAction = onExploreClick
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { order ->
                            OrderHistoryCard(
                                order = order,
                                onOpenTracking = { onOrderClick(order) },
                                onReorder = { onReorder(order) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: PilgrimOrder,
    onOpenTracking: () -> Unit,
    onReorder: () -> Unit
) {
    val stage = order.stageEnum
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTracking)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, if (order.isActiveOrder) TurquoiseGreen else BorderHairline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    style = SmallCapsBadgeStyle,
                    color = SoftGold
                )
                Surface(
                    color = if (order.isActiveOrder) TurquoiseContainer else GoldContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stage.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (order.isActiveOrder) TurquoiseGreen else MidnightNavy,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = order.vendorName,
                style = MaterialTheme.typography.titleMedium,
                color = MidnightNavy
            )
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CatalogData.formatFcfa(order.totalFcfa),
                    style = ItalianaNumericStyle,
                    color = MidnightNavy,
                    fontWeight = FontWeight.Bold
                )
                if (order.isActiveOrder) {
                    Button(
                        onClick = onOpenTracking,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MidnightNavy,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Suivre (${stage.stepIndex}/8)")
                    }
                } else {
                    OutlinedButton(
                        onClick = onReorder,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recommander", color = MidnightNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTrackingScreen(
    order: PilgrimOrder,
    onBack: () -> Unit,
    onAdvanceStage: (PilgrimOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentStage = order.stageEnum
    val allStages = OrderStage.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Suivi ${order.orderNumber}",
            subtitle = "${currentStage.stepIndex}/8 • ${currentStage.label}",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Current status highlight card
            Surface(
                color = MidnightNavy,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ÉTAPE ACTUELLE (${currentStage.stepIndex}/8)",
                        style = SmallCapsBadgeStyle,
                        color = SoftGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentStage.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = PureWhite
                    )
                    Text(
                        text = currentStage.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { currentStage.stepIndex / 8f },
                        color = TurquoiseGreen,
                        trackColor = PureWhite.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }

            // Courier contact card if assigned
            if (order.courierName.isNotBlank()) {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, TurquoiseGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TurquoiseContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TurquoiseGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Livreur : ${order.courierName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MidnightNavy
                                )
                                Text(
                                    text = order.courierPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateText
                                )
                            }
                        }
                        if (order.courierPhone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.courierPhone}"))
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Appeler le livreur", tint = TurquoiseGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Appeler", color = TurquoiseGreen)
                            }
                        }
                    }
                }
            }

            // 8-stage vertical timeline
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Progression de la livraison",
                        style = MaterialTheme.typography.titleLarge,
                        color = MidnightNavy
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    allStages.forEachIndexed { index, stage ->
                        val isCompleted = stage.stepIndex < currentStage.stepIndex ||
                            (stage == OrderStage.DELIVERED && currentStage == OrderStage.DELIVERED)
                        val isCurrent = stage == currentStage && currentStage != OrderStage.DELIVERED
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isCompleted -> TurquoiseGreen
                                        isCurrent -> MidnightNavy
                                        else -> LightGray
                                    },
                                    border = if (isCurrent) BorderStroke(2.dp, SoftGold) else null,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = stage.stepIndex.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isCurrent) PureWhite else SlateText
                                            )
                                        }
                                    }
                                }
                                if (index < allStages.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(30.dp)
                                            .background(if (isCompleted) TurquoiseGreen else BorderHairline)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = stage.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isCompleted || isCurrent) MidnightNavy else SlateText,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = stage.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateText
                                )
                            }
                        }
                    }
                }
            }

            // Delivery & Order recap
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Détails de réception",
                        style = MaterialTheme.typography.titleMedium,
                        color = MidnightNavy
                    )
                    Text("${order.vendorName} • ${order.itemsSummary}", style = MaterialTheme.typography.bodyMedium, color = SlateText)
                    Text("Adresse : ${order.tentNumber}, ${order.sector}", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                    if (order.landmark.isNotBlank()) {
                        Text("Repère : ${order.landmark}", style = MaterialTheme.typography.bodySmall, color = SlateText)
                    }
                    Text(
                        text = "Total : ${CatalogData.formatFcfa(order.totalFcfa)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TurquoiseGreen
                    )
                }
            }
        }

        // Interactive demo step advancement while not DELIVERED
        if (currentStage != OrderStage.DELIVERED) {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Button(
                    onClick = { onAdvanceStage(order) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TurquoiseGreen,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 54.dp)
                        .testTag("tracking_advance_step_button")
                ) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    val nextLabel = currentStage.nextStageOrNull()?.label ?: ""
                    Text("Passer à l'étape suivante : $nextLabel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    displayName: String,
    email: String,
    draft: DeliveryLocationDraft,
    preferredPayment: PaymentMethod,
    mobileMoneyOperator: MobileMoneyOperator,
    notificationsEnabled: Boolean,
    ordersCount: Int,
    onEditDeliveryAddress: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSavePreferences: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Profil & Paramètres",
            subtitle = "Compte pèlerin Sèmè Express"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Identity Card
            Surface(
                color = MidnightNavy,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = MidnightNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName.ifBlank { "Pèlerin Sèmè" },
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite
                        )
                        Text(
                            text = email.ifBlank { "Compte Google connecté" },
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$ordersCount commande(s) enregistrée(s)",
                            style = SmallCapsBadgeStyle,
                            color = SoftGold
                        )
                    }
                }
            }

            // Saved tent & delivery coordinates
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = TurquoiseGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emplacement de livraison",
                                style = MaterialTheme.typography.titleMedium,
                                color = MidnightNavy
                            )
                        }
                        OutlinedButton(
                            onClick = onEditDeliveryAddress,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("profile_edit_location_button")
                        ) {
                            Text("Modifier", color = MidnightNavy)
                        }
                    }
                    HorizontalDivider(color = BorderHairline)
                    Text(
                        text = "Tente : ${draft.tentNumber.ifBlank { "Non renseignée" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MidnightNavy
                    )
                    Text(
                        text = "Secteur : ${draft.sector.ifBlank { "Non renseigné" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateText
                    )
                    Text(
                        text = "Téléphone : ${draft.phone.ifBlank { "Non renseigné" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateText
                    )
                }
            }

            // Preferences & offline status
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Paramètres du compte",
                        style = MaterialTheme.typography.titleMedium,
                        color = MidnightNavy
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MidnightNavy)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Notifications visuelles", style = MaterialTheme.typography.bodyLarge, color = MidnightNavy)
                                Text("Alertes lors des changements d'étape", style = MaterialTheme.typography.bodySmall, color = SlateText)
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(checkedTrackColor = TurquoiseGreen),
                            modifier = Modifier.testTag("profile_notifications_switch")
                        )
                    }
                    HorizontalDivider(color = BorderHairline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = MidnightNavy)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Paiement favori", style = MaterialTheme.typography.bodyLarge, color = MidnightNavy)
                            Text(
                                text = if (preferredPayment == PaymentMethod.MOBILE_MONEY) {
                                    "Mobile Money (${mobileMoneyOperator.label})"
                                } else {
                                    preferredPayment.label
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateText
                            )
                        }
                    }
                    HorizontalDivider(color = BorderHairline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = TurquoiseGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Résilience réseau instable", style = MaterialTheme.typography.bodyLarge, color = MidnightNavy)
                            Text(
                                text = "Cache hors ligne activé : le catalogue et vos commandes restent consultables.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateText
                            )
                        }
                    }
                    Button(
                        onClick = onSavePreferences,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TurquoiseGreen,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("profile_save_button")
                    ) {
                        Text("Sauvegarder mon profil", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("profile_sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se déconnecter", color = ErrorCrimson, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
