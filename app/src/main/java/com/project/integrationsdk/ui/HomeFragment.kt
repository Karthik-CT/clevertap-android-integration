package com.project.integrationsdk.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.FragmentHomeBinding
import com.project.integrationsdk.databinding.ItemCarouselBinding
import com.project.integrationsdk.databinding.ItemCardBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val ctInstance by lazy {
        CleverTapAPI.getDefaultInstance(context as Context)
    }
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private val carouselImages = listOf(
        "https://picsum.photos/800/400?1",
        "https://picsum.photos/800/400?2",
        "https://picsum.photos/800/400?3",
        "https://picsum.photos/800/400?4"
    )
    private val infiniteImages by lazy {
        val list = mutableListOf<String>()
        list.add(carouselImages.last())
        list.addAll(carouselImages)
        list.add(carouselImages.first())
        list
    }
    private val gridImages = List(8) {
        val counter = it + 1
        "https://picsum.photos/400/400?${counter}"
    }
    private val gridTitle = listOf("Upload Events", "Upload User Properties", "Push Notifications", "InApp", "Native Display", "Custom App Inbox")

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupCarousel()
        setupGrid()
    }

    private fun setupCarousel() {
        val adapter = CarouselAdapter(infiniteImages)
        binding.viewPager.adapter = adapter
        (binding.viewPager.getChildAt(0) as RecyclerView).apply {
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            itemAnimator = null
        }
        setupIndicators(carouselImages.size)
        setCurrentIndicator(0)
        binding.viewPager.setCurrentItem(1, false)
        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            binding.viewPager.post {
                                binding.viewPager.setCurrentItem(carouselImages.size, false)
                            }
                        }
                        infiniteImages.size - 1 -> {
                            binding.viewPager.post {
                                binding.viewPager.setCurrentItem(1, false)
                            }
                        }
                        else -> {
                            setCurrentIndicator(position - 1)
                        }
                    }
                }
            }
        )
        binding.viewPager.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.alpha = 0.7f + (1 - absPos) * 0.3f
            page.scaleY = 0.9f + (1 - absPos) * 0.1f
            page.scaleX = 0.9f + (1 - absPos) * 0.1f
        }
    }

    private fun setupIndicators(count: Int) {
        binding.dotLayout.removeAllViews()
        repeat(count) {
            val dot = ImageView(requireContext()).apply {
                setImageResource(
                    if (it == 0)
                        R.drawable.dot_active
                    else
                        R.drawable.dot_inactive
                )
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 8, 0) }
            }
            binding.dotLayout.addView(dot)
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.dotLayout.childCount
        for (i in 0 until childCount) {
            val dot = binding.dotLayout.getChildAt(i) as ImageView
            if (i == position) {
                dot.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .alpha(1f)
                    .setDuration(200)
                    .start()
                dot.setImageResource(R.drawable.dot_active)
            } else {
                dot.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(0.5f)
                    .setDuration(200)
                    .start()
                dot.setImageResource(R.drawable.dot_inactive)
            }
        }
    }

    private fun setupGrid() {
        binding.recyclerGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = GridAdapter(gridTitle) { position ->
                when (position) {
                    0 -> {
                        startActivity(Intent(requireContext(), UploadEventsActivity::class.java))
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        startActivity(Intent(requireContext(), UploadUserPropertiesActivity::class.java))
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        ctInstance?.pushEvent("Karthik's Noti Event")
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        ctInstance?.pushEvent("Karthik's InApp Event")
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                    4 -> {
                        ctInstance?.pushEvent("Karthik's Native Display Event")
                        startActivity(Intent(requireContext(), NativeDisplayActivity::class.java))
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                    5 -> {
                        ctInstance?.pushEvent("Karthik's App Inbox Event")
                        startActivity(Intent(requireContext(), CustomAppInboxActivity::class.java))
                        Toast.makeText(requireContext(), "${gridTitle[position]} clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onPause() {
        handler.removeCallbacks(autoScrollRunnable)
        super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(autoScrollRunnable)
        _binding = null
        super.onDestroyView()
    }
}

class CarouselAdapter(private val images: List<String>) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCarouselBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemCarouselBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(images[position])
            .into(holder.binding.carouselImage)
    }

    override fun getItemCount() = images.size
}

class GridAdapter(private val title: List<String>, private val onItemClick: (position: Int) -> Unit) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.cardTextView1.text = title[position]
        holder.binding.root.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = title.size
}


