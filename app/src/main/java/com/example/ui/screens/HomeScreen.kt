package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CatalogData
import com.example.data.model.DeliveryLocationDraft
import com.example.data.model.PilgrimOrder
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.Vendor
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.ProductCard
import com.example.ui.components.VendorAvailabilityChip
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandCrestStyle
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.Ivory
import com.example.ui.theme.LightGray
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.ScriptWelcomeStyle
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.TurquoiseGreen
import com.example.ui.viewmodel.HomeQuickFilter

@Composable
fun HomeScreen(
    deliveryDraft: DeliveryLocationDraft,
    activeOrder: PilgrimOrder?,
    searchQuery: String,
    selectedCategory: ProductCategory,
    quickFilter: HomeQuickFilter,
    vendors: List<Vendor>,
    products: List<Product>,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onSelectQuickFilter: (HomeQuickFilter) -> Unit,
    onEditLocation: () -> Unit,
    onTrackOrder: (PilgrimOrder) -> Unit,
    onOpenExplore: () -> Unit,
    onVendorClick: (Vendor) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header + Location pill
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MidnightNavy, MidnightNavyDeep)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SÈMÈ EXPRESS",
                            style = BrandCrestStyle.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                            color = PureWhite
                        )
                        Text(
                            text = "Bienvenue, ${deliveryDraft.fullName.ifBlank { "Pèlerin" }}",
                            style = ScriptWelcomeStyle.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                            color = SoftGold
                        )
                    }
                    Surface(
                        color = PureWhite.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable(onClick = onEditLocation)
                            .testTag("home_location_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditLocationAlt,
                                contentDescription = "Modifier l'adresse",
                                tint = SoftGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = deliveryDraft.tentNumber.ifBlank { "Indiquer votre tente" },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = deliveryDraft.sector.ifBlank { "Site Sèmè 2027" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prominent search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "Rechercher un repas, une boisson, un vendeur...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = MidnightNavy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Effacer la recherche",
                                    tint = SlateText
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                        focusedBorderColor = SoftGold,
                        unfocusedBorderColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        // Section « Commande en cours » when active order exists
        if (activeOrder != null) {
            item {
                ActiveOrderHomeBanner(
                    order = activeOrder,
                    onTrackClick = { onTrackOrder(activeOrder) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            // Hero editorial banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(138.dp)
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_seme),
                            contentDescription = "Livraison Sèmè Express",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MidnightNavy.copy(alpha = 0.9f),
                                            MidnightNavy.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                                .fillMaxWidth(0.75f)
                        ) {
                            Text(
                                text = "LIVRAISON SUR LE SITE • 2027",
                                style = SmallCapsBadgeStyle,
                                color = SoftGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Commandez en 30 secondes jusqu'à votre tente",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Frais fixes exemple : 300 FCFA",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldContainer
                            )
                        }
                    }
                }
            }
        }

        // Categories
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Catégories",
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightNavy,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProductCategory.entries) { category ->
                        val selected = category == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    category.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MidnightNavy,
                                selectedLabelColor = PureWhite,
                                containerColor = PureWhite,
                                labelColor = MidnightNavy
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = BorderHairline,
                                selectedBorderColor = MidnightNavy
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("category_chip_${category.name}")
                        )
                    }
                }
            }
        }

        // Quick filters
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HomeQuickFilter.entries) { filter ->
                    val selected = filter == quickFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectQuickFilter(filter) },
                        label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TurquoiseContainer,
                            selectedLabelColor = TurquoiseGreen,
                            containerColor = LightGray,
                            labelColor = SlateText
                        ),
                        border = null,
                        modifier = Modifier.testTag("quick_filter_${filter.name}")
                    )
                }
            }
        }

        // Vendeurs populaires
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vendeurs populaires",
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightNavy
                )
                TextButton(
                    onClick = onOpenExplore,
                    modifier = Modifier.testTag("home_see_all_vendors")
                ) {
                    Text("Tout voir", color = TurquoiseGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TurquoiseGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vendors, key = { it.id }) { vendor ->
                    CompactVendorHomeCard(
                        vendor = vendor,
                        onClick = { onVendorClick(vendor) }
                    )
                }
            }
        }

        // Produits recommandés
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Produits recommandés",
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightNavy
                )
                Text(
                    text = "Tarifs fictifs d'exemple en FCFA",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateText
                )
            }
        }

        if (products.isEmpty()) {
            item {
                EmptyStatePanel(
                    title = "Aucun produit trouvé",
                    subtitle = "Essayez un autre mot-clé ou réinitialisez les filtres.",
                    actionLabel = "Tout afficher",
                    onAction = {
                        onSearchChange("")
                        onSelectCategory(ProductCategory.ALL)
                        onSelectQuickFilter(HomeQuickFilter.ALL)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onProductClick = { onProductClick(product) },
                    onQuickAdd = { onQuickAdd(product) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveOrderHomeBanner(
    order: PilgrimOrder,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stage = order.stageEnum
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick)
            .testTag("active_order_home_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, TurquoiseGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TurquoiseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = TurquoiseGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "COMMANDE EN COURS • ${order.orderNumber}",
                            style = SmallCapsBadgeStyle,
                            color = TurquoiseGreen
                        )
                        Text(
                            text = stage.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Button(
                    onClick = onTrackClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidnightNavy,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("home_track_order_button")
                ) {
                    Text("Suivre")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { stage.stepIndex / 8f },
                color = TurquoiseGreen,
                trackColor = LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${order.vendorName} • ${order.tentNumber} (${order.sector})",
                style = MaterialTheme.typography.bodySmall,
                color = SlateText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactVendorHomeCard(
    vendor: Vendor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable(onClick = onClick)
            .testTag("home_vendor_${vendor.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, BorderHairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(LightGray)
            ) {
                Image(
                    painter = painterResource(id = vendor.imageRes),
                    contentDescription = vendor.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = MidnightNavy.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vendor.type.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MidnightNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = vendor.sampleOfferings,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                VendorAvailabilityChip(isOpen = vendor.isOpen)
            }
        }
    }
}
