package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class PilgrimProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val tentNumber: String = "",
    val sector: String = "",
    val landmark: String = "",
    val deliveryNotes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val preferredPayment: String = PaymentMethod.CASH_ON_DELIVERY.name,
    val mobileMoneyNetwork: String = MobileMoneyOperator.MTN.name,
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PilgrimOrder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderNumber: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val itemsSummary: String = "",
    val itemCount: Long = 0L,
    val subtotalFcfa: Long = 0L,
    val deliveryFeeFcfa: Long = SAMPLE_DELIVERY_FEE_FCFA,
    val totalFcfa: Long = 0L,
    val paymentMethod: String = PaymentMethod.CASH_ON_DELIVERY.name,
    val mobileMoneyNetwork: String = "",
    val mobileMoneyNumber: String = "",
    val status: String = OrderStage.RECEIVED.code,
    val courierName: String = "",
    val courierPhone: String = "",
    val tentNumber: String = "",
    val sector: String = "",
    val landmark: String = "",
    val deliveryNotes: String = "",
    val contactPhone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val stageEnum: OrderStage
        get() = OrderStage.fromCode(status)

    val isActiveOrder: Boolean
        get() = stageEnum != OrderStage.DELIVERED
}
