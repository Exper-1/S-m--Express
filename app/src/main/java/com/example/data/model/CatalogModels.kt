package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

const val SAMPLE_DELIVERY_FEE_FCFA: Long = 300L

enum class ProductCategory(val label: String) {
    ALL("Tout"),
    MEALS("Repas chauds"),
    DRINKS("Boissons"),
    SNACKS("Snacks & Matin"),
    ESSENTIALS("Articles utiles")
}

enum class VendorType(val label: String) {
    RESTAURANT("Restaurant"),
    INDEPENDENT("Vendeur indépendant")
}

enum class ProductAvailability(val label: String) {
    AVAILABLE("Disponible"),
    LIMITED("Stock limité"),
    UNAVAILABLE("Indisponible")
}

enum class PaymentMethod(val label: String, val description: String) {
    CASH_ON_DELIVERY("Paiement à la livraison", "Règlement en espèces (FCFA) à la réception"),
    MOBILE_MONEY("Paiement Mobile Money", "MTN MoMo, Moov Money ou Celtiis Cash")
}

enum class MobileMoneyOperator(val label: String) {
    MTN("MTN MoMo"),
    MOOV("Moov Money"),
    CELTIIS("Celtiis Cash")
}

enum class OrderStage(
    val code: String,
    val label: String,
    val subtitle: String,
    val stepIndex: Int
) {
    RECEIVED("RECEIVED", "Commande reçue", "Votre commande a bien été enregistrée.", 1),
    ACCEPTED("ACCEPTED", "Commande acceptée", "Le vendeur a confirmé la disponibilité.", 2),
    PREPARING("PREPARING", "Préparation en cours", "Votre commande est en cours de préparation.", 3),
    READY("READY", "Commande prête", "Emballée et prête pour le retrait.", 4),
    COURIER_ASSIGNED("COURIER_ASSIGNED", "Livreur affecté", "Un livreur du site a pris votre mission.", 5),
    PICKED_UP("PICKED_UP", "Commande récupérée", "Le livreur a récupéré votre colis au stand.", 6),
    DELIVERING("DELIVERING", "Livraison en cours", "En route vers votre tente et secteur.", 7),
    DELIVERED("DELIVERED", "Commande livrée", "Commande remise avec succès. Bon appétit !", 8);

    fun nextStageOrNull(): OrderStage? {
        val all = entries
        val idx = all.indexOf(this)
        return if (idx in 0 until all.lastIndex) all[idx + 1] else null
    }

    companion object {
        fun fromCode(code: String): OrderStage =
            entries.firstOrNull { it.code == code } ?: RECEIVED
    }
}

data class Vendor(
    val id: String,
    val name: String,
    val type: VendorType,
    val specialty: String,
    val zone: String,
    val estimatedMinutes: Int,
    val rating: Double,
    val isOpen: Boolean,
    val sampleOfferings: String,
    @DrawableRes val imageRes: Int
)

data class Product(
    val id: String,
    val vendorId: String,
    val vendorName: String,
    val name: String,
    val description: String,
    val priceFcfa: Long,
    val category: ProductCategory,
    val availability: ProductAvailability,
    val prepMinutes: Int,
    val isPopular: Boolean,
    @DrawableRes val imageRes: Int
)

data class CartItem(
    val product: Product,
    val quantity: Int,
    val note: String = ""
) {
    val lineTotalFcfa: Long get() = product.priceFcfa * quantity
}

data class DeliveryLocationDraft(
    val fullName: String = "",
    val phone: String = "",
    val tentNumber: String = "",
    val sector: String = "Secteur Nord - Rangée 4",
    val landmark: String = "",
    val deliveryNotes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SiteSector(
    val name: String,
    val zoneCode: String,
    val hint: String,
    val authorized: Boolean = true
)

object CatalogData {
    val authorizedSectors = listOf(
        SiteSector("Secteur Nord - Rangée 4", "N4", "Près du Portail Est & Point d'eau"),
        SiteSector("Esplanade Centrale - Tentes A", "EC", "Autour de la grande allée centrale"),
        SiteSector("Secteur Est - Cité Océan", "E2", "Côté plage & grand manguier"),
        SiteSector("Secteur Ouest - Campement B", "O3", "Proche du poste de secours"),
        SiteSector("Zone Accueil Pèlerins", "ZA", "Entrée principale Sèmè-Podji")
    )

    val vendors = listOf(
        Vendor(
            id = "vendor_saveurs_seme",
            name = "Saveurs de Sèmè",
            type = VendorType.RESTAURANT,
            specialty = "Grillades côtières, tilapia braisé & alloco",
            zone = "Zone Restauration Nord",
            estimatedMinutes = 20,
            rating = 4.8,
            isOpen = true,
            sampleOfferings = "Tilapia braisé, Poulet grillé, Attiéké",
            imageRes = R.drawable.img_food_grill
        ),
        Vendor(
            id = "vendor_marmite_royale",
            name = "La Marmite du Littoral",
            type = VendorType.RESTAURANT,
            specialty = "Riz au gras, sauces béninoises & plats chauds",
            zone = "Allée Marchande Est",
            estimatedMinutes = 25,
            rating = 4.7,
            isOpen = true,
            sampleOfferings = "Riz au gras bœuf, Amiwo poulet, Igname",
            imageRes = R.drawable.img_food_rice
        ),
        Vendor(
            id = "vendor_oasis_fraicheur",
            name = "Oasis Fraîcheur",
            type = VendorType.INDEPENDENT,
            specialty = "Jus naturels glacés, bissap, gingembre & eau",
            zone = "Kiosque Esplanade",
            estimatedMinutes = 12,
            rating = 4.9,
            isOpen = true,
            sampleOfferings = "Bissap menthe, Jus de gingembre, Packs d'eau",
            imageRes = R.drawable.img_drinks_fresh
        ),
        Vendor(
            id = "vendor_pause_matinale",
            name = "Pause Matinale Chez Adjovi",
            type = VendorType.INDEPENDENT,
            specialty = "Bouillie chaude, beignets, café & kits pèlerin",
            zone = "Secteur Ouest - Stand 12",
            estimatedMinutes = 15,
            rating = 4.6,
            isOpen = true,
            sampleOfferings = "Akassa, Beignets yovo-doko, Kit hygiène",
            imageRes = R.drawable.img_hero_seme
        ),
        Vendor(
            id = "vendor_comptoir_cotier",
            name = "Comptoir Côtier",
            type = VendorType.RESTAURANT,
            specialty = "Brochettes, poisson frit & accompagnements rapides",
            zone = "Zone Restauration Sud",
            estimatedMinutes = 28,
            rating = 4.5,
            isOpen = false,
            sampleOfferings = "Brochettes bœuf, Frites d'igname, Jus d'ananas",
            imageRes = R.drawable.img_food_grill
        )
    )

    val products = listOf(
        Product(
            id = "prod_tilapia_alloco",
            vendorId = "vendor_saveurs_seme",
            vendorName = "Saveurs de Sèmè",
            name = "Tilapia braisé & Alloco",
            description = "Tilapia frais grillé au feu doux, servi avec bananes plantains dorées, piment rouge maison et salade croquante. Tarif fictif d'exemple.",
            priceFcfa = 2500L,
            category = ProductCategory.MEALS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 18,
            isPopular = true,
            imageRes = R.drawable.img_food_grill
        ),
        Product(
            id = "prod_poulet_attieke",
            vendorId = "vendor_saveurs_seme",
            vendorName = "Saveurs de Sèmè",
            name = "Poulet braisé & Attiéké",
            description = "Cuisse de poulet marinée aux épices locales avec semoule de manioc tiède, tomates fraîches et oignons émincés. Tarif fictif d'exemple.",
            priceFcfa = 2200L,
            category = ProductCategory.MEALS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 16,
            isPopular = true,
            imageRes = R.drawable.img_food_grill
        ),
        Product(
            id = "prod_riz_gras_boeuf",
            vendorId = "vendor_marmite_royale",
            vendorName = "La Marmite du Littoral",
            name = "Riz au gras & Bœuf mijoté",
            description = "Riz parfumé mijoté à la tomate et aux herbes avec morceaux de bœuf tendres et légumes vapeur. Tarif fictif d'exemple.",
            priceFcfa = 1800L,
            category = ProductCategory.MEALS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 15,
            isPopular = true,
            imageRes = R.drawable.img_food_rice
        ),
        Product(
            id = "prod_amiwo_poulet",
            vendorId = "vendor_marmite_royale",
            vendorName = "La Marmite du Littoral",
            name = "Amiwo traditionnel au poulet",
            description = "Pâte rouge béninoise savoureuse accompagnée de poulet rôti et sauce tomate épicée. Tarif fictif d'exemple.",
            priceFcfa = 2000L,
            category = ProductCategory.MEALS,
            availability = ProductAvailability.LIMITED,
            prepMinutes = 20,
            isPopular = false,
            imageRes = R.drawable.img_food_rice
        ),
        Product(
            id = "prod_bissap_menthe",
            vendorId = "vendor_oasis_fraicheur",
            vendorName = "Oasis Fraîcheur",
            name = "Jus de Bissap glacé (500 ml)",
            description = "Infusion de fleurs d'hibiscus légèrement sucrée à la menthe fraîche, servie bien fraîche en bouteille scellée. Tarif fictif d'exemple.",
            priceFcfa = 500L,
            category = ProductCategory.DRINKS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 5,
            isPopular = true,
            imageRes = R.drawable.img_drinks_fresh
        ),
        Product(
            id = "prod_gingembre_citron",
            vendorId = "vendor_oasis_fraicheur",
            vendorName = "Oasis Fraîcheur",
            name = "Jus de Gingembre & Citron (500 ml)",
            description = "Boisson tonique au gingembre frais pressé et citron vert, idéale pour rester hydraté sous le soleil de Sèmè. Tarif fictif d'exemple.",
            priceFcfa = 600L,
            category = ProductCategory.DRINKS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 5,
            isPopular = true,
            imageRes = R.drawable.img_drinks_fresh
        ),
        Product(
            id = "prod_pack_eau",
            vendorId = "vendor_oasis_fraicheur",
            vendorName = "Oasis Fraîcheur",
            name = "Pack 6 bouteilles Eau minérale (1,5 L)",
            description = "Six grandes bouteilles d'eau minérale fraîche livrées directement jusqu'à votre tente. Tarif fictif d'exemple.",
            priceFcfa = 1500L,
            category = ProductCategory.DRINKS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 8,
            isPopular = false,
            imageRes = R.drawable.img_drinks_fresh
        ),
        Product(
            id = "prod_petit_dej_yovo",
            vendorId = "vendor_pause_matinale",
            vendorName = "Pause Matinale Chez Adjovi",
            name = "Box Petit-déjeuner & Beignets",
            description = "Bouillie chaude sucrée au lait ou café, accompagnée de 6 beignets chauds et arachides grillées. Tarif fictif d'exemple.",
            priceFcfa = 900L,
            category = ProductCategory.SNACKS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 10,
            isPopular = true,
            imageRes = R.drawable.img_hero_seme
        ),
        Product(
            id = "prod_kit_pelerin",
            vendorId = "vendor_pause_matinale",
            vendorName = "Pause Matinale Chez Adjovi",
            name = "Kit Essentiel Pèlerin",
            description = "Éventail pliant, mouchoirs doux, gel hydroalcoolique, lampe de poche compacte et lingettes rafraîchissantes. Tarif fictif d'exemple.",
            priceFcfa = 1700L,
            category = ProductCategory.ESSENTIALS,
            availability = ProductAvailability.AVAILABLE,
            prepMinutes = 8,
            isPopular = false,
            imageRes = R.drawable.img_hero_seme
        ),
        Product(
            id = "prod_brochettes_soya",
            vendorId = "vendor_comptoir_cotier",
            vendorName = "Comptoir Côtier",
            name = "Duo Brochettes & Igname frite",
            description = "Brochettes grillées aux épices soya avec bâtonnets d'igname croustillants. Actuellement en pause cuisine.",
            priceFcfa = 1600L,
            category = ProductCategory.MEALS,
            availability = ProductAvailability.UNAVAILABLE,
            prepMinutes = 22,
            isPopular = false,
            imageRes = R.drawable.img_food_grill
        )
    )

    fun formatFcfa(amount: Long): String {
        val digits = amount.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            sb.append(digits[i])
            count++
            if (count % 3 == 0 && i != 0) {
                sb.append(' ')
            }
        }
        return "${sb.reverse()} FCFA"
    }
}
