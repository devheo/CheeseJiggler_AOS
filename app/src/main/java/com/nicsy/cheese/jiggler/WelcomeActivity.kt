package com.nicsy.cheese.jiggler

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nicsy.cheese.jiggler.layout.AppPreferences

class WelcomeActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var layoutIndicators: LinearLayout

    data class WelcomeStep(
        val emoji: String,
        val titleRes: Int,
        val descRes: Int
    )

    private val steps = listOf(
        WelcomeStep("🧀", R.string.welcome_step1_title, R.string.welcome_step1_desc),
        WelcomeStep("🖱️", R.string.welcome_step2_title, R.string.welcome_step2_desc),
        WelcomeStep("🔴", R.string.welcome_step3_title, R.string.welcome_step3_desc)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = AppPreferences(this)
        
        if (!prefs.isFirstRun) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_welcome)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        layoutIndicators = findViewById(R.id.layoutIndicators)

        setupViewPager()
        setupIndicators()

        btnNext.setOnClickListener {
            if (viewPager.currentItem < steps.size - 1) {
                viewPager.currentItem += 1
            } else {
                prefs.isFirstRun = false
                startMainActivity()
            }
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = WelcomeAdapter(steps)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                if (position == steps.size - 1) {
                    btnNext.text = getString(R.string.welcome_start_button)
                } else {
                    btnNext.text = getString(R.string.welcome_next_button)
                }
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(steps.size)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, android.R.drawable.presence_invisible)) // Dummy
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setBackgroundColor(Color.parseColor("#FFBB00")) // Active
            } else {
                imageView.setBackgroundColor(Color.parseColor("#D1D1D1")) // Inactive
            }
            // Dot size
            val size = if (i == position) 12 else 8
            val px = (size * resources.displayMetrics.density).toInt()
            val params = imageView.layoutParams as LinearLayout.LayoutParams
            params.width = px
            params.height = px
            imageView.layoutParams = params
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    inner class WelcomeAdapter(private val steps: List<WelcomeStep>) :
        RecyclerView.Adapter<WelcomeAdapter.WelcomeViewHolder>() {

        inner class WelcomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEmoji: TextView = view.findViewById(R.id.tvStepEmoji)
            val tvTitle: TextView = view.findViewById(R.id.tvStepTitle)
            val tvDesc: TextView = view.findViewById(R.id.tvStepDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WelcomeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_welcome_page, parent, false)
            return WelcomeViewHolder(view)
        }

        override fun onBindViewHolder(holder: WelcomeViewHolder, position: Int) {
            val step = steps[position]
            holder.tvEmoji.text = step.emoji
            holder.tvTitle.setText(step.titleRes)
            holder.tvDesc.setText(step.descRes)
        }

        override fun getItemCount(): Int = steps.size
    }
}
