package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.CatalogData
import com.example.data.model.DeliveryLocationDraft
import com.example.data.model.MobileMoneyOperator
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PilgrimOrder
import com.example.data.model.PilgrimProfile
import com.example.data.model.Product
import com.example.data.model.ProductAvailability
import com.example.data.model.ProductCategory
import com.example.data.model.SAMPLE_DELIVERY_FEE_FCFA
import com.example.data.model.VendorType
import com.example.data.repository.SemeRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class HomeQuickFilter(val label: String) {
    ALL("Tous"),
    AVAILABLE("Disponibles"),
    BUDGET("Moins de 2 000 F"),
    FAST("Rapide (<15 min)")
}

enum class ExploreVendorFilter(val label: String) {
    ALL("Tous"),
    RESTAURANTS("Restaurants"),
    INDEPENDENT("Vendeurs indépendants"),
    OPEN_ONLY("Ouverts")
}

data class VisualNotification(
    val title: String,
    val message: String,
    val isError: Boolean = false
)

class SemeViewModel(
    private val repository: SemeRepository,
    val currentUserId: String,
    private val initialDisplayName: String,
    val userEmail: String
) : ViewModel() {

    val profileState: StateFlow<UiState<PilgrimProfile?>> = repository.observeProfile(currentUserId)
        .map<PilgrimProfile?, UiState<PilgrimProfile?>> { profile ->
            if (profile != null && !_hasEditedDraft.value) {
                _deliveryDraft.value = DeliveryLocationDraft(
                    fullName = profile.fullName,
                    phone = profile.phone,
                    tentNumber = profile.tentNumber,
                    sector = profile.sector.ifBlank { CatalogData.authorizedSectors.first().name },
                    landmark = profile.landmark,
                    deliveryNotes = profile.deliveryNotes,
                    latitude = profile.latitude,
                    longitude = profile.longitude
                )
                _selectedPayment.value = enumValues<PaymentMethod>().firstOrNull {
                    it.name == profile.preferredPayment
                } ?: PaymentMethod.CASH_ON_DELIVERY
                _selectedMobileOperator.value = enumValues<MobileMoneyOperator>().firstOrNull {
                    it.name == profile.mobileMoneyNetwork
                } ?: MobileMoneyOperator.MTN
                _notificationsEnabled.value = profile.notificationsEnabled
            }
            UiState.Success(profile)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing profile", rootError)
            emit(UiState.Error(rootError.message ?: "Impossible de charger le profil."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val ordersState: StateFlow<UiState<List<PilgrimOrder>>> = repository.observeOrders(currentUserId)
        .map<List<PilgrimOrder>, UiState<List<PilgrimOrder>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing orders", rootError)
            emit(UiState.Error(rootError.message ?: "Impossible de charger les commandes."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _homeQuickFilter = MutableStateFlow(HomeQuickFilter.ALL)
    val homeQuickFilter: StateFlow<HomeQuickFilter> = _homeQuickFilter.asStateFlow()

    private val _exploreFilter = MutableStateFlow(ExploreVendorFilter.ALL)
    val exploreFilter: StateFlow<ExploreVendorFilter> = _exploreFilter.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _hasEditedDraft = MutableStateFlow(false)
    private val _deliveryDraft = MutableStateFlow(
        DeliveryLocationDraft(
            fullName = initialDisplayName.ifBlank { "Pèlerin Sèmè" },
            sector = CatalogData.authorizedSectors.first().name
        )
    )
    val deliveryDraft: StateFlow<DeliveryLocationDraft> = _deliveryDraft.asStateFlow()

    private val _selectedPayment = MutableStateFlow(PaymentMethod.CASH_ON_DELIVERY)
    val selectedPayment: StateFlow<PaymentMethod> = _selectedPayment.asStateFlow()

    private val _selectedMobileOperator = MutableStateFlow(MobileMoneyOperator.MTN)
    val selectedMobileOperator: StateFlow<MobileMoneyOperator> = _selectedMobileOperator.asStateFlow()

    private val _mobileMoneyPhone = MutableStateFlow("")
    val mobileMoneyPhone: StateFlow<String> = _mobileMoneyPhone.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _visualNotification = MutableStateFlow<VisualNotification?>(null)
    val visualNotification: StateFlow<VisualNotification?> = _visualNotification.asStateFlow()

    private val _lastConfirmedOrder = MutableStateFlow<PilgrimOrder?>(null)
    val lastConfirmedOrder: StateFlow<PilgrimOrder?> = _lastConfirmedOrder.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun selectHomeQuickFilter(filter: HomeQuickFilter) {
        _homeQuickFilter.value = filter
    }

    fun selectExploreFilter(filter: ExploreVendorFilter) {
        _exploreFilter.value = filter
    }

    fun addToCart(product: Product, quantity: Int = 1, note: String = "") {
        if (product.availability == ProductAvailability.UNAVAILABLE) {
            showBanner(
                title = "Produit indisponible",
                message = "${product.name} est temporairement épuisé.",
                isError = true
            )
            return
        }
        val safeQty = quantity.coerceIn(1, 20)
        _cartItems.update { current ->
            val index = current.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                current.toMutableList().apply {
                    val existing = this[index]
                    this[index] = existing.copy(
                        quantity = (existing.quantity + safeQty).coerceAtMost(30),
                        note = note.ifBlank { existing.note }
                    )
                }
            } else {
                current + CartItem(product = product, quantity = safeQty, note = note)
            }
        }
        showBanner(
            title = "Ajouté au panier",
            message = "$safeQty× ${product.name}"
        )
    }

    fun updateCartItemQuantity(productId: String, delta: Int) {
        _cartItems.update { current ->
            current.mapNotNull { item ->
                if (item.product.id == productId) {
                    val updatedQty = item.quantity + delta
                    if (updatedQty <= 0) null else item.copy(quantity = updatedQty.coerceAtMost(30))
                } else {
                    item
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { current -> current.filterNot { it.product.id == productId } }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun reorderFromHistory(order: PilgrimOrder) {
        val matchingProducts = CatalogData.products.filter { it.vendorId == order.vendorId && it.availability != ProductAvailability.UNAVAILABLE }
        if (matchingProducts.isNotEmpty()) {
            addToCart(matchingProducts.first(), 1)
        } else {
            val fallback = CatalogData.products.firstOrNull { it.availability == ProductAvailability.AVAILABLE }
            if (fallback != null) addToCart(fallback, 1)
        }
    }

    fun updateDeliveryDraft(transform: (DeliveryLocationDraft) -> DeliveryLocationDraft) {
        _hasEditedDraft.value = true
        _deliveryDraft.update(transform)
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPayment.value = method
        if (method == PaymentMethod.MOBILE_MONEY && _mobileMoneyPhone.value.isBlank()) {
            _mobileMoneyPhone.value = _deliveryDraft.value.phone
        }
    }

    fun selectMobileOperator(operator: MobileMoneyOperator) {
        _selectedMobileOperator.value = operator
    }

    fun updateMobileMoneyPhone(phone: String) {
        _mobileMoneyPhone.value = phone
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        saveProfilePreferences(showFeedback = false)
    }

    fun saveProfilePreferences(showFeedback: Boolean = true) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.saveProfile(
                userId = currentUserId,
                draft = _deliveryDraft.value,
                preferredPayment = _selectedPayment.value,
                mobileMoneyOperator = _selectedMobileOperator.value,
                notificationsEnabled = _notificationsEnabled.value
            )
            _isSubmitting.value = false
            if (showFeedback) {
                result.onSuccess {
                    showBanner("Profil enregistré", "Vos repères de livraison à Sèmè ont été sauvegardés.")
                }.onFailure { err ->
                    showBanner("Erreur de sauvegarde", err.message ?: "Veuillez réessayer.", isError = true)
                }
            }
        }
    }

    fun submitOrder(onSuccess: (PilgrimOrder) -> Unit) {
        val items = _cartItems.value
        val draft = _deliveryDraft.value
        val payment = _selectedPayment.value
        val mmoPhone = _mobileMoneyPhone.value.ifBlank { draft.phone }

        if (items.isEmpty()) {
            showBanner("Panier vide", "Ajoutez au moins un article avant de commander.", isError = true)
            return
        }
        if (draft.tentNumber.isBlank() || draft.sector.isBlank() || draft.phone.trim().length < 4) {
            showBanner(
                "Adresse incomplète",
                "Renseignez le numéro de tente, le secteur et un téléphone joignable.",
                isError = true
            )
            return
        }
        if (payment == PaymentMethod.MOBILE_MONEY && mmoPhone.trim().length < 6) {
            showBanner(
                "Numéro Mobile Money requis",
                "Saisissez un numéro valide pour ${_selectedMobileOperator.value.label}.",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            repository.saveProfile(
                userId = currentUserId,
                draft = draft,
                preferredPayment = payment,
                mobileMoneyOperator = _selectedMobileOperator.value,
                notificationsEnabled = _notificationsEnabled.value
            )
            val result = repository.placeOrder(
                userId = currentUserId,
                cartItems = items,
                delivery = draft,
                paymentMethod = payment,
                mobileMoneyOperator = _selectedMobileOperator.value,
                mobileMoneyNumber = mmoPhone
            )
            _isSubmitting.value = false
            result.onSuccess { created ->
                _lastConfirmedOrder.value = created
                _cartItems.value = emptyList()
                showBanner(
                    "Commande confirmée",
                    "Commande ${created.orderNumber} transmise à ${created.vendorName}."
                )
                onSuccess(created)
            }.onFailure { err ->
                showBanner(
                    "Échec de la commande",
                    err.message ?: "Impossible d'enregistrer la commande.",
                    isError = true
                )
            }
        }
    }

    fun advanceOrderStage(order: PilgrimOrder) {
        viewModelScope.launch {
            val result = repository.advanceOrderStatus(
                userId = currentUserId,
                orderId = order.id,
                currentStage = order.stageEnum
            )
            result.onSuccess { newStage ->
                if (_notificationsEnabled.value) {
                    showBanner(
                        "Suivi mis à jour",
                        "${order.orderNumber} : ${newStage.label}"
                    )
                }
            }.onFailure { err ->
                showBanner(
                    "Mise à jour impossible",
                    err.message ?: "Erreur réseau.",
                    isError = true
                )
            }
        }
    }

    fun cartSubtotalFcfa(): Long = _cartItems.value.sumOf { it.lineTotalFcfa }

    fun cartTotalWithDeliveryFcfa(): Long {
        val sub = cartSubtotalFcfa()
        return if (sub == 0L) 0L else sub + SAMPLE_DELIVERY_FEE_FCFA
    }

    fun filteredVendors() = CatalogData.vendors.filter { vendor ->
        val query = _searchQuery.value.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
            vendor.name.lowercase().contains(query) ||
            vendor.specialty.lowercase().contains(query) ||
            vendor.sampleOfferings.lowercase().contains(query)
        val matchesFilter = when (_exploreFilter.value) {
            ExploreVendorFilter.ALL -> true
            ExploreVendorFilter.RESTAURANTS -> vendor.type == VendorType.RESTAURANT
            ExploreVendorFilter.INDEPENDENT -> vendor.type == VendorType.INDEPENDENT
            ExploreVendorFilter.OPEN_ONLY -> vendor.isOpen
        }
        matchesQuery && matchesFilter
    }

    fun filteredProducts(): List<Product> {
        val query = _searchQuery.value.trim().lowercase()
        val category = _selectedCategory.value
        val quick = _homeQuickFilter.value
        return CatalogData.products.filter { product ->
            val matchesQuery = query.isEmpty() ||
                product.name.lowercase().contains(query) ||
                product.vendorName.lowercase().contains(query) ||
                product.description.lowercase().contains(query)
            val matchesCategory = category == ProductCategory.ALL || product.category == category
            val matchesQuick = when (quick) {
                HomeQuickFilter.ALL -> true
                HomeQuickFilter.AVAILABLE -> product.availability == ProductAvailability.AVAILABLE
                HomeQuickFilter.BUDGET -> product.priceFcfa < 2000L
                HomeQuickFilter.FAST -> product.prepMinutes <= 15
            }
            matchesQuery && matchesCategory && matchesQuick
        }
    }

    fun showBanner(title: String, message: String, isError: Boolean = false) {
        _visualNotification.value = VisualNotification(title, message, isError)
    }

    fun dismissBanner() {
        _visualNotification.value = null
    }

    private companion object {
        const val TAG = "SemeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
