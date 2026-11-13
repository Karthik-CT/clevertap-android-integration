package com.project.integrationsdk.vertical

/**
 * Registry of demo verticals shown on [com.project.integrationsdk.ui.VerticalPickerActivity].
 * Every vertical is rendered inside ProductExperiencesNewActivity.
 *
 * To add a new vertical: create a class implementing [VerticalExperience], then add an entry
 * here with [experience] pointing at it. Entries with `experience = null` show a placeholder.
 */
enum class Vertical(
    val title: String,
    val tagline: String,
    val emoji: String,
    val colorHex: String,
    private val experience: ((ExperienceHost) -> VerticalExperience)?,
) {
    ECOMMERCE("E-Commerce", "Shop, cart and offers", "🛍️", "#E8590C", null),
    FINTECH("FinTech", "Banking and payments", "🏦", "#0B2A5B", { FinTechExperience(it) }),
    FOODTECH("FoodTech", "Order food and delivery", "🍔", "#E03131", null),
    RIDE_HAILING("Ride Hailing", "Book and track rides", "🚕", "#212529", { RideHailingExperience(it) }),
    TELECOM("Telecom", "Plans, data and recharge", "📶", "#5B4FCF", { TelecomExperience(it) });

    val hasOwnUi get() = experience != null

    fun createExperience(host: ExperienceHost): VerticalExperience =
        experience?.invoke(host) ?: PlaceholderExperience(host, this)

    companion object {
        const val EXTRA_VERTICAL = "extra_vertical"

        fun fromName(name: String?) = values().firstOrNull { it.name == name } ?: FINTECH
    }
}
