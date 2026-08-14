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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nicsy.cheese.jiggler.layout.AppPreferences

class WelcomeActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var layoutIndicators: LinearLayout

    data class WelcomeStep(
        val emoji: String? = null,
        val imageRes: Int? = null,
        val titleRes: Int,
        val descRes: Int
    )

    private val steps = listOf(
        WelcomeStep(emoji = "🧀", titleRes = R.string.welcome_step1_title, descRes = R.string.welcome_step1_desc),
        WelcomeStep(emoji = "🖱️", titleRes = R.string.welcome_step2_title, descRes = R.string.welcome_step2_desc),
        WelcomeStep(imageRes = R.drawable.ic_optical_mouse, titleRes = R.string.welcome_step3_title, descRes = R.string.welcome_step3_desc)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        prefs = AppPreferences(this)
        
        if (!prefs.isFirstRun) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_welcome)

        val rootLayout = findViewById<View>(R.id.welcomeRootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        layoutIndicators.removeAllViews()
        repeat(steps.size) {
            val imageView = ImageView(this)
            val lp = LinearLayout.LayoutParams(
                (8 * resources.displayMetrics.density).toInt(),
                (8 * resources.displayMetrics.density).toInt()
            )
            lp.setMargins(
                (4 * resources.displayMetrics.density).toInt(), 0,
                (4 * resources.displayMetrics.density).toInt(), 0
            )
            imageView.layoutParams = lp
            imageView.setImageResource(R.drawable.ic_indicator_dot)
            layoutIndicators.addView(imageView)
        }
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until layoutIndicators.childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            val isSelected = i == position
            
            // Tint color
            val color = if (isSelected) R.color.cheese_primary else R.color.cheese_secondary
            imageView.setColorFilter(ContextCompat.getColor(this, color))

            // Size / Shape (Modern Pill effect)
            val width = if (isSelected) 24 else 8
            val height = 8
            val lp = imageView.layoutParams as LinearLayout.LayoutParams
            lp.width = (width * resources.displayMetrics.density).toInt()
            lp.height = (height * resources.displayMetrics.density).toInt()
            imageView.layoutParams = lp
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
            val ivImage: ImageView = view.findViewById(R.id.ivStepImage)
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
            
            if (step.imageRes != null) {
                holder.ivImage.visibility = View.VISIBLE
                holder.tvEmoji.visibility = View.GONE
                holder.ivImage.setImageResource(step.imageRes)
            } else {
                holder.ivImage.visibility = View.GONE
                holder.tvEmoji.visibility = View.VISIBLE
                holder.tvEmoji.text = step.emoji
            }
            
            holder.tvTitle.setText(step.titleRes)
            holder.tvDesc.setText(step.descRes)
        }

        override fun getItemCount(): Int = steps.size
    }
}
