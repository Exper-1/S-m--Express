package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.DeliveryLocationDraft
import com.example.data.model.MobileMoneyOperator
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PilgrimOrder
import com.example.data.model.PilgrimProfile
import com.example.data.model.SAMPLE_DELIVERY_FEE_FCFA
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SemeRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("L'utilisateur doit être connecté avec Google.")
    }

    fun observeProfile(userId: String): Flow<PilgrimProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session utilisateur modifiée." }
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            PilgrimProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeOrders(userId: String): Flow<List<PilgrimOrder>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session utilisateur modifiée." }
        val collectionRef = db.collection("users").document(uid).collection("orders")
        val path = collectionRef.path
        emitAll(
            collectionRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PilgrimOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveProfile(
        userId: String,
        draft: DeliveryLocationDraft,
        preferredPayment: PaymentMethod,
        mobileMoneyOperator: MobileMoneyOperator,
        notificationsEnabled: Boolean
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Session utilisateur modifiée." }
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path

            val safeName = draft.fullName.trim().ifBlank {
                auth.currentUser?.displayName?.take(80)?.ifBlank { "Pèlerin Sèmè" } ?: "Pèlerin Sèmè"
            }.take(80)
            val safePhone = draft.phone.trim().take(30)
            val safeTent = draft.tentNumber.trim().take(40)
            val safeSector = draft.sector.trim().take(60)
            val safeLandmark = draft.landmark.trim().take(120)
            val safeNotes = draft.deliveryNotes.trim().take(250)

            val existing = docRef.get().await()
            if (existing.exists()) {
                val updatePayload = mutableMapOf<String, Any?>(
                    "fullName" to safeName,
                    "phone" to safePhone,
                    "tentNumber" to safeTent,
                    "sector" to safeSector,
                    "landmark" to safeLandmark,
                    "deliveryNotes" to safeNotes,
                    "latitude" to draft.latitude,
                    "longitude" to draft.longitude,
                    "preferredPayment" to preferredPayment.name,
                    "mobileMoneyNetwork" to mobileMoneyOperator.name,
                    "notificationsEnabled" to notificationsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "fullName" to safeName,
                    "phone" to safePhone,
                    "tentNumber" to safeTent,
                    "sector" to safeSector,
                    "landmark" to safeLandmark,
                    "deliveryNotes" to safeNotes,
                    "latitude" to draft.latitude,
                    "longitude" to draft.longitude,
                    "preferredPayment" to preferredPayment.name,
                    "mobileMoneyNetwork" to mobileMoneyOperator.name,
                    "notificationsEnabled" to notificationsEnabled,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun placeOrder(
        userId: String,
        cartItems: List<CartItem>,
        delivery: DeliveryLocationDraft,
        paymentMethod: PaymentMethod,
        mobileMoneyOperator: MobileMoneyOperator,
        mobileMoneyNumber: String
    ): Result<PilgrimOrder> {
        var docPath = "users/$userId/orders"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Session utilisateur modifiée." }
            require(cartItems.isNotEmpty()) { "Le panier est vide." }
            require(delivery.tentNumber.trim().isNotEmpty()) { "Le numéro ou nom de tente est requis." }
            require(delivery.sector.trim().isNotEmpty()) { "Le secteur ou la rangée est requis." }
            require(delivery.phone.trim().length >= 4) { "Un numéro de téléphone joignable est requis." }

            val docRef = db.collection("users").document(uid).collection("orders").document()
            docPath = docRef.path

            val subtotal = cartItems.sumOf { it.lineTotalFcfa }
            val deliveryFee = SAMPLE_DELIVERY_FEE_FCFA
            val total = subtotal + deliveryFee
            val totalCount = cartItems.sumOf { it.quantity.toLong() }.coerceIn(1L, 100L)

            val distinctVendors = cartItems.map { it.product.vendorName }.distinct()
            val primaryVendorId = cartItems.first().product.vendorId.take(60)
            val primaryVendorName = if (distinctVendors.size == 1) {
                distinctVendors.first().take(100)
            } else {
                "${distinctVendors.first()} (+${distinctVendors.size - 1})".take(100)
            }

            val summary = cartItems.joinToString(", ") { item ->
                "${item.quantity}x ${item.product.name}"
            }.take(600)

            val orderNumber = "SE-2027-${(1000..9999).random()}"
            val mmoNetwork = if (paymentMethod == PaymentMethod.MOBILE_MONEY) {
                mobileMoneyOperator.name
            } else {
                ""
            }
            val mmoPhone = if (paymentMethod == PaymentMethod.MOBILE_MONEY) {
                mobileMoneyNumber.trim().take(30)
            } else {
                ""
            }

            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "orderNumber" to orderNumber,
                "vendorId" to primaryVendorId,
                "vendorName" to primaryVendorName,
                "itemsSummary" to summary,
                "itemCount" to totalCount,
                "subtotalFcfa" to subtotal,
                "deliveryFeeFcfa" to deliveryFee,
                "totalFcfa" to total,
                "paymentMethod" to paymentMethod.name,
                "mobileMoneyNetwork" to mmoNetwork,
                "mobileMoneyNumber" to mmoPhone,
                "status" to OrderStage.RECEIVED.code,
                "courierName" to "",
                "courierPhone" to "",
                "tentNumber" to delivery.tentNumber.trim().take(40),
                "sector" to delivery.sector.trim().take(60),
                "landmark" to delivery.landmark.trim().take(120),
                "deliveryNotes" to delivery.deliveryNotes.trim().take(250),
                "contactPhone" to delivery.phone.trim().take(30),
                "latitude" to delivery.latitude,
                "longitude" to delivery.longitude,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            docRef.set(payload).await()

            val createdOrder = PilgrimOrder(
                id = docRef.id,
                userId = uid,
                orderNumber = orderNumber,
                vendorId = primaryVendorId,
                vendorName = primaryVendorName,
                itemsSummary = summary,
                itemCount = totalCount,
                subtotalFcfa = subtotal,
                deliveryFeeFcfa = deliveryFee,
                totalFcfa = total,
                paymentMethod = paymentMethod.name,
                mobileMoneyNetwork = mmoNetwork,
                mobileMoneyNumber = mmoPhone,
                status = OrderStage.RECEIVED.code,
                tentNumber = delivery.tentNumber.trim().take(40),
                sector = delivery.sector.trim().take(60),
                landmark = delivery.landmark.trim().take(120),
                deliveryNotes = delivery.deliveryNotes.trim().take(250),
                contactPhone = delivery.phone.trim().take(30),
                latitude = delivery.latitude,
                longitude = delivery.longitude,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            Result.success(createdOrder)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun advanceOrderStatus(
        userId: String,
        orderId: String,
        currentStage: OrderStage
    ): Result<OrderStage> {
        var docPath = "users/$userId/orders/$orderId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Session utilisateur modifiée." }
            require(orderId.isNotBlank()) { "Identifiant de commande invalide." }
            val nextStage = currentStage.nextStageOrNull()
                ?: return Result.success(OrderStage.DELIVERED)

            val docRef = db.collection("users").document(uid).collection("orders").document(orderId)
            docPath = docRef.path

            val updateMap = mutableMapOf<String, Any>(
                "status" to nextStage.code,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (nextStage == OrderStage.COURIER_ASSIGNED) {
                updateMap["courierName"] = "Moussa Dossou"
                updateMap["courierPhone"] = "+229 01 96 44 55 66"
            }

            docRef.update(updateMap).await()
            Result.success(nextStage)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }
}
