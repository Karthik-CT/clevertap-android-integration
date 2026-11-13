package com.project.integrationsdk.vertical

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
 * Telecom home screen ("Demo Telco"): usage cards, bill, benefits, offers and shop.
 * Key copy and numbers are driven by CleverTap Product Experience variables; everything
 * else is dummy data. The UI reads ONLY from [TelecomState].
 */
class TelecomExperience(private val host: ExperienceHost) : VerticalExperience {

    private val ctx: Context get() = host.context
    private val cleverTap: CleverTapAPI? = host.cleverTap
    private val vars = TelecomVariables(cleverTap, host.isDebug)
    private var state: TelecomState = vars.state.value
    private var pal: TelecomPalette = TelecomPalette.from(state)
    private var lastRendered: TelecomState? = null

    private var tab = TAB_HOME
    private var notifBadge: TextView? = null
    private var bottomBar: LinearLayout? = null
    private val tabViews = mutableMapOf<Int, Pair<TextView, TextView>>() // id -> (icon, label)

    override val updates: StateFlow<Any> get() = vars.state

    override fun setup() {
        host.bottomNav.visibility = View.GONE
        (host.root as ViewGroup).clipChildren = false
        (ctx as? Activity)?.window?.let {
            WindowCompat.getInsetsController(it, host.root).isAppearanceLightStatusBars = false
        }
        val bar = buildBottomBar()
        bottomBar = bar
        (host.root as ViewGroup).addView(bar)
        ViewCompat.setOnApplyWindowInsetsListener(bar) { v, insets ->
            v.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }
    }

    override fun fetch(onDone: (Boolean) -> Unit) = vars.fetch(onDone)

    override fun updateInboxBadge() { notifBadge?.bindInboxUnreadCount(cleverTap) }

    override fun render(force: Boolean) = render(vars.state.value, force)

    // ── Render from state ──

    private fun render(s: TelecomState, force: Boolean = false) {
        val previous = lastRendered
        if (!force && previous == s) return
        state = s
        pal = TelecomPalette.from(s)

        host.root.setBackgroundColor(pal.bg)
        host.swipeRefresh.setColorSchemeColors(pal.primary)
        styleBottomBar()

        val content = host.content
        content.removeAllViews()
        notifBadge = null
        when (tab) {
            TAB_SERVICES -> buildSimplePage(content, "Services", listOf("Manage plan", "Add-ons", "Roaming", "International calls", "Bill & payments"))
            TAB_HELP -> buildSimplePage(content, "Help", listOf("Chat with us", "Call support", "Store locator", "FAQs"))
            TAB_MORE -> buildSimplePage(content, "More", listOf("Profile", "Language", "Notifications", "About"))
            else -> buildHome(content, s)
        }
        updateInboxBadge()

        if (previous != null && previous != s) {
            Log.d(PE_DEMO, "Telecom UI re-rendered with new values")
            host.showMessage("Variables updated")
        }
        lastRendered = s
    }

    // ───────────── Home ─────────────

    private fun buildHome(root: LinearLayout, s: TelecomState) {
        root.addView(header(s))                                                       // 1 + 2
        if (s.usageVisible) root.addView(usageRow(s), lp(topDp = 8, horizontalDp = 0, bottomDp = 0)) // 3
        if (s.actionsVisible) root.addView(actionButtons(s), lp(topDp = 4))           // 4
        if (s.billVisible) root.addView(billCard(s))                                  // 5
        if (s.benefitsVisible) root.addView(benefits(s))                              // 6
        if (s.offersVisible) root.addView(specialOffers(s))                           // 7
        if (s.promoVisible) root.addView(promoTiles(s), lp(topDp = 16))               // 8
        if (s.shopVisible) root.addView(shopContainer(s))                             // 9
        if (s.linksVisible) root.addView(moreContainer(s), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = if (s.shopVisible) -dp(20) else dp(16) })            // 10
    }

    // 1 + 2. Header and account selector
    private fun header(s: TelecomState): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR, intArrayOf(pal.primary, pal.light)
            ).apply {
                val r = dp(28).toFloat()
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r)
            }
            setPaddingRelative(dp(16), host.statusTop + dp(14), dp(16), dp(22))
        }

        val top = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(logo(), LinearLayout.LayoutParams(dp(28), dp(30)).apply { marginEnd = dp(10) })
        top.addView(label(if (s.userName.isBlank()) "${s.greeting} 👋" else "${s.greeting} ${s.userName} 👋", 20f, Color.WHITE),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val bell = FrameLayout(ctx)
        bell.addView(ImageView(ctx).apply {
            setImageResource(R.drawable.bell_icon)
            setColorFilter(Color.WHITE)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            tap(this, "Notifications")
        }, FrameLayout.LayoutParams(dp(36), dp(36)))
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
        top.addView(bell, LinearLayout.LayoutParams(dp(38), dp(38)))
        box.addView(top)

        // Account selector
        val acct = LinearLayout(ctx).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(16), 0)
            background = rounded(Color.WHITE, pal.radius)
            elevation = dp(3).toFloat()
            tap(this, "Account selector")
        }
        acct.addView(label(s.accountBadge, 9f, pal.primary, bold = true).apply {
            gravity = Gravity.CENTER
            background = rounded(pal.tint, dp(10).toFloat())
        }, LinearLayout.LayoutParams(dp(38), dp(38)).apply { marginEnd = dp(12) })
        acct.addView(label(s.account, 16f, pal.text, bold = true))
        acct.addView(label(s.plan, 14f, pal.muted).apply { setPaddingRelative(dp(10), 0, 0, 0) },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        acct.addView(label("⌄", 24f, pal.primary, bold = true))
        box.addView(acct, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58))
            .apply { topMargin = dp(18) })
        return box
    }

    /** Generic "signal" mark: four ascending bars. */
    private fun logo(): View = LinearLayout(ctx).apply {
        gravity = Gravity.BOTTOM
        listOf(10, 16, 22, 28).forEachIndexed { i, h ->
            addView(View(ctx).apply {
                background = rounded(Color.WHITE, dp(2).toFloat())
                alpha = if (i == 3) 0.6f else 1f
            }, LinearLayout.LayoutParams(dp(5), dp(h)).apply { if (i < 3) marginEnd = dp(2) })
        }
    }

    // 3. Usage cards: horizontal carousel, every card the same size.
    // Which cards show, and in what order, comes from telecom.usage.order.
    private fun usageRow(s: TelecomState): View {
        val row = LinearLayout(ctx).apply {
            gravity = Gravity.CENTER_VERTICAL
            clipChildren = false
            clipToPadding = false
            setPaddingRelative(dp(16), dp(8), dp(16), dp(14))
        }
        val data = SpannableStringBuilder("${s.dataLeft} ${s.dataUnit}").apply {
            val split = s.dataLeft.length
            setSpan(AbsoluteSizeSpan(20, true), 0, split, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(pal.muted), split, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(AbsoluteSizeSpan(12, true), split, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val keys = s.usageOrder.split(",").map { it.trim().lowercase() }.filter { it in USAGE_KEYS }.distinct()
        keys.forEachIndexed { i, key ->
            val last = i == keys.lastIndex
            row.addView(when (key) {
                "international" -> usageCard("◔", s.intlTitle, s.intlDescription, false, last)
                "data" -> usageCard("⇅", s.dataTitle, data, true, last)
                "units" -> usageCard("✆", s.unitsTitle, s.unitsValue, true, last)
                "sms" -> usageCard("✉", s.smsTitle, s.smsValue, true, last)
                else -> usageCard("✈", s.roamingTitle, s.roamingValue, true, last)
            })
        }
        return HorizontalScrollView(ctx).apply {
            isHorizontalScrollBarEnabled = false
            clipChildren = false
            clipToPadding = false
            addView(row)
        }
    }

    private fun usageCard(icon: String, title: String, value: CharSequence, isValue: Boolean, last: Boolean) =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pal.tint, Color.WHITE))
                .apply { cornerRadius = pal.radius }
            elevation = dp(3).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(128), dp(150)).apply { if (!last) marginEnd = dp(10) }
            addView(iconChip(icon))
            addView(label(title, 13f, pal.text, bold = true, topDp = 10))
            addView(View(ctx), LinearLayout.LayoutParams(0, 0, 1f))
            val bottom = LinearLayout(ctx).apply { gravity = Gravity.BOTTOM }
            bottom.addView(
                if (isValue) label(value, 16f, pal.primary, bold = true) else label(value, 10f, pal.muted),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            bottom.addView(arrow())
            addView(bottom)
            tap(this, title)
        }

    private fun iconChip(symbol: String) = label(symbol, 16f, pal.primary, bold = true).apply {
        gravity = Gravity.CENTER
        background = rounded(ColorUtils.blendARGB(pal.tint, Color.WHITE, 0.2f), dp(10).toFloat())
        layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
    }

    private fun arrow() = label("↗", 18f, pal.primary, bold = true).apply {
        gravity = Gravity.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.END }
    }

    // 4. Outlined action buttons
    private fun actionButtons(s: TelecomState): View {
        val row = LinearLayout(ctx).apply { setPaddingRelative(dp(16), dp(4), dp(16), dp(4)) }
        listOf(s.manageLabel to "↗", s.addonsLabel to "+").forEachIndexed { i, (name, glyph) ->
            val btn = LinearLayout(ctx).apply {
                gravity = Gravity.CENTER
                background = rounded(Color.WHITE, pal.radius, pal.line, 1f)
                tap(this, name)
            }
            btn.addView(label(glyph, 13f, pal.primary, bold = true).apply {
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL; setColor(Color.TRANSPARENT); setStroke(dp(2), pal.primary)
                }
            }, LinearLayout.LayoutParams(dp(24), dp(24)).apply { marginEnd = dp(10) })
            btn.addView(label(name, 14f, pal.text).apply { letterSpacing = 0.05f })
            row.addView(btn, LinearLayout.LayoutParams(0, dp(48), 1f).apply { if (i == 0) marginEnd = dp(12) })
        }
        return row
    }

    // 5. Bill card
    private fun billCard(s: TelecomState): View {
        val wrap = LinearLayout(ctx).apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(pal.chipGrey, dp(24).toFloat())
        }
        val card = LinearLayout(ctx).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(Color.WHITE, pal.radius + dp(4))
        }
        val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        col.addView(label(s.billTitle, 15f, pal.muted))
        col.addView(label(s.billAmount, 22f, pal.text, bold = true, topDp = 2))
        col.addView(label(s.billSubtitle, 14f, pal.muted, topDp = 2))
        card.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(label(s.billCta, 13f, pal.primary, bold = true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = rounded(Color.WHITE, dp(999).toFloat(), pal.primary, 1.5f)
            tap(this, s.billCta)
        })
        wrap.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return LinearLayout(ctx).apply {
            addView(wrap, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            layoutParams = lp(topDp = 12, horizontalDp = 16, bottomDp = 4)
        }
    }

    // 6. Activate free benefits
    private fun benefits(s: TelecomState): View {
        val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        col.addView(label(s.benefitsTitle, 20f, pal.text).apply { setPaddingRelative(dp(16), dp(20), dp(16), dp(12)) })
        val row = LinearLayout(ctx).apply { setPaddingRelative(dp(16), 0, dp(16), dp(4)) }

        row.addView(benefitCard(Color.BLACK) { c ->
            c.addView(label(s.benefit1Title, 22f, Color.parseColor("#F5C518"), bold = true).apply { letterSpacing = 0.04f })
            if (s.benefit1Subtitle.isNotBlank()) c.addView(label(s.benefit1Subtitle, 11f, Color.WHITE, bold = true))
        }, benefitParams())
        row.addView(benefitCard(Color.parseColor("#FFBF14")) { c ->
            c.addView(label("▶  ${s.benefit2Title}", 40f, Color.WHITE, bold = true))
        }, benefitParams())
        row.addView(benefitCard(Color.BLACK) { c ->
            c.addView(FrameLayout(ctx).apply {
                listOf(64 to 6, 44 to 5, 24 to 4).forEach { (size, stroke) ->
                    addView(View(ctx).apply {
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL; setColor(Color.TRANSPARENT)
                            setStroke(dp(stroke), Color.parseColor("#1CC6C1"))
                        }
                    }, FrameLayout.LayoutParams(dp(size), dp(size), Gravity.CENTER))
                }
            }, LinearLayout.LayoutParams(dp(70), dp(70)))
        }, benefitParams(last = true))

        col.addView(HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; addView(row) })
        return col
    }

    private fun benefitParams(last: Boolean = false) =
        LinearLayout.LayoutParams(dp(250), dp(130)).apply { if (!last) marginEnd = dp(12) }

    private fun benefitCard(bg: Int, fill: (LinearLayout) -> Unit) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        background = rounded(bg, dp(14).toFloat())
        elevation = dp(2).toFloat()
        fill(this)
        tap(this, "Free benefit")
    }

    // 7. Special offers
    private fun specialOffers(s: TelecomState): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pal.offersBg)
            setPaddingRelative(0, dp(16), 0, dp(16))
        }
        val head = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL; setPaddingRelative(dp(16), 0, dp(16), dp(10)) }
        head.addView(label(s.offersTitle, 16f, pal.muted, bold = true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(label(s.offersViewAll, 13f, pal.primary).apply {
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            tap(this, s.offersViewAll)
        })
        head.addView(label("  →", 16f, pal.text, bold = true))
        box.addView(head)

        val offers = listOf(
            Offer(s.offer1Color, s.offer1Title, s.offer1Body, s.offer1Badge),
            Offer(s.offer2Color, s.offer2Title, s.offer2Body, s.offer2Badge),
            Offer(s.offer3Color, s.offer3Title, s.offer3Body, s.offer3Badge),
            Offer(s.offer4Color, s.offer4Title, s.offer4Body, s.offer4Badge),
        )
        val row = LinearLayout(ctx).apply { clipChildren = false; setPaddingRelative(dp(16), 0, dp(16), 0) }
        offers.forEachIndexed { i, o ->
            row.addView(offerCard(o, s), LinearLayout.LayoutParams(dp(280), ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { if (i < offers.lastIndex) marginEnd = dp(12) })
        }
        val dots = LinearLayout(ctx).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) }
        fun paintDots(active: Int) {
            dots.removeAllViews()
            offers.indices.forEach { i ->
                val size = if (i == active) 10 else 8
                dots.addView(View(ctx).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(if (i == active) Color.parseColor("#0B5A8A") else pal.dotIdle)
                    }
                }, LinearLayout.LayoutParams(dp(size), dp(size)).apply { marginStart = dp(4); marginEnd = dp(4) })
            }
        }
        paintDots(0)
        val scroller = HorizontalScrollView(ctx).apply {
            isHorizontalScrollBarEnabled = false
            clipChildren = false
            addView(row)
            var shown = 0
            setOnScrollChangeListener { _, scrollX, _, _, _ ->
                val idx = Math.round(scrollX / dp(292).toFloat()).coerceIn(0, offers.lastIndex)
                if (idx != shown) { shown = idx; paintDots(idx) }
            }
        }
        box.addView(scroller)
        box.addView(dots)
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(box)
            layoutParams = lp(topDp = 16, horizontalDp = 0, bottomDp = 0)
        }
    }

    private class Offer(val colorHex: String, val title: String, val body: String, val badge: String)

    private fun offerCard(o: Offer, s: TelecomState): View {
        val wrap = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        wrap.addView(label(s.offersTabLabel, 13f, Color.WHITE, bold = true).apply {
            setPadding(dp(16), dp(7), dp(16), dp(9))
            background = GradientDrawable().apply {
                setColor(pal.primary)
                cornerRadii = floatArrayOf(dp(14).toFloat(), dp(14).toFloat(), dp(14).toFloat(), dp(14).toFloat(), 0f, 0f, 0f, 0f)
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(120) })

        val card = FrameLayout(ctx).apply {
            background = rounded(safeColor(o.colorHex, Color.parseColor("#88C7BB")), dp(24).toFloat())
            setPadding(dp(18), dp(18), dp(18), dp(18))
            tap(this, o.title)
        }
        val text = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        text.addView(label(o.title, 18f, pal.text, bold = true))
        text.addView(label(o.body, 14f, pal.text, topDp = 6))
        card.addView(text, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { marginEnd = dp(84) })
        // simple "phone" illustration
        card.addView(label(o.badge, 11f, Color.WHITE, bold = true).apply {
            gravity = Gravity.CENTER
            rotation = -10f
            background = rounded(pal.primary, dp(14).toFloat())
        }, FrameLayout.LayoutParams(dp(64), dp(104), Gravity.END or Gravity.CENTER_VERTICAL))
        card.addView(label("${s.offersCta}  →", 14f, pal.primary, bold = true),
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START))
        card.minimumHeight = dp(190)
        wrap.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(190)))
        return wrap
    }

    // 8. Gradient tiles
    private fun promoTiles(s: TelecomState): View {
        val row = LinearLayout(ctx).apply { setPaddingRelative(dp(16), 0, dp(16), 0) }
        fun tile(from: String, to: String, name: String, fill: (LinearLayout) -> Unit) = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor(from), Color.parseColor(to))).apply { cornerRadius = dp(20).toFloat() }
            fill(this)
            tap(this, name)
        }
        row.addView(tile(pal.dark.toHex(), pal.primary.toHex(), s.rechargeTitle) { c ->
            c.addView(label("📱", 22f, Color.WHITE))
            c.addView(View(ctx), LinearLayout.LayoutParams(0, 0, 1f))
            c.addView(label(s.rechargeTitle, 20f, Color.WHITE, bold = true))
        }, LinearLayout.LayoutParams(0, dp(150), 1f).apply { marginEnd = dp(12) })
        row.addView(tile("#EF4444", "#FF9800", s.rewardsTitle) { c ->
            val t = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
            t.addView(label("✦", 22f, Color.WHITE))
            t.addView(label(" ${s.rewardsTitle}", 20f, Color.WHITE, bold = true))
            c.addView(t)
            c.addView(View(ctx), LinearLayout.LayoutParams(0, 0, 1f))
            c.addView(SpannableStringBuilder("${s.rewardsPoints} ${s.rewardsPointsLabel}").apply {
                setSpan(AbsoluteSizeSpan(22, true), 0, s.rewardsPoints.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }.let { label(it, 16f, Color.WHITE, bold = true) })
        }, LinearLayout.LayoutParams(0, dp(150), 1f))
        return row
    }

    // 9. Shop container
    private fun shopContainer(s: TelecomState): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = topRounded(pal.slate, 32)
            setPadding(dp(16), dp(28), dp(16), dp(44))
        }
        box.addView(label(s.shopTitle, 24f, Color.WHITE, bold = true).apply { gravity = Gravity.CENTER })
        val row = LinearLayout(ctx).apply { setPadding(0, dp(20), 0, 0) }
        listOf("📶" to s.prepaidLabel, "📱" to s.postpaidLabel, "🎧" to s.devicesLabel, "🏠" to s.homeLabel).forEach { (icon, name) ->
            val item = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                tap(this, name)
            }
            item.addView(label(icon, 26f, Color.WHITE).apply {
                gravity = Gravity.CENTER
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(pal.primary) }
            }, LinearLayout.LayoutParams(dp(68), dp(68)))
            item.addView(label(name, 12f, Color.WHITE, topDp = 8).apply { gravity = Gravity.CENTER })
            row.addView(item, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        box.addView(row)
        return LinearLayout(ctx).apply {
            addView(box, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            layoutParams = lp(topDp = 24, horizontalDp = 0, bottomDp = 0)
        }
    }

    // 10. Looking for more
    private fun moreContainer(s: TelecomState): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = topRounded(pal.bg, 32)
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        box.addView(label(s.linksTitle, 20f, pal.muted, bold = true))
        box.addView(label(s.linksSubtitle, 16f, pal.muted, topDp = 4))
        s.linksItems.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEachIndexed { i, name ->
            if (i > 0) box.addView(View(ctx).apply { setBackgroundColor(pal.line) },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))
            val r = LinearLayout(ctx).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(16), 0, dp(16))
                tap(this, name)
            }
            r.addView(label(name, 15f, pal.text), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(label("›", 22f, pal.muted))
            box.addView(r)
        }
        return box
    }

    // ───────────── Other tabs ─────────────

    private fun buildSimplePage(root: LinearLayout, title: String, rows: List<String>) {
        root.addView(label(title, 22f, Color.WHITE, bold = true).apply {
            setPaddingRelative(dp(16), host.statusTop + dp(20), dp(16), dp(22))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(pal.primary, pal.light)).apply {
                val r = dp(28).toFloat()
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r)
            }
        })
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(4), dp(16), dp(4))
            background = rounded(Color.WHITE, dp(16).toFloat())
            layoutParams = lp(topDp = 4)
        }
        rows.forEachIndexed { i, name ->
            if (i > 0) card.addView(View(ctx).apply { setBackgroundColor(pal.line) },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))
            val r = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(16), 0, dp(16)); tap(this, name) }
            r.addView(label(name, 15f, pal.text), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(label("›", 22f, pal.muted))
            card.addView(r)
        }
        root.addView(card)
    }

    // 11. Bottom navigation (custom: centre gift button sits above the bar)
    private fun buildBottomBar(): LinearLayout {
        val bar = LinearLayout(ctx).apply {
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
            setBackgroundColor(Color.WHITE)
            elevation = dp(12).toFloat()
        }
        fun slot(id: Int, icon: String, name: String): View {
            val col = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setOnClickListener { tab = id; render(state, force = true); toast(tabViews.getValue(id).second.text.toString()) }
            }
            val i = label(icon, 22f, pal.muted).apply { gravity = Gravity.CENTER }
            val l = label(name, 11f, pal.muted).apply { gravity = Gravity.CENTER }
            tabViews[id] = i to l
            col.addView(i); col.addView(l)
            return col
        }
        bar.addView(slot(TAB_HOME, "⌂", "Home"), LinearLayout.LayoutParams(0, dp(64), 1f))
        bar.addView(slot(TAB_SERVICES, "▤", "Services"), LinearLayout.LayoutParams(0, dp(64), 1f))

        val gift = FrameLayout(ctx).apply { clipChildren = false; setOnClickListener { toast("Rewards") } }
        gift.addView(label("🎁", 28f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(pal.primary) }
            elevation = dp(6).toFloat()
        }, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER_HORIZONTAL).apply { topMargin = -dp(22) })
        gift.addView(label("✦", 12f, pal.primary), FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
            .apply { topMargin = -dp(26); marginStart = -dp(48) })
        gift.addView(label("✦", 9f, pal.primary), FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
            .apply { topMargin = dp(18); marginStart = dp(46) })
        bar.addView(gift, LinearLayout.LayoutParams(0, dp(64), 1f))

        bar.addView(slot(TAB_HELP, "?", "Help"), LinearLayout.LayoutParams(0, dp(64), 1f))
        bar.addView(slot(TAB_MORE, "☰", "More"), LinearLayout.LayoutParams(0, dp(64), 1f))
        bar.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return bar
    }

    private fun styleBottomBar() {
        val names = mapOf(TAB_HOME to state.navHome, TAB_SERVICES to state.navServices,
            TAB_HELP to state.navHelp, TAB_MORE to state.navMore)
        tabViews.forEach { (id, v) ->
            v.second.text = names.getValue(id)
            val c = if (id == tab) pal.primary else pal.muted
            v.first.setTextColor(c)
            v.second.setTextColor(c)
        }
    }

    // ───────────── View helpers ─────────────

    private fun tap(v: View, name: String) = v.setOnClickListener { toast(name) }
    private fun toast(name: String) = Toast.makeText(ctx, name, Toast.LENGTH_SHORT).show()

    private fun label(text: CharSequence, sp: Float, color: Int, bold: Boolean = false, topDp: Int = 0) =
        TextView(ctx).apply {
            this.text = text
            textSize = sp
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            if (topDp != 0) setPadding(0, dp(topDp), 0, 0)
        }

    private fun lp(topDp: Int = 0, horizontalDp: Int = 16, bottomDp: Int = 12) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(horizontalDp), dp(topDp), dp(horizontalDp), dp(bottomDp))
        }

    private fun safeColor(hex: String, fallback: Int) =
        try { Color.parseColor(hex.trim()) } catch (e: IllegalArgumentException) { fallback }

    private fun dp(v: Int) = (v * ctx.resources.displayMetrics.density).toInt()
    private fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this)

    private fun rounded(color: Int, radius: Float, strokeColor: Int? = null, strokeDp: Float = 1f) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            strokeColor?.let { setStroke((strokeDp * ctx.resources.displayMetrics.density).toInt(), it) }
        }

    private fun topRounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        val r = dp(radiusDp).toFloat()
        cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
    }

    private companion object {
        const val TAB_HOME = 1
        const val TAB_SERVICES = 2
        const val TAB_HELP = 3
        const val TAB_MORE = 4

        val USAGE_KEYS = setOf("international", "data", "units", "sms", "roaming")
    }
}

// ───────────────────────────── Theme ─────────────────────────────

/** Resolved colours. telecom.theme.primary_color drives the brand colour everywhere. */
private class TelecomPalette(
    val primary: Int, val dark: Int, val light: Int, val tint: Int,
    val bg: Int, val text: Int, val muted: Int, val line: Int,
    val chipGrey: Int, val offersBg: Int, val dotIdle: Int, val slate: Int, val radius: Float,
) {
    companion object {
        fun from(s: TelecomState): TelecomPalette {
            val primary = try { Color.parseColor(s.primaryColor.trim()) } catch (e: IllegalArgumentException) {
                Color.parseColor(TelecomState.DEFAULT_PRIMARY)
            }
            return TelecomPalette(
                primary = primary,
                dark = ColorUtils.blendARGB(primary, Color.BLACK, 0.3f),
                light = ColorUtils.blendARGB(primary, Color.WHITE, 0.28f),
                tint = ColorUtils.blendARGB(primary, Color.WHITE, 0.86f),
                bg = Color.parseColor("#F0F0F4"),          // @color/screen_background
                text = Color.parseColor("#1A1A2E"),        // @color/text_primary
                muted = Color.parseColor("#666666"),       // @color/text_secondary
                line = Color.parseColor("#E5E5E5"),        // @color/divider
                chipGrey = Color.parseColor("#E6E4F5"),
                offersBg = ColorUtils.blendARGB(primary, Color.WHITE, 0.92f),
                dotIdle = ColorUtils.blendARGB(primary, Color.WHITE, 0.78f),
                slate = ColorUtils.blendARGB(primary, Color.BLACK, 0.68f),
                radius = s.cardRadius * android.content.res.Resources.getSystem().displayMetrics.density,
            )
        }
    }
}

// ───────────────────────────── Remote config layer ─────────────────────────────

/**
 * Immutable snapshot of every Product Experience variable the Telecom UI consumes.
 * All variables live under the "telecom." root, grouped by screen section:
 * telecom.theme / header / account / usage / actions / bill / benefits / offers /
 * promo / shop / links / nav.
 */
private data class TelecomState(
    // telecom.theme.*
    val primaryColor: String = DEFAULT_PRIMARY,
    val cardRadius: Int = 16,
    // telecom.header.*
    val greeting: String = "Hello,",
    val userName: String = "",
    // telecom.account.*
    val account: String = "33800037",
    val plan: String = "Shahry Voice",
    val accountBadge: String = "eSIM",
    // telecom.usage.*
    val usageVisible: Boolean = true,
    val usageOrder: String = "international,data,units,sms,roaming",
    val intlTitle: String = "International Minutes",
    val intlDescription: String = "Details for international Mins & SMS",
    val dataTitle: String = "Local Data",
    val dataLeft: String = "13.6",
    val dataUnit: String = "GB",
    val unitsTitle: String = "Local Units",
    val unitsValue: String = "Unlimited",
    val smsTitle: String = "SMS Bundle",
    val smsValue: String = "250",
    val roamingTitle: String = "Roaming",
    val roamingValue: String = "Off",
    // telecom.actions.*
    val actionsVisible: Boolean = true,
    val manageLabel: String = "Manage Service",
    val addonsLabel: String = "Buy add-ons",
    // telecom.bill.*
    val billVisible: Boolean = true,
    val billTitle: String = "My Bill",
    val billAmount: String = "QR 38.2",
    val billSubtitle: String = "Total for all accounts",
    val billCta: String = "View & pay bill",
    // telecom.benefits.*
    val benefitsVisible: Boolean = true,
    val benefitsTitle: String = "Activate Free Benefits",
    val benefit1Title: String = "STREAMPLAY",
    val benefit1Subtitle: String = "MALAYALAM",
    val benefit2Title: String = "viu",
    // telecom.offers.*
    val offersVisible: Boolean = true,
    val offersTitle: String = "Your Special Offers",
    val offersTabLabel: String = "Today's Offer",
    val offersViewAll: String = "View all",
    val offersCta: String = "Claim Offer",
    val offer1Title: String = "Local voice offer",
    val offer1Body: String = "Unlimited local minutes valid for 30 days at 50 QAR",
    val offer1Color: String = "#88C7BB",
    val offer1Badge: String = "Local Mins",
    val offer2Title: String = "Waterpark Offer",
    val offer2Body: String = "Buy 1 get 1 entry ticket",
    val offer2Color: String = "#EF9A94",
    val offer2Badge: String = "Tickets",
    val offer3Title: String = "Roaming Pass",
    val offer3Body: String = "Stay connected abroad from 12 QAR a day",
    val offer3Color: String = "#B9B2E8",
    val offer3Badge: String = "Roam",
    val offer4Title: String = "Data Booster",
    val offer4Body: String = "Extra 10 GB for 7 days",
    val offer4Color: String = "#F2C879",
    val offer4Badge: String = "10 GB",
    // telecom.promo.*
    val promoVisible: Boolean = true,
    val rechargeTitle: String = "Recharge &\nTransfer",
    val rewardsTitle: String = "Rewards",
    val rewardsPoints: String = "662",
    val rewardsPointsLabel: String = "points",
    // telecom.shop.*
    val shopVisible: Boolean = true,
    val shopTitle: String = "Shop with Us",
    val prepaidLabel: String = "Buy Prepaid SIM",
    val postpaidLabel: String = "Buy Postpaid SIM",
    val devicesLabel: String = "Buy Devices",
    val homeLabel: String = "Buy Home+",
    // telecom.links.*
    val linksVisible: Boolean = true,
    val linksTitle: String = "Looking for More?",
    val linksSubtitle: String = "Here are some quick links",
    val linksItems: String = "Roaming,Support,Store locator,Offers",
    // telecom.nav.*
    val navHome: String = "Home",
    val navServices: String = "Services",
    val navHelp: String = "Help",
    val navMore: String = "More",
) {
    companion object {
        const val DEFAULT_PRIMARY = "#5B4FCF" // app theme purple_500
    }
}

/**
 * Single source of truth for the Telecom UI-driving CleverTap variables
 * (defineVariable / syncVariables / fetchVariables / addVariablesChangedCallback).
 * Exposes [state] as a StateFlow; the UI never reads CleverTap directly.
 */
private class TelecomVariables(private val ct: CleverTapAPI?, debug: Boolean) {

    private val d = TelecomState()
    private var defined = 0

    private fun <T : Any> def(name: String, default: T): Var<T>? {
        defined++
        val v = ct?.defineVariable(name, default)
        Log.d(PE_DEMO, "defineVariable name=$name default=$default -> " + if (v == null) "NULL" else "ok value=${v.value()}")
        return v
    }

    // Each helper defines the variable once and returns a reader for the latest value.
    private fun str(name: String, default: String): () -> String { val v = def(name, default); return { v?.value() ?: default } }
    private fun flag(name: String, default: Boolean): () -> Boolean { val v = def(name, default); return { v?.value() ?: default } }
    // The dashboard may send 16 or 16.0 for a number; read it as Number so either works.
    private fun int(name: String, default: Int): () -> Int {
        val v = def(name, default)
        return { try { (v?.value() as? Number)?.toInt() ?: default } catch (e: Exception) { default } }
    }

    // telecom.theme.*
    private val primaryColor = str("telecom.theme.primary_color", d.primaryColor)
    private val cardRadius = int("telecom.theme.card_radius", d.cardRadius)
    // telecom.header.*
    private val greeting = str("telecom.header.greeting", d.greeting)
    private val userName = str("telecom.header.user_name", d.userName)
    // telecom.account.*
    private val account = str("telecom.account.number", d.account)
    private val plan = str("telecom.account.plan", d.plan)
    private val accountBadge = str("telecom.account.badge_text", d.accountBadge)
    // telecom.usage.*
    private val usageVisible = flag("telecom.usage.visible", d.usageVisible)
    private val usageOrder = str("telecom.usage.order", d.usageOrder)
    private val intlTitle = str("telecom.usage.international.title", d.intlTitle)
    private val intlDescription = str("telecom.usage.international.description", d.intlDescription)
    private val dataTitle = str("telecom.usage.data.title", d.dataTitle)
    private val dataLeft = str("telecom.usage.data.left", d.dataLeft)
    private val dataUnit = str("telecom.usage.data.unit", d.dataUnit)
    private val unitsTitle = str("telecom.usage.units.title", d.unitsTitle)
    private val unitsValue = str("telecom.usage.units.value", d.unitsValue)
    private val smsTitle = str("telecom.usage.sms.title", d.smsTitle)
    private val smsValue = str("telecom.usage.sms.value", d.smsValue)
    private val roamingTitle = str("telecom.usage.roaming.title", d.roamingTitle)
    private val roamingValue = str("telecom.usage.roaming.value", d.roamingValue)
    // telecom.actions.*
    private val actionsVisible = flag("telecom.actions.visible", d.actionsVisible)
    private val manageLabel = str("telecom.actions.manage.label", d.manageLabel)
    private val addonsLabel = str("telecom.actions.addons.label", d.addonsLabel)
    // telecom.bill.*
    private val billVisible = flag("telecom.bill.visible", d.billVisible)
    private val billTitle = str("telecom.bill.title", d.billTitle)
    private val billAmount = str("telecom.bill.amount", d.billAmount)
    private val billSubtitle = str("telecom.bill.subtitle", d.billSubtitle)
    private val billCta = str("telecom.bill.cta_text", d.billCta)
    // telecom.benefits.*
    private val benefitsVisible = flag("telecom.benefits.visible", d.benefitsVisible)
    private val benefitsTitle = str("telecom.benefits.title", d.benefitsTitle)
    private val benefit1Title = str("telecom.benefits.card1.title", d.benefit1Title)
    private val benefit1Subtitle = str("telecom.benefits.card1.subtitle", d.benefit1Subtitle)
    private val benefit2Title = str("telecom.benefits.card2.title", d.benefit2Title)
    // telecom.offers.*
    private val offersVisible = flag("telecom.offers.visible", d.offersVisible)
    private val offersTitle = str("telecom.offers.title", d.offersTitle)
    private val offersTabLabel = str("telecom.offers.tab_label", d.offersTabLabel)
    private val offersViewAll = str("telecom.offers.view_all_text", d.offersViewAll)
    private val offersCta = str("telecom.offers.cta_text", d.offersCta)
    private val offer1Title = str("telecom.offers.offer1.title", d.offer1Title)
    private val offer1Body = str("telecom.offers.offer1.body", d.offer1Body)
    private val offer1Color = str("telecom.offers.offer1.color", d.offer1Color)
    private val offer1Badge = str("telecom.offers.offer1.badge", d.offer1Badge)
    private val offer2Title = str("telecom.offers.offer2.title", d.offer2Title)
    private val offer2Body = str("telecom.offers.offer2.body", d.offer2Body)
    private val offer2Color = str("telecom.offers.offer2.color", d.offer2Color)
    private val offer2Badge = str("telecom.offers.offer2.badge", d.offer2Badge)
    private val offer3Title = str("telecom.offers.offer3.title", d.offer3Title)
    private val offer3Body = str("telecom.offers.offer3.body", d.offer3Body)
    private val offer3Color = str("telecom.offers.offer3.color", d.offer3Color)
    private val offer3Badge = str("telecom.offers.offer3.badge", d.offer3Badge)
    private val offer4Title = str("telecom.offers.offer4.title", d.offer4Title)
    private val offer4Body = str("telecom.offers.offer4.body", d.offer4Body)
    private val offer4Color = str("telecom.offers.offer4.color", d.offer4Color)
    private val offer4Badge = str("telecom.offers.offer4.badge", d.offer4Badge)
    // telecom.promo.*
    private val promoVisible = flag("telecom.promo.visible", d.promoVisible)
    private val rechargeTitle = str("telecom.promo.recharge.title", d.rechargeTitle)
    private val rewardsTitle = str("telecom.promo.rewards.title", d.rewardsTitle)
    private val rewardsPoints = str("telecom.promo.rewards.points", d.rewardsPoints)
    private val rewardsPointsLabel = str("telecom.promo.rewards.points_label", d.rewardsPointsLabel)
    // telecom.shop.*
    private val shopVisible = flag("telecom.shop.visible", d.shopVisible)
    private val shopTitle = str("telecom.shop.title", d.shopTitle)
    private val prepaidLabel = str("telecom.shop.prepaid.label", d.prepaidLabel)
    private val postpaidLabel = str("telecom.shop.postpaid.label", d.postpaidLabel)
    private val devicesLabel = str("telecom.shop.devices.label", d.devicesLabel)
    private val homeLabel = str("telecom.shop.home.label", d.homeLabel)
    // telecom.links.*
    private val linksVisible = flag("telecom.links.visible", d.linksVisible)
    private val linksTitle = str("telecom.links.title", d.linksTitle)
    private val linksSubtitle = str("telecom.links.subtitle", d.linksSubtitle)
    private val linksItems = str("telecom.links.items", d.linksItems)
    // telecom.nav.*
    private val navHome = str("telecom.nav.home_label", d.navHome)
    private val navServices = str("telecom.nav.services_label", d.navServices)
    private val navHelp = str("telecom.nav.help_label", d.navHelp)
    private val navMore = str("telecom.nav.more_label", d.navMore)

    private val _state = MutableStateFlow(d)
    val state: StateFlow<TelecomState> = _state

    init {
        Log.d(PE_DEMO, "TelecomVariables init: ct=${ct != null} debug=$debug defined=$defined")
        publish("init")
        ct?.addVariablesChangedCallback(object : VariablesChangedCallback() {
            override fun variablesChanged() {
                Log.d(PE_DEMO, "telecom variablesChanged callback fired")
                publish("variablesChanged")
            }
        })
        // syncVariables() only works for test profiles in debug builds (per CleverTap docs)
        if (debug) ct?.syncVariables()
    }

    fun fetch(onDone: (Boolean) -> Unit) {
        if (ct == null) { onDone(false); return }
        ct.fetchVariables { success ->
            Log.d(PE_DEMO, "telecom fetchVariables callback success=$success")
            publish("fetchVariables")
            onDone(success)
        }
    }

    private fun publish(source: String) {
        val next = TelecomState(
            primaryColor = primaryColor(), cardRadius = cardRadius().coerceIn(0, 32),
            greeting = greeting(), userName = userName(),
            account = account(), plan = plan(), accountBadge = accountBadge(),
            usageVisible = usageVisible(), usageOrder = usageOrder(),
            intlTitle = intlTitle(), intlDescription = intlDescription(),
            dataTitle = dataTitle(), dataLeft = dataLeft(), dataUnit = dataUnit(),
            unitsTitle = unitsTitle(), unitsValue = unitsValue(),
            smsTitle = smsTitle(), smsValue = smsValue(),
            roamingTitle = roamingTitle(), roamingValue = roamingValue(),
            actionsVisible = actionsVisible(), manageLabel = manageLabel(), addonsLabel = addonsLabel(),
            billVisible = billVisible(), billTitle = billTitle(), billAmount = billAmount(),
            billSubtitle = billSubtitle(), billCta = billCta(),
            benefitsVisible = benefitsVisible(), benefitsTitle = benefitsTitle(),
            benefit1Title = benefit1Title(), benefit1Subtitle = benefit1Subtitle(), benefit2Title = benefit2Title(),
            offersVisible = offersVisible(), offersTitle = offersTitle(), offersTabLabel = offersTabLabel(),
            offersViewAll = offersViewAll(), offersCta = offersCta(),
            offer1Title = offer1Title(), offer1Body = offer1Body(), offer1Color = offer1Color(), offer1Badge = offer1Badge(),
            offer2Title = offer2Title(), offer2Body = offer2Body(), offer2Color = offer2Color(), offer2Badge = offer2Badge(),
            offer3Title = offer3Title(), offer3Body = offer3Body(), offer3Color = offer3Color(), offer3Badge = offer3Badge(),
            offer4Title = offer4Title(), offer4Body = offer4Body(), offer4Color = offer4Color(), offer4Badge = offer4Badge(),
            promoVisible = promoVisible(), rechargeTitle = rechargeTitle(), rewardsTitle = rewardsTitle(),
            rewardsPoints = rewardsPoints(), rewardsPointsLabel = rewardsPointsLabel(),
            shopVisible = shopVisible(), shopTitle = shopTitle(), prepaidLabel = prepaidLabel(),
            postpaidLabel = postpaidLabel(), devicesLabel = devicesLabel(), homeLabel = homeLabel(),
            linksVisible = linksVisible(), linksTitle = linksTitle(), linksSubtitle = linksSubtitle(), linksItems = linksItems(),
            navHome = navHome(), navServices = navServices(), navHelp = navHelp(), navMore = navMore(),
        )
        Log.d(PE_DEMO, "telecom publish($source) changed=${next != _state.value}")
        _state.value = next
    }
}
