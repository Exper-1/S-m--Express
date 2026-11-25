package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CartItem
import com.example.data.model.CatalogData
import com.example.data.model.DeliveryLocationDraft
import com.example.data.model.MobileMoneyOperator
import com.example.data.model.PaymentMethod
import com.example.data.model.PilgrimOrder
import com.example.data.model.SAMPLE_DELIVERY_FEE_FCFA
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.SemeTopBar
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandCrestStyle
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ItalianaNumericStyle
import com.example.ui.theme.Ivory
import com.example.ui.theme.LightGray
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.ScriptWelcomeStyle
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.TurquoiseGreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    subtotalFcfa: Long,
    totalFcfa: Long,
    onBack: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onExploreClick: () -> Unit,
    onProceedToLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Votre panier",
            subtitle = "${cartItems.sumOf { it.quantity }} article(s)",
            onBack = onBack,
            trailingAction = if (cartItems.isNotEmpty()) {
                {
                    TextButton(
                        onClick = onClearCart,
                        modifier = Modifier.testTag("cart_clear_button")
                    ) {
                        Text("Vider", color = ErrorCrimson)
                    }
                }
            } else null
        )

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStatePanel(
                    title = "Votre panier est vide",
                    subtitle = "Choisissez un repas chaud, une boisson fraîche ou un kit pèlerin pour commencer.",
                    actionLabel = "Explorer les vendeurs",
                    onAction = onExploreClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderHairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = item.product.imageRes),
                                contentDescription = item.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.vendorName,
                                    style = SmallCapsBadgeStyle,
                                    color = SoftGold
                                )
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MidnightNavy
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = "Note : ${item.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateText
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = CatalogData.formatFcfa(item.lineTotalFcfa),
                                    style = ItalianaNumericStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = TurquoiseGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(
                                    onClick = { onRemoveItem(item.product.id) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("cart_remove_${item.product.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer ${item.product.name}",
                                        tint = ErrorCrimson
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = LightGray,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onQuantityChange(item.product.id, -1) },
                                            modifier = Modifier.testTag("cart_minus_${item.product.id}")
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Moins", tint = MidnightNavy, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text(
                                        text = item.quantity.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MidnightNavy,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MidnightNavy,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onQuantityChange(item.product.id, 1) },
                                            modifier = Modifier.testTag("cart_plus_${item.product.id}")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = PureWhite, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    PriceBreakdownCard(
                        subtotalFcfa = subtotalFcfa,
                        deliveryFeeFcfa = SAMPLE_DELIVERY_FEE_FCFA,
                        totalFcfa = totalFcfa
                    )
                }
            }

            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Button(
                    onClick = onProceedToLocation,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidnightNavy,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 56.dp)
                        .testTag("cart_continue_location_button")
                ) {
                    Text(
                        text = "Continuer vers la livraison",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryLocationScreen(
    draft: DeliveryLocationDraft,
    onDraftChange: ((DeliveryLocationDraft) -> DeliveryLocationDraft) -> Unit,
    onBack: () -> Unit,
    onContinueToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var gpsStatus by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        isLocating = true
        gpsStatus = "Recherche de votre position GPS..."
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                isLocating = false
                if (location != null) {
                    onDraftChange {
                        it.copy(latitude = location.latitude, longitude = location.longitude)
                    }
                    gpsStatus = "Position GPS enregistrée (${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)})"
                } else {
                    client.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            onDraftChange {
                                it.copy(latitude = last.latitude, longitude = last.longitude)
                            }
                            gpsStatus = "Dernière position connue (${String.format("%.4f", last.latitude)}, ${String.format("%.4f", last.longitude)})"
                        } else {
                            gpsStatus = "Signal GPS faible : précisez bien votre tente et un point de repère ci-dessous."
                        }
                    }.addOnFailureListener {
                        gpsStatus = "GPS indisponible actuellement : vos repères manuels suffisent."
                    }
                }
            }
            .addOnFailureListener {
                isLocating = false
                gpsStatus = "Impossible de lire le GPS : vos repères de tente guideront le livreur."
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchGpsLocation()
        } else {
            gpsStatus = "Permission de localisation refusée. Indiquez votre tente et un repère."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Adresse sur le site",
            subtitle = "Localisation pèlerin • Zones autorisées",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Authorized zone card + visual Sèmè site schematic map
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TurquoiseGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Livraison directe dans les zones autorisées de Sèmè",
                            style = MaterialTheme.typography.labelLarge,
                            color = MidnightNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SiteMapCard(
                        selectedSector = draft.sector,
                        latitude = draft.latitude,
                        longitude = draft.longitude
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasFine || hasCoarse) {
                                fetchGpsLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !isLocating,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TurquoiseGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("gps_locate_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = TurquoiseGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = TurquoiseGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Détecter ma position GPS",
                            color = TurquoiseGreen,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    if (gpsStatus != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gpsStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateText
                        )
                    }
                }
            }

            // Sector quick selection
            Text(
                text = "Rangée ou secteur autorisé *",
                style = MaterialTheme.typography.titleMedium,
                color = MidnightNavy
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CatalogData.authorizedSectors) { sector ->
                    val selected = draft.sector == sector.name
                    FilterChip(
                        selected = selected,
                        onClick = { onDraftChange { it.copy(sector = sector.name) } },
                        label = { Text("${sector.zoneCode} • ${sector.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MidnightNavy,
                            selectedLabelColor = PureWhite,
                            containerColor = PureWhite,
                            labelColor = MidnightNavy
                        ),
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            OutlinedTextField(
                value = draft.tentNumber,
                onValueChange = { v -> onDraftChange { it.copy(tentNumber = v.take(40)) } },
                label = { Text("Numéro ou nom de la tente *") },
                placeholder = { Text("Ex: Tente B-14 ou Tente Paroisse Akpakpa") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_tent_input")
            )

            OutlinedTextField(
                value = draft.sector,
                onValueChange = { v -> onDraftChange { it.copy(sector = v.take(60)) } },
                label = { Text("Rangée ou secteur *") },
                placeholder = { Text("Ex: Secteur Nord - Rangée 4") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_sector_input")
            )

            OutlinedTextField(
                value = draft.landmark,
                onValueChange = { v -> onDraftChange { it.copy(landmark = v.take(120)) } },
                label = { Text("Point de repère proche") },
                placeholder = { Text("Ex: Près du grand manguier, entrée Est, point d'eau") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_landmark_input")
            )

            OutlinedTextField(
                value = draft.phone,
                onValueChange = { v -> onDraftChange { it.copy(phone = v.take(30)) } },
                label = { Text("Numéro de téléphone joignable *") },
                placeholder = { Text("Ex: +229 01 97 00 00 00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_phone_input")
            )

            OutlinedTextField(
                value = draft.deliveryNotes,
                onValueChange = { v -> onDraftChange { it.copy(deliveryNotes = v.take(250)) } },
                label = { Text("Instructions complémentaires") },
                placeholder = { Text("Ex: M'appeler en arrivant au portail") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_notes_input")
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorCrimson
                )
            }
        }

        Surface(
            color = PureWhite,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, BorderHairline)
        ) {
            Button(
                onClick = {
                    when {
                        draft.tentNumber.isBlank() -> validationError = "Veuillez indiquer votre numéro ou nom de tente."
                        draft.sector.isBlank() -> validationError = "Veuillez préciser votre rangée ou secteur."
                        draft.phone.trim().length < 4 -> validationError = "Veuillez saisir un numéro de téléphone joignable."
                        else -> {
                            validationError = null
                            onContinueToCheckout()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MidnightNavy,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 56.dp)
                    .testTag("delivery_continue_payment_button")
            ) {
                Text("Valider et choisir le paiement", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SiteMapCard(
    selectedSector: String,
    latitude: Double?,
    longitude: Double?
) {
    Surface(
        color = TurquoiseContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, TurquoiseGreen.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = MidnightNavy.copy(alpha = 0.12f)
                val roadColor = SoftGold.copy(alpha = 0.65f)
                // Site grid lines
                for (i in 1..4) {
                    val x = size.width * (i / 5f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                }
                for (j in 1..2) {
                    val y = size.height * (j / 3f)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                }
                // Main pilgrimage avenue
                drawLine(
                    color = roadColor,
                    start = Offset(0f, size.height * 0.55f),
                    end = Offset(size.width, size.height * 0.45f),
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MidnightNavy,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Zone de livraison",
                            tint = SoftGold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = selectedSector.ifBlank { "Zone autorisée Sèmè" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MidnightNavy
                    )
                    Text(
                        text = if (latitude != null && longitude != null) {
                            "GPS actif : ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
                        } else {
                            "Zone couverte • Livraison directe à la tente"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateText
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    delivery: DeliveryLocationDraft,
    selectedPayment: PaymentMethod,
    selectedOperator: MobileMoneyOperator,
    mobileMoneyPhone: String,
    subtotalFcfa: Long,
    totalFcfa: Long,
    isSubmitting: Boolean,
    onSelectPayment: (PaymentMethod) -> Unit,
    onSelectOperator: (MobileMoneyOperator) -> Unit,
    onMobilePhoneChange: (String) -> Unit,
    onEditDelivery: () -> Unit,
    onBack: () -> Unit,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Récapitulatif & Paiement",
            subtitle = "Vérifiez avant de confirmer",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Delivery summary card
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Livraison à votre emplacement",
                            style = MaterialTheme.typography.titleMedium,
                            color = MidnightNavy
                        )
                        TextButton(onClick = onEditDelivery) {
                            Text("Modifier", color = TurquoiseGreen)
                        }
                    }
                    Text(
                        text = "${delivery.tentNumber} • ${delivery.sector}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MidnightNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (delivery.landmark.isNotBlank()) {
                        Text(
                            text = "Repère : ${delivery.landmark}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateText
                        )
                    }
                    Text(
                        text = "Tél. : ${delivery.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateText
                    )
                }
            }

            // Items recap
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Articles commandés (${cartItems.sumOf { it.quantity }})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MidnightNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}× ${item.product.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MidnightNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CatalogData.formatFcfa(item.lineTotalFcfa),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MidnightNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            PriceBreakdownCard(
                subtotalFcfa = subtotalFcfa,
                deliveryFeeFcfa = SAMPLE_DELIVERY_FEE_FCFA,
                totalFcfa = totalFcfa
            )

            // Payment method selector
            Text(
                text = "Choix du mode de paiement",
                style = MaterialTheme.typography.titleLarge,
                color = MidnightNavy
            )

            PaymentMethod.entries.forEach { method ->
                val selected = method == selectedPayment
                Surface(
                    color = if (selected) TurquoiseContainer.copy(alpha = 0.45f) else PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) TurquoiseGreen else BorderHairline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPayment(method) }
                        .testTag("payment_option_${method.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onSelectPayment(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = TurquoiseGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (method == PaymentMethod.CASH_ON_DELIVERY) {
                                Icons.Default.Payments
                            } else {
                                Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = MidnightNavy
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MidnightNavy
                            )
                            Text(
                                text = method.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateText
                            )
                        }
                    }
                }
            }

            if (selectedPayment == PaymentMethod.MOBILE_MONEY) {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Opérateur Mobile Money",
                            style = MaterialTheme.typography.labelLarge,
                            color = MidnightNavy
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MobileMoneyOperator.entries) { op ->
                                val opSelected = op == selectedOperator
                                FilterChip(
                                    selected = opSelected,
                                    onClick = { onSelectOperator(op) },
                                    label = { Text(op.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MidnightNavy,
                                        selectedLabelColor = PureWhite
                                    )
                                )
                            }
                        }
                        OutlinedTextField(
                            value = mobileMoneyPhone,
                            onValueChange = { onMobilePhoneChange(it.take(30)) },
                            label = { Text("Numéro ${selectedOperator.label}") },
                            placeholder = { Text("Ex: +229 01 97 00 00 00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mobile_money_phone_input")
                        )
                    }
                }
            }
        }

        Surface(
            color = PureWhite,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, BorderHairline)
        ) {
            Button(
                onClick = onConfirmOrder,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TurquoiseGreen,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 56.dp)
                    .testTag("checkout_confirm_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Envoi de la commande...", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        text = "Confirmer la commande • ${CatalogData.formatFcfa(totalFcfa)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PriceBreakdownCard(
    subtotalFcfa: Long,
    deliveryFeeFcfa: Long,
    totalFcfa: Long
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Détail des montants (tarifs fictifs d'exemple)",
                style = SmallCapsBadgeStyle,
                color = SoftGold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sous-total des produits", style = MaterialTheme.typography.bodyMedium, color = SlateText)
                Text(CatalogData.formatFcfa(subtotalFcfa), style = MaterialTheme.typography.bodyLarge, color = MidnightNavy)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Frais fixes de livraison (exemple)", style = MaterialTheme.typography.bodyMedium, color = SlateText)
                Text(CatalogData.formatFcfa(deliveryFeeFcfa), style = MaterialTheme.typography.bodyLarge, color = MidnightNavy)
            }
            HorizontalDivider(color = BorderHairline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total à payer", style = MaterialTheme.typography.titleMedium, color = MidnightNavy)
                Text(
                    text = CatalogData.formatFcfa(totalFcfa),
                    style = ItalianaNumericStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    color = TurquoiseGreen
                )
            }
        }
    }
}

@Composable
fun OrderConfirmationScreen(
    order: PilgrimOrder,
    onTrackOrder: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Surface(
                shape = CircleShape,
                color = TurquoiseContainer,
                border = BorderStroke(2.dp, TurquoiseGreen),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Commande confirmée",
                        tint = TurquoiseGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Merci pour votre commande",
                style = ScriptWelcomeStyle,
                color = TurquoiseGreen,
                textAlign = TextAlign.Center
            )
            Text(
                text = "COMMANDE CONFIRMÉE",
                style = BrandCrestStyle.copy(fontSize = 22.sp),
                color = MidnightNavy,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, SoftGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NUMÉRO DE COMMANDE",
                        style = SmallCapsBadgeStyle,
                        color = SlateText
                    )
                    Text(
                        text = order.orderNumber,
                        style = ItalianaNumericStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = MidnightNavy
                    )
                    HorizontalDivider(color = BorderHairline)
                    Text(
                        text = order.vendorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MidnightNavy
                    )
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateText,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = GoldContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Livraison : ${order.tentNumber} • ${order.sector}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MidnightNavy,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = "Total : ${CatalogData.formatFcfa(order.totalFcfa)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TurquoiseGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onTrackOrder,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MidnightNavy,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("confirmation_track_button")
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suivre ma commande", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("confirmation_home_button")
            ) {
                Text("Retour à l'accueil", color = MidnightNavy, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
