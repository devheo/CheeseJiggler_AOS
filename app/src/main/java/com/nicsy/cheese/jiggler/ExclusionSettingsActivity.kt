package com.nicsy.cheese.jiggler

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nicsy.cheese.jiggler.data.ExclusionRange
import com.nicsy.cheese.jiggler.layout.AppPreferences

class ExclusionSettingsActivity : ComponentActivity() {

    private lateinit var rvExclusions: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var adapter: ExclusionAdapter
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exclusion_settings)

        prefs = AppPreferences(this)
        rvExclusions = findViewById(R.id.rvExclusions)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)
        btnAdd = findViewById(R.id.btnAdd)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener { finish() }
        btnAdd.setOnClickListener { showAddDialog() }

        adapter = ExclusionAdapter { deleteRange(it) }
        rvExclusions.layoutManager = LinearLayoutManager(this)
        rvExclusions.adapter = adapter

        refreshList()
    }

    private fun refreshList() {
        val ranges = prefs.getExclusionRanges()
        adapter.submitList(ranges)
        tvEmpty.visibility = if (ranges.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddDialog() {
        val now = java.util.Calendar.getInstance()
        val currentH = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentM = now.get(java.util.Calendar.MINUTE)

        // Pick Start Time (is24HourView = false for AM/PM)
        TimePickerDialog(this, { _, startH, startM ->
            // Pick End Time
            TimePickerDialog(this, { _, endH, endM ->
                val newRange = ExclusionRange(
                    id = System.currentTimeMillis(),
                    startHour = startH,
                    startMinute = startM,
                    endHour = endH,
                    endMinute = endM
                )
                prefs.addExclusionRange(newRange)
                refreshList()
            }, startH, startM, false).apply {
                setTitle(getString(R.string.settings_end_time_label))
                show()
            }
        }, currentH, currentM, false).apply {
            setTitle(getString(R.string.settings_start_time_label))
            show()
        }
    }

    private fun deleteRange(range: ExclusionRange) {
        prefs.removeExclusionRange(range.id)
        refreshList()
    }

    inner class ExclusionAdapter(
        private val onDeleteClick: (ExclusionRange) -> Unit
    ) : RecyclerView.Adapter<ExclusionAdapter.ViewHolder>() {

        private var items = listOf<ExclusionRange>()

        fun submitList(newItems: List<ExclusionRange>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exclusion_range, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvRange.text = getString(R.string.exclude_format, item.startHour, item.startMinute, item.endHour, item.endMinute)
            holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRange: TextView = view.findViewById(R.id.tvRange)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }
    }
}
