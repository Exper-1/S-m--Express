package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOut
import com.example.data.model.CatalogData
import com.example.data.repository.SemeRepository
import com.example.ui.components.FloatingCartSummaryBar
import com.example.ui.components.VisualNotificationBanner
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.DeliveryLocationScreen
import com.example.ui.screens.ExploreVendorsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VendorDetailScreen
import com.example.ui.theme.Ivory
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateText
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.TurquoiseGreen
import com.example.ui.viewmodel.SemeViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

object SemeRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val VENDOR_DETAIL = "vendor/{vendorId}"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val LOCATION = "location"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation"
    const val TRACKING = "tracking/{orderId}"

    fun vendorDetail(vendorId: String) = "vendor/$vendorId"
    fun productDetail(productId: String) = "product/$productId"
    fun tracking(orderId: String) = "tracking/$orderId"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val bottomNavTabs = listOf(
    BottomNavTab(SemeRoutes.HOME, "Accueil", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    BottomNavTab(SemeRoutes.EXPLORE, "Explorer", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    BottomNavTab(SemeRoutes.ORDERS, "Commandes", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_orders"),
    BottomNavTab(SemeRoutes.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
)

@Composable
fun SemeAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var onboardingSeen by rememberSaveable { mutableStateOf(false) }

    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    when {
        showSplash -> {
            SplashScreen(onFinished = { showSplash = false })
        }
        !onboardingSeen && currentUser == null -> {
            OnboardingScreen(onComplete = { onboardingSeen = true })
        }
        currentUser == null -> {
            AuthScreen(onAuthSuccess = {})
        }
        else -> {
            val user = currentUser!!
            key(user.uid) {
                AuthenticatedSemeShell(
                    user = user,
                    onSignOut = {
                        signOut(
                            credentialManager = credentialManager,
                            scope = scope
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedSemeShell(
    user: FirebaseUser,
    onSignOut: () -> Unit
) {
    val viewModel: SemeViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SemeViewModel(
                    repository = SemeRepository(db),
                    currentUserId = user.uid,
                    initialDisplayName = user.displayName.orEmpty(),
                    userEmail = user.email.orEmpty()
                )
            }
        }
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val homeQuickFilter by viewModel.homeQuickFilter.collectAsStateWithLifecycle()
    val exploreFilter by viewModel.exploreFilter.collectAsStateWithLifecycle()
    val deliveryDraft by viewModel.deliveryDraft.collectAsStateWithLifecycle()
    val selectedPayment by viewModel.selectedPayment.collectAsStateWithLifecycle()
    val selectedOperator by viewModel.selectedMobileOperator.collectAsStateWithLifecycle()
    val mobileMoneyPhone by viewModel.mobileMoneyPhone.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val visualNotification by viewModel.visualNotification.collectAsStateWithLifecycle()
    val lastConfirmedOrder by viewModel.lastConfirmedOrder.collectAsStateWithLifecycle()

    LaunchedEffect(visualNotification) {
        if (visualNotification != null) {
            delay(3200)
            viewModel.dismissBanner()
        }
    }

    val ordersList = (ordersState as? UiState.Success)?.data.orEmpty()
    val activeOrder = remember(ordersList) { ordersList.firstOrNull { it.isActiveOrder } }

    val topLevelRoutes = remember {
        setOf(SemeRoutes.HOME, SemeRoutes.EXPLORE, SemeRoutes.ORDERS, SemeRoutes.PROFILE)
    }
    val showBottomBar = currentRoute in topLevelRoutes
    val showCartBar = currentRoute in setOf(
        SemeRoutes.HOME,
        SemeRoutes.EXPLORE,
        SemeRoutes.VENDOR_DETAIL
    ) && cartItems.isNotEmpty()

    val cartItemCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.lineTotalFcfa }
    val cartTotal = if (cartSubtotal == 0L) 0L else cartSubtotal + com.example.data.model.SAMPLE_DELIVERY_FEE_FCFA

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && showBottomBar) {
                NavigationRail(
                    containerColor = PureWhite,
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    bottomNavTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TurquoiseGreen,
                                selectedTextColor = MidnightNavy,
                                indicatorColor = TurquoiseContainer,
                                unselectedIconColor = SlateText,
                                unselectedTextColor = SlateText
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }

            Scaffold(
                containerColor = Ivory,
                bottomBar = {
                    if (!useRail && showBottomBar) {
                        NavigationBar(
                            containerColor = PureWhite,
                            tonalElevation = 4.dp
                        ) {
                            bottomNavTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TurquoiseGreen,
                                        selectedTextColor = MidnightNavy,
                                        indicatorColor = TurquoiseContainer,
                                        unselectedIconColor = SlateText,
                                        unselectedTextColor = SlateText
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SemeRoutes.HOME,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(SemeRoutes.HOME) {
                            val vendors = remember(searchQuery) {
                                CatalogData.vendors.filter { v ->
                                    val q = searchQuery.trim().lowercase()
                                    q.isEmpty() || v.name.lowercase().contains(q) || v.specialty.lowercase().contains(q)
                                }
                            }
                            val products = remember(searchQuery, selectedCategory, homeQuickFilter) {
                                viewModel.filteredProducts()
                            }
                            HomeScreen(
                                deliveryDraft = deliveryDraft,
                                activeOrder = activeOrder,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                quickFilter = homeQuickFilter,
                                vendors = vendors,
                                products = products,
                                onSearchChange = viewModel::updateSearchQuery,
                                onSelectCategory = viewModel::selectCategory,
                                onSelectQuickFilter = viewModel::selectHomeQuickFilter,
                                onEditLocation = { navController.navigate(SemeRoutes.LOCATION) },
                                onTrackOrder = { order -> navController.navigate(SemeRoutes.tracking(order.id)) },
                                onOpenExplore = { navigateTopTab(SemeRoutes.EXPLORE) },
                                onVendorClick = { vendor -> navController.navigate(SemeRoutes.vendorDetail(vendor.id)) },
                                onProductClick = { product -> navController.navigate(SemeRoutes.productDetail(product.id)) },
                                onQuickAdd = { product -> viewModel.addToCart(product) }
                            )
                        }

                        composable(SemeRoutes.EXPLORE) {
                            val vendors = remember(searchQuery, exploreFilter) {
                                viewModel.filteredVendors()
                            }
                            ExploreVendorsScreen(
                                searchQuery = searchQuery,
                                exploreFilter = exploreFilter,
                                vendors = vendors,
                                onSearchChange = viewModel::updateSearchQuery,
                                onFilterChange = viewModel::selectExploreFilter,
                                onVendorClick = { vendor -> navController.navigate(SemeRoutes.vendorDetail(vendor.id)) }
                            )
                        }

                        composable(SemeRoutes.ORDERS) {
                            OrdersScreen(
                                ordersState = ordersState,
                                onOrderClick = { order -> navController.navigate(SemeRoutes.tracking(order.id)) },
                                onReorder = { order ->
                                    viewModel.reorderFromHistory(order)
                                    navController.navigate(SemeRoutes.CART)
                                },
                                onExploreClick = { navigateTopTab(SemeRoutes.EXPLORE) }
                            )
                        }

                        composable(SemeRoutes.PROFILE) {
                            val profileName = (profileState as? UiState.Success)?.data?.fullName
                                ?: deliveryDraft.fullName
                            ProfileScreen(
                                displayName = profileName,
                                email = viewModel.userEmail,
                                draft = deliveryDraft,
                                preferredPayment = selectedPayment,
                                mobileMoneyOperator = selectedOperator,
                                notificationsEnabled = notificationsEnabled,
                                ordersCount = ordersList.size,
                                onEditDeliveryAddress = { navController.navigate(SemeRoutes.LOCATION) },
                                onToggleNotifications = viewModel::toggleNotifications,
                                onSavePreferences = { viewModel.saveProfilePreferences(showFeedback = true) },
                                onSignOut = onSignOut
                            )
                        }

                        composable(
                            route = SemeRoutes.VENDOR_DETAIL,
                            arguments = listOf(navArgument("vendorId") { type = NavType.StringType })
                        ) { entry ->
                            val vendorId = entry.arguments?.getString("vendorId").orEmpty()
                            val vendor = CatalogData.vendors.firstOrNull { it.id == vendorId }
                                ?: CatalogData.vendors.first()
                            val vendorProducts = CatalogData.products.filter { it.vendorId == vendor.id }
                            VendorDetailScreen(
                                vendor = vendor,
                                products = vendorProducts,
                                onBack = { navController.popBackStack() },
                                onProductClick = { product -> navController.navigate(SemeRoutes.productDetail(product.id)) },
                                onQuickAdd = { product -> viewModel.addToCart(product) }
                            )
                        }

                        composable(
                            route = SemeRoutes.PRODUCT_DETAIL,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { entry ->
                            val productId = entry.arguments?.getString("productId").orEmpty()
                            val product = CatalogData.products.firstOrNull { it.id == productId }
                                ?: CatalogData.products.first()
                            ProductDetailScreen(
                                product = product,
                                onBack = { navController.popBackStack() },
                                onAddToCart = { prod, qty, note ->
                                    viewModel.addToCart(prod, qty, note)
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(SemeRoutes.CART) {
                            CartScreen(
                                cartItems = cartItems,
                                subtotalFcfa = cartSubtotal,
                                totalFcfa = cartTotal,
                                onBack = { navController.popBackStack() },
                                onQuantityChange = viewModel::updateCartItemQuantity,
                                onRemoveItem = viewModel::removeFromCart,
                                onClearCart = viewModel::clearCart,
                                onExploreClick = {
                                    navController.popBackStack()
                                    navigateTopTab(SemeRoutes.EXPLORE)
                                },
                                onProceedToLocation = { navController.navigate(SemeRoutes.LOCATION) }
                            )
                        }

                        composable(SemeRoutes.LOCATION) {
                            DeliveryLocationScreen(
                                draft = deliveryDraft,
                                onDraftChange = viewModel::updateDeliveryDraft,
                                onBack = { navController.popBackStack() },
                                onContinueToCheckout = {
                                    if (cartItems.isEmpty()) {
                                        viewModel.saveProfilePreferences(showFeedback = true)
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(SemeRoutes.CHECKOUT)
                                    }
                                }
                            )
                        }

                        composable(SemeRoutes.CHECKOUT) {
                            CheckoutScreen(
                                cartItems = cartItems,
                                delivery = deliveryDraft,
                                selectedPayment = selectedPayment,
                                selectedOperator = selectedOperator,
                                mobileMoneyPhone = mobileMoneyPhone,
                                subtotalFcfa = cartSubtotal,
                                totalFcfa = cartTotal,
                                isSubmitting = isSubmitting,
                                onSelectPayment = viewModel::selectPaymentMethod,
                                onSelectOperator = viewModel::selectMobileOperator,
                                onMobilePhoneChange = viewModel::updateMobileMoneyPhone,
                                onEditDelivery = { navController.navigate(SemeRoutes.LOCATION) },
                                onBack = { navController.popBackStack() },
                                onConfirmOrder = {
                                    viewModel.submitOrder {
                                        navController.navigate(SemeRoutes.CONFIRMATION) {
                                            popUpTo(SemeRoutes.HOME)
                                        }
                                    }
                                }
                            )
                        }

                        composable(SemeRoutes.CONFIRMATION) {
                            val confirmed = lastConfirmedOrder ?: activeOrder ?: ordersList.firstOrNull()
                            if (confirmed != null) {
                                OrderConfirmationScreen(
                                    order = confirmed,
                                    onTrackOrder = {
                                        navController.navigate(SemeRoutes.tracking(confirmed.id)) {
                                            popUpTo(SemeRoutes.HOME)
                                        }
                                    },
                                    onBackHome = {
                                        navController.navigate(SemeRoutes.HOME) {
                                            popUpTo(SemeRoutes.HOME) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable(
                            route = SemeRoutes.TRACKING,
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { entry ->
                            val orderId = entry.arguments?.getString("orderId").orEmpty()
                            val order = ordersList.firstOrNull { it.id == orderId }
                                ?: lastConfirmedOrder?.takeIf { it.id == orderId }
                                ?: ordersList.firstOrNull()
                            if (order != null) {
                                OrderTrackingScreen(
                                    order = order,
                                    onBack = { navController.popBackStack() },
                                    onAdvanceStage = viewModel::advanceOrderStage
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Commande introuvable.", color = MidnightNavy)
                                }
                            }
                        }
                    }

                    if (showCartBar) {
                        FloatingCartSummaryBar(
                            itemCount = cartItemCount,
                            subtotalFcfa = cartSubtotal,
                            onOpenCart = { navController.navigate(SemeRoutes.CART) },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    VisualNotificationBanner(
                        notification = visualNotification,
                        onDismiss = viewModel::dismissBanner,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
