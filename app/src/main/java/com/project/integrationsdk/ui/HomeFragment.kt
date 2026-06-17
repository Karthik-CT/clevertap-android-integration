package com.project.integrationsdk.ui

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.data.UserPrefs
import com.project.integrationsdk.util.bindInboxUnreadCount
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.FragmentHomeBinding
import com.project.integrationsdk.databinding.ItemCardBinding
import com.project.integrationsdk.databinding.ItemCarouselBinding

object ColorPalette {
    data class Swatch(val background: Int, val accent: Int)

    val light = listOf(
        Swatch(0xFFF0EEFF.toInt(), 0xFF5B4FCF.toInt()),
        Swatch(0xFFE2FAF4.toInt(), 0xFF00A878.toInt()),
        Swatch(0xFFFFF0EC.toInt(), 0xFFD9634F.toInt()),
        Swatch(0xFFE8F4FF.toInt(), 0xFF0870C9.toInt()),
        Swatch(0xFFFFF9E5.toInt(), 0xFFC48B00.toInt()),
        Swatch(0xFFFFEAF5.toInt(), 0xFFD63580.toInt()),
        Swatch(0xFFE6FFF9.toInt(), 0xFF00868A.toInt()),
        Swatch(0xFFFFF3E0.toInt(), 0xFFE67E22.toInt()),
        Swatch(0xFFF3E5FF.toInt(), 0xFF8E44AD.toInt()),
    )

    val dark = listOf(
        Swatch(0xFF1E1A3A.toInt(), 0xFF9D94F0.toInt()),
        Swatch(0xFF0D2820.toInt(), 0xFF1D9E75.toInt()),
        Swatch(0xFF2A1510.toInt(), 0xFFD85A30.toInt()),
        Swatch(0xFF081B30.toInt(), 0xFF378ADD.toInt()),
        Swatch(0xFF261A04.toInt(), 0xFFBA7517.toInt()),
        Swatch(0xFF280D1D.toInt(), 0xFFD4537E.toInt()),
        Swatch(0xFF042820.toInt(), 0xFF1D9E8A.toInt()),
        Swatch(0xFF261608.toInt(), 0xFFE67E22.toInt()),
        Swatch(0xFF1E0F30.toInt(), 0xFF9B59B6.toInt()),
    )

    fun at(position: Int, isDark: Boolean): Swatch {
        val list = if (isDark) dark else light
        return list[position % list.size]
    }
}

// ─────────────────────────────────────────────────────────────────
//  DATA MODEL
// ─────────────────────────────────────────────────────────────────

data class ActionCard(val title: String, val onClick: () -> Unit)

// ─────────────────────────────────────────────────────────────────
//  HOME FRAGMENT
// ─────────────────────────────────────────────────────────────────

class HomeFragment : Fragment(), CTInboxListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val ct by lazy { CleverTapManager.getInstance(requireContext() as Context) }
    private val handler = Handler(Looper.getMainLooper())

    private val carouselImages = listOf(
        "https://picsum.photos/800/400?random=10",
        "https://picsum.photos/800/400?random=20",
        "https://picsum.photos/800/400?random=30",
        "https://picsum.photos/800/400?random=40"
    )

    private val infiniteImages by lazy {
        buildList {
            add(carouselImages.last()); addAll(carouselImages); add(carouselImages.first())
        }
    }

    // ✅ Add new ActionCard("Title") { } entries here freely
    private val actionCards by lazy {
        listOf(
            ActionCard("Upload Events") {
                startActivity(Intent(requireContext(), UploadEventsActivity::class.java))
            },
            ActionCard("Upload User Properties") {
                startActivity(Intent(requireContext(), UploadUserPropertiesActivity::class.java))
            },
            ActionCard("Push Notifications") {
                ct?.pushEvent("Karthik's Noti Event"); toast("Push Notification triggered")
            },
            ActionCard("InApp") {
                ct?.pushEvent("Karthik's InApp Event"); toast("InApp triggered")
            },
            ActionCard("Native Display") {
                ct?.pushEvent("Karthik's Native Display Event")
                startActivity(Intent(requireContext(), NativeDisplayActivity::class.java))
            },
            ActionCard("Product Experiences") {
                startActivity(Intent(requireContext(), ProductExperiencesNewActivity::class.java))
            },
            ActionCard("Custom App Inbox") {
                ct?.pushEvent("Karthik's App Inbox Event")
                startActivity(Intent(requireContext(), CustomAppInboxActivity::class.java))
            }
        )
    }

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            binding.viewPager.currentItem += 1
            handler.postDelayed(this, 3500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentHomeBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyStatusBarInset()
        setupToolbar()
        setupCarousel()
        setupList()
        setupInboxBadge()
    }

    // ─────────────────────────────────────────────────────────────
    //  APP INBOX UNREAD BADGE
    //  Initialize the inbox, listen for updates and render the unread
    //  count (ct?.inboxMessageUnreadCount) on top of the bell icon.
    // ─────────────────────────────────────────────────────────────
    private fun setupInboxBadge() {
        ct?.apply {
            ctNotificationInboxListener = this@HomeFragment
            initializeInbox()
        }
        updateInboxBadge()
    }

    private fun updateInboxBadge() {
        _binding?.notifBadge?.bindInboxUnreadCount(ct)
    }

    override fun inboxDidInitialize() {
        activity?.runOnUiThread { updateInboxBadge() }
    }

    override fun inboxMessagesDidUpdate() {
        activity?.runOnUiThread { updateInboxBadge() }
    }

    // ─────────────────────────────────────────────────────────────
    //  STATUS BAR FIX
    //  Attach a WindowInsets listener directly on the toolbar.
    //  This fires reliably on EVERY layout pass — including the
    //  very first frame — so the toolbar is always padded correctly
    //  and never overlaps the clock/battery row.
    // ─────────────────────────────────────────────────────────────
    private fun applyStatusBarInset() {
        val extraPadding = (14 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                statusBarHeight + extraPadding,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        // Request insets immediately so it doesn't wait for the next layout pass
        ViewCompat.requestApplyInsets(binding.toolbarContainer)
    }

    private fun setupToolbar() {
        val firstName = UserPrefs.load(requireContext()).firstName
        binding.tvGreeting.text = "Hi, ${firstName.ifEmpty { "there" }}"

        binding.btnRefresh.setOnClickListener {
            it.animate().scaleX(0.82f).scaleY(0.82f).setDuration(90).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(DecelerateInterpolator()).start()
                toast("Refreshing…")
                restartApp(requireActivity())
            }.start()
        }

        binding.btnNotification.setOnClickListener {
            it.animate().scaleX(0.82f).scaleY(0.82f).setDuration(90).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(DecelerateInterpolator()).start()
                toast("App Inbox")
                ct?.showAppInbox()
            }.start()
        }
    }

    fun restartApp(activity: Activity) {
//        val intent = activity.intent
//        activity.finish()
//        activity.startActivity(intent)

        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    private fun setupCarousel() {
        binding.viewPager.adapter = CarouselAdapter(infiniteImages)
        (binding.viewPager.getChildAt(0) as RecyclerView).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        binding.viewPager.setPageTransformer { page, position ->
            val abs = kotlin.math.abs(position)
            page.alpha = 1f - abs * 0.2f
            page.scaleY = 0.94f + (1f - abs) * 0.06f
        }
        setupIndicators(carouselImages.size)
        setCurrentIndicator(0)
        binding.viewPager.setCurrentItem(1, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.viewPager.post {
                        binding.viewPager.setCurrentItem(
                            carouselImages.size,
                            false
                        )
                    }

                    infiniteImages.size - 1 -> binding.viewPager.post {
                        binding.viewPager.setCurrentItem(
                            1,
                            false
                        )
                    }

                    else -> setCurrentIndicator(position - 1)
                }
            }
        })
    }

    private fun setupIndicators(count: Int) {
        binding.dotLayout.removeAllViews()
        repeat(count) { i ->
            binding.dotLayout.addView(ImageView(requireContext()).apply {
                setImageResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(6, 0, 6, 0) }
            })
        }
    }

    private fun setCurrentIndicator(position: Int) {
        repeat(binding.dotLayout.childCount) { i ->
            (binding.dotLayout.getChildAt(i) as ImageView).apply {
                setImageResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
                animate().scaleX(if (i == position) 1.3f else 1f)
                    .scaleY(if (i == position) 1.3f else 1f)
                    .alpha(if (i == position) 1f else 0.4f)
                    .setDuration(220).setInterpolator(DecelerateInterpolator()).start()
            }
        }
    }

    private fun setupList() {
        val isDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        binding.recyclerGrid.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ActionCardAdapter(actionCards, isDark)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    override fun onResume() {
        super.onResume(); handler.postDelayed(autoScrollRunnable, 3500)
        updateInboxBadge()
    }

    override fun onPause() {
        handler.removeCallbacks(autoScrollRunnable); super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(autoScrollRunnable); _binding = null; super.onDestroyView()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

// ─────────────────────────────────────────────────────────────────
//  CAROUSEL ADAPTER
// ─────────────────────────────────────────────────────────────────

class CarouselAdapter(private val images: List<String>) :
    RecyclerView.Adapter<CarouselAdapter.VH>() {
    inner class VH(val b: ItemCarouselBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.itemView).load(images[position])
            .transition(DrawableTransitionOptions.withCrossFade(300)).into(holder.b.carouselImage)
    }

    override fun getItemCount() = images.size
}

// ─────────────────────────────────────────────────────────────────
//  CARD ADAPTER
// ─────────────────────────────────────────────────────────────────

class ActionCardAdapter(
    private val cards: List<ActionCard>,
    private val isDark: Boolean
) : RecyclerView.Adapter<ActionCardAdapter.VH>() {

    inner class VH(val b: ItemCardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val card = cards[position]
        val swatch = ColorPalette.at(position, isDark)

        holder.b.cardTextView1.text = card.title
        holder.b.root.setCardBackgroundColor(swatch.background)
        holder.b.cardAccentBar.backgroundTintList = ColorStateList.valueOf(swatch.accent)
        holder.b.cardChevron.imageTintList = ColorStateList.valueOf(swatch.accent)

        // Text color: light on dark bg, dark on light bg
        holder.b.cardTextView1.setTextColor(
            if (isDark) 0xFFE8E4FF.toInt() else 0xFF1A1A2E.toInt()
        )

        holder.b.root.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(180)
                    .setInterpolator(DecelerateInterpolator()).start()
                card.onClick()
            }.start()
        }

        // Staggered entrance
        holder.b.root.alpha = 0f
        holder.b.root.translationY = 28f
        ObjectAnimator.ofFloat(holder.b.root, "alpha", 0f, 1f).apply {
            startDelay = position * 55L; duration = 320; start()
        }
        ObjectAnimator.ofFloat(holder.b.root, "translationY", 28f, 0f).apply {
            startDelay = position * 55L; duration = 320
            interpolator = DecelerateInterpolator(); start()
        }
    }

    override fun getItemCount() = cards.size
}