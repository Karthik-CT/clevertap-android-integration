package com.project.integrationsdk.vertical

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.variables.Var
import com.clevertap.android.sdk.variables.callbacks.VariablesChangedCallback
import com.project.integrationsdk.R
import com.project.integrationsdk.util.bindInboxUnreadCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PE_DEMO = "PE_DEMO"

// ───────────────────────────── Experience ─────────────────────────────

/**
 * "DEMO BANK" home screen driven entirely by CleverTap Product Experience variables
 * (same schema as the PE Remote Config / A-B test prototype).
 * The UI reads ONLY from [UiState]; it never touches CleverTap variables directly.
 */
class FinTechExperience(private val host: ExperienceHost) : VerticalExperience {

    private val ctx: Context get() = host.context
    private val cleverTap: CleverTapAPI? = host.cleverTap
    private val vars = UiVariables(cleverTap, host.isDebug)
    private var pal: Palette
    private var state: UiState = vars.state.value
    private var lastRendered: UiState? = null

    private var tab = TAB_HOME
    private var arabic = false
    private var balanceVisible: Boolean? = null // null = follow home.balance_visible_default
    private var notifBadge: TextView? = null

    init { pal = Palette.from(state) }

    override val updates: StateFlow<Any> get() = vars.state

    override fun setup() {
        val items = listOf(
            Triple(TAB_HOME, "home", R.drawable.ic_nbk_home),
            Triple(TAB_ACCOUNTS, "accounts", R.drawable.ic_nbk_payments),
            Triple(TAB_TRANSFER, "transfer", R.drawable.ic_nbk_transfer),
            Triple(TAB_OFFERS, "offers", R.drawable.ic_nbk_apply),
        )
        items.forEachIndexed { i, (id, key, icon) ->
            host.bottomNav.menu.add(Menu.NONE, id, i, T(key)).setIcon(icon)
        }
        host.bottomNav.setOnItemSelectedListener {
            tab = it.itemId
            render(state, force = true)
            true
        }
        host.bottomNav.selectedItemId = TAB_HOME
    }

    override fun fetch(onDone: (Boolean) -> Unit) = vars.fetch(onDone)

    override fun updateInboxBadge() { notifBadge?.bindInboxUnreadCount(cleverTap) }

    override fun render(force: Boolean) = render(vars.state.value, force)

    // ── Render from UiState ──

    private fun render(s: UiState, force: Boolean = false) {
        val previous = lastRendered
        if (!force && previous == s) return
        state = s
        pal = Palette.from(s)

        host.root.setBackgroundColor(pal.bg)
        host.root.layoutDirection = if (arabic) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        host.swipeRefresh.setColorSchemeColors(pal.primary)

        // Bottom nav: Offers tab can be switched off remotely
        val bottomNav = host.bottomNav
        bottomNav.visibility = View.VISIBLE
        bottomNav.setBackgroundColor(pal.card)
        val active = if (s.darkMode) pal.accent else pal.primary
        val tint = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(active, pal.muted)
        )
        bottomNav.itemIconTintList = tint
        bottomNav.itemTextColor = tint
        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)
        listOf(TAB_HOME to "home", TAB_ACCOUNTS to "accounts", TAB_TRANSFER to "transfer", TAB_OFFERS to "offers")
            .forEach { (id, key) -> bottomNav.menu.findItem(id)?.title = T(key) }
        bottomNav.menu.findItem(TAB_OFFERS)?.isVisible = s.offersTabVisible
        if (tab == TAB_OFFERS && !s.offersTabVisible) {
            tab = TAB_HOME
            bottomNav.selectedItemId = TAB_HOME
        }

        val content = host.content
        content.removeAllViews()
        notifBadge = null
        when (tab) {
            TAB_ACCOUNTS -> buildAccounts(content)
            TAB_TRANSFER -> buildTransfer(content, s)
            TAB_OFFERS -> buildOffers(content, s)
            else -> buildHome(content, s)
        }
        updateInboxBadge()

        if (previous != null && previous != s) {
            Log.d(PE_DEMO, "UI re-rendered with new values")
            host.showMessage("Variables updated")
        }
        lastRendered = s
    }

    // ── Home ──

    private fun buildHome(root: LinearLayout, s: UiState) {
        root.addView(homeHeader())
        root.addView(balanceCard(s), lp(topDp = -40))
        root.addView(sectionTitle(T("qa")))
        root.addView(quickActions(s))
        if (s.heroVisible) root.addView(hero(s))
        if (s.loanVisible) root.addView(loanCard(s))
        if (s.offersVisible) {
            root.addView(sectionTitle(T("off"), T("seeall")))
            root.addView(offersStrip(s))
        }
        root.addView(sectionTitle(T("rec"), T("seeall")))
        root.addView(recentActivity())
        if (host.isDebug) root.addView(TextView(ctx).apply {
            text = "↻ fetchVariables()"
            textSize = 12f
            setTextColor(pal.muted)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(16))
            setOnClickListener { host.refreshVariables("debug_button") }
        })
    }

    private fun homeHeader(): View {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(pal.primary)
            setPaddingRelative(dp(16), host.statusTop + dp(10), dp(16), dp(54))
        }
        val names = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        names.addView(label(T("gm"), 12f, Color.WHITE).apply { alpha = 0.8f })
        names.addView(label(T("name"), 16f, Color.WHITE, bold = true))
        row.addView(names, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        row.addView(label("DEMO BANK", 13f, pal.accent, bold = true).apply {
            setPadding(dp(6), dp(2), dp(6), dp(2))
            background = rounded(Color.TRANSPARENT, dp(6).toFloat(), pal.accent, 1.5f)
        })
        row.addView(label(if (arabic) "EN" else "عربي", 12f, Color.WHITE, bold = true).apply {
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnClickListener { arabic = !arabic; render(state, force = true) }
        })

        val bell = FrameLayout(ctx)
        bell.addView(ImageView(ctx).apply {
            setImageResource(R.drawable.bell_icon)
            setColorFilter(Color.WHITE)
            setPadding(dp(7), dp(7), dp(7), dp(7))
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0x26FFFFFF) }
            setOnClickListener { demoOnly() }
        }, FrameLayout.LayoutParams(dp(32), dp(32)))
        notifBadge = TextView(ctx).apply {
            setBackgroundResource(R.drawable.badge_count_bg)
            gravity = Gravity.CENTER
            minWidth = dp(16); minHeight = dp(16)
            setPadding(dp(4), 0, dp(4), 0)
            textSize = 10f
            setTextColor(Color.WHITE)
            visibility = View.GONE
        }
        bell.addView(notifBadge, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END))
        row.addView(bell, LinearLayout.LayoutParams(dp(34), dp(34)))
        return row
    }

    private fun balanceCard(s: UiState): View {
        val shown = balanceVisible ?: s.balanceVisibleDefault
        val card = card(padding = 14)
        val top = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(label(T("tot"), 11f, pal.muted), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(TextView(ctx).apply {
            text = if (shown) "👁" else "🙈"
            textSize = 14f
            setPadding(dp(8), 0, 0, 0)
            setOnClickListener { balanceVisible = !shown; render(state, force = true) }
        })
        card.addView(top)
        val amt = if (shown) SpannableStringBuilder("KWD 12,458.750").apply {
            setSpan(RelativeSizeSpan(0.5f), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(0.5f), length - 4, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else "KWD ••••••"
        card.addView(label(amt, 26f, pal.text, bold = true, topDp = 4))
        val accts = LinearLayout(ctx).apply { setPadding(0, dp(4), 0, 0) }
        accts.addView(label("Current · ••4821", 11f, pal.muted), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        accts.addView(label("Savings · ••9907", 11f, pal.muted))
        card.addView(accts)
        card.elevation = dp(6).toFloat()
        return card
    }

    private fun quickActions(s: UiState): View {
        val order = s.actionsOrder.split(",").map { it.trim() }.filter { it in ACTION_ICONS }
        val cols = s.actionsColumns.toIntOrNull()?.coerceIn(1, 6) ?: 4
        val grid = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        order.chunked(cols).forEach { chunk ->
            val row = LinearLayout(ctx)
            repeat(cols) { i ->
                val key = chunk.getOrNull(i)
                row.addView(if (key != null) actionCell(key) else View(ctx),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = dp(4); marginEnd = dp(4); bottomMargin = dp(8)
                    })
            }
            grid.addView(row)
        }
        return LinearLayout(ctx).apply {
            setPaddingRelative(dp(10), 0, dp(10), dp(4))
            addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun actionCell(key: String): View {
        val cell = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(10), dp(4), dp(10))
            background = rounded(pal.card, pal.radius, pal.line)
            setOnClickListener { demoOnly() }
        }
        val tintColor = if (state.darkMode) pal.accent else pal.primary
        cell.addView(TextView(ctx).apply {
            text = ACTION_ICONS.getValue(key)
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(tintColor)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ColorUtils.setAlphaComponent(tintColor, 0x2A))
            }
        }, LinearLayout.LayoutParams(dp(34), dp(34)))
        cell.addView(label(T(key), 11f, pal.text, bold = true, topDp = 5).apply { gravity = Gravity.CENTER })
        return cell
    }

    private fun hero(s: UiState): View {
        val style = s.heroStyle
        val fg = when (style) { "solid" -> Color.parseColor("#1B1500"); "outline" -> pal.text; else -> Color.WHITE }
        val bg: GradientDrawable = when (style) {
            "solid" -> rounded(pal.accent, pal.radius)
            "outline" -> rounded(pal.card, pal.radius, pal.accent, 2f)
            else -> GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(pal.primary, ColorUtils.blendARGB(pal.primary, Color.parseColor("#3B82F6"), 0.45f))
            ).apply { cornerRadius = pal.radius }
        }
        val frame = FrameLayout(ctx).apply { background = bg; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        col.addView(label(s.heroTitle, 16f, fg, bold = true))
        col.addView(label(s.heroSubtitle, 12f, fg, topDp = 4))
        col.addView(label(s.heroCta, 12f, if (style == "solid") Color.WHITE else Color.parseColor("#1B1500"), bold = true).apply {
            setPadding(dp(14), dp(7), dp(14), dp(7))
            background = rounded(if (style == "solid") pal.primary else pal.accent, dp(999).toFloat())
            setOnClickListener { demoOnly() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })
        frame.addView(col, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = dp(64)
        })
        frame.addView(TextView(ctx).apply { text = "🪙"; textSize = 52f },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL or Gravity.END))
        return frame.also { it.layoutParams = lp(horizontalDp = 14, bottomDp = 12) }
    }

    private fun loanCard(s: UiState): View {
        val card = card(padding = 14).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val pct = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(ColorUtils.setAlphaComponent(pal.accent, 0x38), dp(12).toFloat())
            minimumWidth = dp(64)
        }
        pct.addView(label("%.1f%%".format(s.loanRate), 20f, if (s.darkMode) pal.accent else pal.primary, bold = true))
        pct.addView(label(T("loanS"), 9f, pal.muted))
        card.addView(pct)
        val txt = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; setPaddingRelative(dp(12), 0, 0, 0) }
        txt.addView(label(T("loanT"), 13f, pal.text, bold = true))
        txt.addView(label("Pre-approved offers in minutes", 11f, pal.muted, topDp = 2))
        txt.addView(label("${s.loanCta} ›", 11f, if (s.darkMode) pal.accent else pal.primary, bold = true, topDp = 6)
            .apply { setOnClickListener { demoOnly() } })
        card.addView(txt)
        return card
    }

    private fun offersStrip(s: UiState): View {
        val row = LinearLayout(ctx).apply { setPaddingRelative(dp(14), 0, dp(14), dp(12)) }
        listOf("Dining" to "Selected restaurants", "Travel" to "Flights and hotels", "Fuel" to "Weekend cashback")
            .forEach { (n, m) ->
                val c = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = rounded(pal.card, pal.radius, pal.line)
                }
                c.addView(label("${s.offersDiscount}%", 22f, pal.accent, bold = true))
                c.addView(label(n, 12f, pal.text, bold = true))
                c.addView(label(m, 10f, pal.muted))
                row.addView(c, LinearLayout.LayoutParams(dp(130), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(10) })
            }
        return HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; addView(row) }
    }

    private fun recentActivity(): View {
        val card = card(padding = 14)
        val rows = listOf(
            Triple("Coffee House", "Today · Card ••1102", "−2.350"),
            Triple("Salary credit", "Yesterday", "+1,850.000"),
            Triple("Grocery Mart", "Oct 22 · Card ••1102", "−18.725"),
        )
        rows.forEachIndexed { i, (n, sub, amt) ->
            val r = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
            val l = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            l.addView(label(n, 12f, pal.text))
            l.addView(label(sub, 10f, pal.muted))
            r.addView(l, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(label(amt, 12f, if (amt.startsWith("+")) Color.parseColor("#16A34A") else Color.parseColor("#DC2626"), bold = true))
            card.addView(r)
            if (i < rows.lastIndex) card.addView(View(ctx).apply { setBackgroundColor(pal.line) },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))
        }
        return card
    }

    // ── Other tabs ──

    private fun pageHead(title: String): View = label(title, 17f, Color.WHITE, bold = true).apply {
        setBackgroundColor(pal.primary)
        setPaddingRelative(dp(16), host.statusTop + dp(10), dp(16), dp(18))
    }

    private fun buildAccounts(root: LinearLayout) {
        root.addView(pageHead(T("accounts")))
        listOf(Triple("Current", "••4821", "12,458.750"), Triple("Savings", "••9907", "8,300.000"),
            Triple("Rewards card", "••1102", "−312.400")).forEach { (n, num, bal) ->
            val c = card(padding = 14).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val l = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            l.addView(label(n, 14f, pal.text, bold = true))
            l.addView(label(num, 11f, pal.muted))
            c.addView(l, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            c.addView(label("KWD $bal", 14f, pal.text, bold = true))
            root.addView(c, lp(topDp = if (n == "Current") 12 else 0))
        }
    }

    private fun buildTransfer(root: LinearLayout, s: UiState) {
        root.addView(pageHead(T("transfer")))
        val c = card(padding = 14)
        c.addView(field(T("from"), "Current · ••4821   KWD 12,458.750"))
        c.addView(field(T("to"), "Savings · ••9907"))
        c.addView(field(T("amt"), "KWD 250.000"))
        c.addView(label("ℹ ${s.transferFeeNote}", 11f, pal.text).apply {
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(ColorUtils.setAlphaComponent(pal.accent, 0x2E), dp(10).toFloat())
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) })
        if (s.transferFxVisible) {
            val chips = LinearLayout(ctx)
            listOf("USD 0.306", "EUR 0.281", "GBP 0.238", "AED 1.124").forEach {
                chips.addView(label(it, 11f, pal.text).apply {
                    setPadding(dp(9), dp(5), dp(9), dp(5))
                    background = rounded(Color.TRANSPARENT, dp(999).toFloat(), pal.line)
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(6) })
            }
            c.addView(HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; addView(chips) })
        }
        c.addView(label(s.transferCta, 14f, if (s.darkMode) Color.parseColor("#1B1500") else Color.WHITE, bold = true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(if (s.darkMode) pal.accent else pal.primary, pal.radius)
            setOnClickListener { demoOnly() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })
        root.addView(c, lp(topDp = 12))
    }

    private fun buildOffers(root: LinearLayout, s: UiState) {
        root.addView(pageHead(T("offers")))
        listOf("Dining", "Travel", "Fuel", "Electronics").forEachIndexed { i, n ->
            val c = card(padding = 14).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val l = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            l.addView(label(n, 14f, pal.text, bold = true))
            l.addView(label("Valid this month · demo offer", 11f, pal.muted))
            c.addView(l, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            c.addView(label("${s.offersDiscount}%", 22f, pal.accent, bold = true))
            root.addView(c, lp(topDp = if (i == 0) 12 else 0))
        }
    }

    private fun field(caption: String, value: String): View = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        addView(label(caption, 10f, pal.muted))
        addView(label(value, 13f, pal.text).apply {
            setPadding(dp(10), dp(9), dp(10), dp(9))
            background = rounded(pal.bg, dp(10).toFloat(), pal.line)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(3) })
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { bottomMargin = dp(10) }
    }

    // ── View helpers ──

    private fun T(key: String) = (if (arabic) I18N_AR else I18N_EN).getValue(key)

    private fun card(padding: Int) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
        background = rounded(pal.card, pal.radius, pal.line)
        layoutParams = lp()
    }

    private fun sectionTitle(title: String, trailing: String? = null) = LinearLayout(ctx).apply {
        setPaddingRelative(dp(16), dp(6), dp(16), dp(8))
        addView(label(title, 13f, pal.text, bold = true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        trailing?.let { addView(label(it, 11f, pal.muted)) }
    }

    private fun label(text: CharSequence, sp: Float, color: Int, bold: Boolean = false, topDp: Int = 0) =
        TextView(ctx).apply {
            this.text = text
            textSize = sp
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            if (topDp != 0) setPadding(0, dp(topDp), 0, 0)
        }

    private fun lp(topDp: Int = 0, horizontalDp: Int = 14, bottomDp: Int = 12) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(horizontalDp), dp(topDp), dp(horizontalDp), dp(bottomDp))
        }

    private fun demoOnly() = Toast.makeText(ctx, "Demo only", Toast.LENGTH_SHORT).show()
    private fun dp(v: Int) = (v * ctx.resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Float, strokeColor: Int? = null, strokeDp: Float = 1f) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            strokeColor?.let { setStroke((strokeDp * ctx.resources.displayMetrics.density).toInt(), it) }
        }

    private companion object {
        const val TAB_HOME = 1
        const val TAB_ACCOUNTS = 2
        const val TAB_TRANSFER = 3
        const val TAB_OFFERS = 4

        val ACTION_ICONS = mapOf("transfer" to "⇄", "pay" to "▣", "cards" to "▭", "topup" to "＋")

        val I18N_EN = mapOf(
            "home" to "Home", "accounts" to "Accounts", "transfer" to "Transfer", "offers" to "Offers",
            "pay" to "Pay", "cards" to "Cards", "topup" to "Top-up",
            "gm" to "Good morning", "tot" to "Total balance", "qa" to "Quick actions", "rec" to "Recent activity",
            "seeall" to "See all", "off" to "Offers for you", "from" to "From", "to" to "To", "amt" to "Amount",
            "loanT" to "Personal finance", "loanS" to "Starting rate p.a.", "name" to "Fatima",
        )
        val I18N_AR = mapOf(
            "home" to "الرئيسية", "accounts" to "الحسابات", "transfer" to "تحويل", "offers" to "العروض",
            "pay" to "دفع", "cards" to "البطاقات", "topup" to "شحن",
            "gm" to "صباح الخير", "tot" to "إجمالي الرصيد", "qa" to "إجراءات سريعة", "rec" to "آخر العمليات",
            "seeall" to "عرض الكل", "off" to "عروض لك", "from" to "من", "to" to "إلى", "amt" to "المبلغ",
            "loanT" to "التمويل الشخصي", "loanS" to "يبدأ من (سنوياً)", "name" to "فاطمة",
        )
    }
}

// ───────────────────────────── Theme ─────────────────────────────

/** Resolved colours / radius. theme.* variables drive primary, accent, radius and dark mode. */
private class Palette(
    val primary: Int, val accent: Int, val bg: Int, val card: Int,
    val text: Int, val muted: Int, val line: Int, val radius: Float,
) {
    companion object {
        private fun parse(value: String, fallback: String) =
            try { Color.parseColor(value.trim()) } catch (e: IllegalArgumentException) { Color.parseColor(fallback) }

        fun from(s: UiState): Palette {
            val density = android.content.res.Resources.getSystem().displayMetrics.density
            val dark = s.darkMode
            return Palette(
                primary = parse(s.primaryColor, UiState.DEFAULT_PRIMARY),
                accent = parse(s.accentColor, UiState.DEFAULT_ACCENT),
                bg = Color.parseColor(if (dark) "#0D1426" else "#F3F5F9"),
                card = Color.parseColor(if (dark) "#17203A" else "#FFFFFF"),
                text = Color.parseColor(if (dark) "#EAF0FF" else "#13203D"),
                muted = Color.parseColor(if (dark) "#9FB0D6" else "#667391"),
                line = Color.parseColor(if (dark) "#26314F" else "#E3E8F1"),
                radius = s.cornerRadius * density,
            )
        }
    }
}

// ───────────────────────────── Remote config layer ─────────────────────────────

/** Immutable snapshot of every Product Experience variable the UI consumes. */
private data class UiState(
    val primaryColor: String = DEFAULT_PRIMARY,
    val accentColor: String = DEFAULT_ACCENT,
    val cornerRadius: Int = 16,
    val darkMode: Boolean = false,
    val balanceVisibleDefault: Boolean = true,
    val actionsOrder: String = "transfer,pay,cards,topup",
    val actionsColumns: String = "4",
    val heroVisible: Boolean = true,
    val heroStyle: String = "gradient", // gradient | solid | outline
    val heroTitle: String = "Earn 2x points on every swipe",
    val heroSubtitle: String = "Limited time for rewards cardholders",
    val heroCta: String = "Explore",
    val loanVisible: Boolean = true,
    val loanRate: Double = 4.5,
    val loanCta: String = "Check eligibility",
    val offersVisible: Boolean = true,
    val offersDiscount: Int = 20,
    val transferFeeNote: String = "Free transfers between your own accounts",
    val transferFxVisible: Boolean = true,
    val transferCta: String = "Continue",
    val offersTabVisible: Boolean = true,
) {
    companion object {
        const val DEFAULT_PRIMARY = "#0B2A5B"
        const val DEFAULT_ACCENT = "#C9A227"
    }
}

/**
 * Single source of truth for UI-driving CleverTap variables (SDK API:
 * defineVariable / syncVariables / fetchVariables / addVariablesChangedCallback).
 * Exposes [state] as a StateFlow; the UI never reads CleverTap directly.
 */
private class UiVariables(private val ct: CleverTapAPI?, debug: Boolean) {

    private val d = UiState()

    private fun <T : Any> def(name: String, default: T): Var<T>? {
        val v = ct?.defineVariable(name, default)
        Log.d(PE_DEMO, "defineVariable name=$name default=$default(${default.javaClass.simpleName}) -> " +
            if (v == null) "NULL (ct=${ct == null})" else "ok name=${v.name()} value=${v.value()}")
        return v
    }

    private val primaryColor = def("ui_theme.primary_color", d.primaryColor)
    private val accentColor = def("ui_theme.accent_color", d.accentColor)
    private val cornerRadius = def("ui_theme.corner_radius", d.cornerRadius)
    private val darkMode = def("ui_theme.dark_mode", d.darkMode)
    private val balanceVisibleDefault = def("home.balance_visible_default", d.balanceVisibleDefault)
    private val actionsOrder = def("home.actions.order", d.actionsOrder)
    private val actionsColumns = def("home.actions.columns", d.actionsColumns)
    private val heroVisible = def("home.hero.visible", d.heroVisible)
    private val heroStyle = def("home.hero.style", d.heroStyle)
    private val heroTitle = def("home.hero.title", d.heroTitle)
    private val heroSubtitle = def("home.hero.subtitle", d.heroSubtitle)
    private val heroCta = def("home.hero.cta_text", d.heroCta)
    private val loanVisible = def("home.loan.visible", d.loanVisible)
    private val loanRate = def("home.loan.rate", d.loanRate)
    private val loanCta = def("home.loan.cta_text", d.loanCta)
    private val offersVisible = def("home.offers.visible", d.offersVisible)
    private val offersDiscount = def("home.offers.discount_pct", d.offersDiscount)
    private val transferFeeNote = def("transfer.fee_note", d.transferFeeNote)
    private val transferFxVisible = def("transfer.fx_chips_visible", d.transferFxVisible)
    private val transferCta = def("transfer.cta_text", d.transferCta)
    private val offersTabVisible = def("nav.offers_tab_visible", d.offersTabVisible)

    private val _state = MutableStateFlow(d)
    val state: StateFlow<UiState> = _state

    init {
        Log.d(PE_DEMO, "UiVariables init: ct=${ct != null} debug=$debug accountId=${ct?.accountId} " +
            "cleverTapID=${ct?.cleverTapID} defined=21")
        publish("init")
        ct?.addVariablesChangedCallback(object : VariablesChangedCallback() {
            override fun variablesChanged() {
                Log.d(PE_DEMO, "variablesChanged callback fired")
                publish("variablesChanged")
            }
        })
        // syncVariables() only works for test profiles in debug builds (per CleverTap docs)
        if (debug) {
            Log.d(PE_DEMO, "calling syncVariables() (debug build)")
            ct?.syncVariables()
            Log.d(PE_DEMO, "syncVariables() returned (it is async; it does nothing unless this is a test profile)")
        }
    }

    fun fetch(onDone: (Boolean) -> Unit) {
        if (ct == null) { onDone(false); return }
        Log.d(PE_DEMO, "calling fetchVariables()")
        ct.fetchVariables { success ->
            Log.d(PE_DEMO, "fetchVariables callback success=$success")
            publish("fetchVariables")
            onDone(success)
        }
    }

    private fun <T : Any> Var<T>?.or(default: T): T = this?.value() ?: default

    // The dashboard may send 4 or 4.0 for a number; read it as Number so either works.
    private fun Var<*>?.num(default: Double): Double =
        try { (this?.value() as? Number)?.toDouble() ?: default } catch (e: Exception) { default }

    private fun publish(source: String) {
        val next = UiState(
            primaryColor = primaryColor.or(d.primaryColor), accentColor = accentColor.or(d.accentColor),
            cornerRadius = cornerRadius.num(d.cornerRadius.toDouble()).toInt().coerceIn(0, 28),
            darkMode = darkMode.or(d.darkMode),
            balanceVisibleDefault = balanceVisibleDefault.or(d.balanceVisibleDefault),
            actionsOrder = actionsOrder.or(d.actionsOrder), actionsColumns = actionsColumns.or(d.actionsColumns),
            heroVisible = heroVisible.or(d.heroVisible), heroStyle = heroStyle.or(d.heroStyle).lowercase(),
            heroTitle = heroTitle.or(d.heroTitle), heroSubtitle = heroSubtitle.or(d.heroSubtitle),
            heroCta = heroCta.or(d.heroCta),
            loanVisible = loanVisible.or(d.loanVisible), loanRate = loanRate.num(d.loanRate),
            loanCta = loanCta.or(d.loanCta),
            offersVisible = offersVisible.or(d.offersVisible),
            offersDiscount = offersDiscount.num(d.offersDiscount.toDouble()).toInt(),
            transferFeeNote = transferFeeNote.or(d.transferFeeNote),
            transferFxVisible = transferFxVisible.or(d.transferFxVisible),
            transferCta = transferCta.or(d.transferCta),
            offersTabVisible = offersTabVisible.or(d.offersTabVisible),
        )
        Log.d(PE_DEMO, "publish($source) changed=${next != _state.value} values=$next")
        _state.value = next
    }
}
