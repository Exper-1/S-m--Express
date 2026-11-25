package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogData
import com.example.data.model.Product
import com.example.data.model.ProductAvailability
import com.example.data.model.Vendor
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ErrorSoftContainer
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ItalianaNumericStyle
import com.example.ui.theme.LightGray
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateText
import com.example.ui.theme.SmallCapsBadgeStyle
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.TurquoiseGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningSoftContainer
import com.example.ui.viewmodel.VisualNotification

@Composable
fun SemeTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Surface(
        color = PureWhite,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = MidnightNavy
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = SmallCapsBadgeStyle,
                        color = SlateText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            trailingAction?.invoke()
        }
    }
}

@Composable
fun AvailabilityBadge(availability: ProductAvailability, modifier: Modifier = Modifier) {
    val (bg, fg) = when (availability) {
        ProductAvailability.AVAILABLE -> TurquoiseContainer to TurquoiseGreen
        ProductAvailability.LIMITED -> WarningSoftContainer to WarningAmber
        ProductAvailability.UNAVAILABLE -> ErrorSoftContainer to ErrorCrimson
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = availability.label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VendorAvailabilityChip(isOpen: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isOpen) TurquoiseContainer else ErrorSoftContainer
    val fg = if (isOpen) TurquoiseGreen else ErrorCrimson
    val text = if (isOpen) "Ouvert • Livraison active" else "Fermé momentanément"
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = product.availability != ProductAvailability.UNAVAILABLE
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, BorderHairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LightGray)
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.vendorName,
                    style = SmallCapsBadgeStyle,
                    color = SoftGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MidnightNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                AvailabilityBadge(availability = product.availability)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = CatalogData.formatFcfa(product.priceFcfa),
                            style = ItalianaNumericStyle.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = MidnightNavy
                        )
                        Text(
                            text = "~${product.prepMinutes} min • Exemple",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateText
                        )
                    }
                    Button(
                        onClick = onQuickAdd,
                        enabled = isAvailable,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TurquoiseGreen,
                            contentColor = PureWhite,
                            disabledContainerColor = LightGray,
                            disabledContentColor = SlateText
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("quick_add_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter ${product.name}",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorCard(
    vendor: Vendor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("vendor_card_${vendor.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, BorderHairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(LightGray)
            ) {
                Image(
                    painter = painterResource(id = vendor.imageRes),
                    contentDescription = vendor.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Surface(
                    color = MidnightNavy.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vendor.type.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                    }
                }
                Surface(
                    color = PureWhite.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Note",
                            tint = SoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vendor.rating.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vendor.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MidnightNavy,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "~${vendor.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelLarge,
                        color = TurquoiseGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vendor.specialty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Produits : ${vendor.sampleOfferings}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidnightNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VendorAvailabilityChip(isOpen = vendor.isOpen)
                    Text(
                        text = vendor.zone,
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateText
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingCartSummaryBar(
    itemCount: Int,
    subtotalFcfa: Long,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 0) return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("floating_cart_bar"),
        color = MidnightNavy,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenCart)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SoftGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Panier",
                        tint = MidnightNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$itemCount article${if (itemCount > 1) "s" else ""} dans le panier",
                        style = MaterialTheme.typography.labelLarge,
                        color = PureWhite
                    )
                    Text(
                        text = "Sous-total : ${CatalogData.formatFcfa(subtotalFcfa)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldContainer
                    )
                }
            }
            Surface(
                color = TurquoiseGreen,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Voir panier",
                    style = MaterialTheme.typography.labelLarge,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun VisualNotificationBanner(
    notification: VisualNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        if (notification != null) {
            val bg = if (notification.isError) ErrorSoftContainer else MidnightNavy
            val fg = if (notification.isError) ErrorCrimson else PureWhite
            val icon = if (notification.isError) Icons.Default.ErrorOutline else Icons.Default.NotificationsActive
            Surface(
                color = bg,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, if (notification.isError) ErrorCrimson.copy(alpha = 0.3f) else SoftGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (notification.isError) ErrorCrimson else SoftGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = fg
                        )
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = fg.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer la notification",
                            tint = fg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePanel(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderHairline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GoldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MidnightNavy,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MidnightNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateText
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(actionLabel, color = MidnightNavy)
                }
            }
        }
    }
}

@Composable
fun ErrorStateBanner(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Surface(
        color = ErrorSoftContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Erreur",
                tint = ErrorCrimson
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorCrimson,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                OutlinedButton(onClick = onRetry) {
                    Text("Réessayer", color = ErrorCrimson)
                }
            }
        }
    }
}

@Composable
fun OrderStepCompleteIcon() {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Étape validée",
        tint = TurquoiseGreen,
        modifier = Modifier.size(22.dp)
    )
}
