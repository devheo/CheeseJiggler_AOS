package com.nicsy.cheese.jiggler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RemoteGuideActivity : ComponentActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnNext: MaterialButton
    private lateinit var btnClose: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_guide)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnNext = findViewById(R.id.btnNext)
        btnClose = findViewById(R.id.btnClose)

        val adapter = GuideAdapter(
            listOf(
                GuidePage(R.drawable.ic_launcher_foreground, getString(R.string.remote_guide_step1_title), getString(R.string.remote_guide_step1_desc)),
                GuidePage(R.drawable.ico_app_settings, getString(R.string.remote_guide_step2_title), getString(R.string.remote_guide_step2_desc)),
                GuidePage(R.drawable.ic_optical_mouse, getString(R.string.remote_guide_step3_title), getString(R.string.remote_guide_step3_desc))
            )
        )
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                btnNext.text = if (position == adapter.itemCount - 1) getString(R.string.remote_guide_close) else getString(R.string.welcome_next_button)
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                finish()
            }
        }

        btnClose.setOnClickListener { finish() }
    }

    data class GuidePage(val imageRes: Int, val title: String, val desc: String)

    class GuideAdapter(private val pages: List<GuidePage>) : RecyclerView.Adapter<GuideAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivGuide: ImageView = view.findViewById(R.id.ivGuide)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_remote_guide, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val page = pages[position]
            holder.ivGuide.setImageResource(page.imageRes)
            holder.tvTitle.text = page.title
            holder.tvDesc.text = page.desc
        }

        override fun getItemCount() = pages.size
    }
}
