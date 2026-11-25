package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.ProductAvailability
import com.example.data.model.ProductCategory
import com.example.data.model.Vendor
import com.example.ui.components.AvailabilityBadge
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.ProductCard
import com.example.ui.components.SemeTopBar
import com.example.ui.components.VendorAvailabilityChip
import com.example.ui.components.VendorCard
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.EditorialHeadingStyle
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ItalianaNumericStyle
import com.example.ui.theme.Ivory
import com.example.ui.theme.LightGray
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseGreen
import com.example.ui.viewmodel.ExploreVendorFilter

@Composable
fun ExploreVendorsScreen(
    searchQuery: String,
    exploreFilter: ExploreVendorFilter,
    vendors: List<Vendor>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (ExploreVendorFilter) -> Unit,
    onVendorClick: (Vendor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Explorer les vendeurs",
            subtitle = "Restaurants & Vendeurs indépendants • Sèmè"
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Filtrer par nom, plat ou spécialité...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher un vendeur",
                            tint = MidnightNavy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Effacer",
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
                        focusedBorderColor = MidnightNavy,
                        unfocusedBorderColor = BorderHairline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ExploreVendorFilter.entries) { filter ->
                        val selected = filter == exploreFilter
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MidnightNavy,
                                selectedLabelColor = PureWhite,
                                containerColor = PureWhite,
                                labelColor = MidnightNavy
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("explore_filter_${filter.name}")
                        )
                    }
                }
            }

            if (vendors.isEmpty()) {
                item {
                    EmptyStatePanel(
                        title = "Aucun vendeur correspondant",
                        subtitle = "Modifiez vos critères pour afficher tous les stands disponibles.",
                        actionLabel = "Réinitialiser",
                        onAction = {
                            onSearchChange("")
                            onFilterChange(ExploreVendorFilter.ALL)
                        }
                    )
                }
            } else {
                items(vendors, key = { it.id }) { vendor ->
                    VendorCard(
                        vendor = vendor,
                        onClick = { onVendorClick(vendor) }
                    )
                }
            }
        }
    }
}

@Composable
fun VendorDetailScreen(
    vendor: Vendor,
    products: List<Product>,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ProductCategory.ALL) }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == ProductCategory.ALL) products
        else products.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = vendor.name,
            subtitle = vendor.type.label,
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = vendor.imageRes),
                        contentDescription = vendor.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MidnightNavy.copy(alpha = 0.15f),
                                        MidnightNavy.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        VendorAvailabilityChip(isOpen = vendor.isOpen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = vendor.name,
                            style = EditorialHeadingStyle,
                            color = PureWhite
                        )
                        Text(
                            text = vendor.specialty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldContainer
                        )
                    }
                }
            }

            item {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SoftGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${vendor.rating}", style = MaterialTheme.typography.titleMedium, color = MidnightNavy)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TurquoiseGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("~${vendor.estimatedMinutes} min", style = MaterialTheme.typography.bodyMedium, color = MidnightNavy)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(vendor.zone, style = MaterialTheme.typography.bodySmall, color = SlateText)
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Catalogue du vendeur",
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
                                onClick = { selectedCategory = category },
                                label = { Text(category.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MidnightNavy,
                                    selectedLabelColor = PureWhite,
                                    containerColor = PureWhite,
                                    labelColor = MidnightNavy
                                )
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    EmptyStatePanel(
                        title = "Aucun article dans cette catégorie",
                        subtitle = "Sélectionnez « Tout » pour voir l'ensemble des produits de ${vendor.name}.",
                        actionLabel = "Tout voir",
                        onAction = { selectedCategory = ProductCategory.ALL },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { onProductClick(product) },
                        onQuickAdd = { onQuickAdd(product) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (Product, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    var note by remember(product.id) { mutableStateOf("") }
    val isAvailable = product.availability != ProductAvailability.UNAVAILABLE

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        SemeTopBar(
            title = "Détail du produit",
            subtitle = product.vendorName,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(LightGray)
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                AvailabilityBadge(
                    availability = product.availability,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = product.vendorName.uppercase(),
                    style = SmallCapsBadgeStyle,
                    color = SoftGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.name,
                    style = EditorialHeadingStyle,
                    color = MidnightNavy
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CatalogData.formatFcfa(product.priceFcfa),
                        style = ItalianaNumericStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = TurquoiseGreen
                    )
                    Surface(
                        color = GoldContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Préparation ~${product.prepMinutes} min",
                            style = MaterialTheme.typography.labelMedium,
                            color = MidnightNavy,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SlateText
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Précision pour le vendeur (optionnel)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MidnightNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(120) },
                    placeholder = { Text("Ex: Peu de piment, bien chaud...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                        focusedBorderColor = MidnightNavy,
                        unfocusedBorderColor = BorderHairline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_note_input")
                )
                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantité",
                            style = MaterialTheme.typography.titleMedium,
                            color = MidnightNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = LightGray,
                                modifier = Modifier.size(48.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.testTag("product_qty_minus")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuer", tint = MidnightNavy)
                                }
                            }
                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MidnightNavy,
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                            Surface(
                                shape = CircleShape,
                                color = MidnightNavy,
                                modifier = Modifier.size(48.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity < 20) quantity++ },
                                    modifier = Modifier.testTag("product_qty_plus")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Augmenter", tint = PureWhite)
                                }
                            }
                        }
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
                onClick = { onAddToCart(product, quantity, note) },
                enabled = isAvailable,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TurquoiseGreen,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 56.dp)
                    .testTag("product_add_to_cart_button")
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAvailable) {
                        "Ajouter au panier • ${CatalogData.formatFcfa(product.priceFcfa * quantity)}"
                    } else {
                        "Produit indisponible"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
