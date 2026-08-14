package com.nicsy.cheese.jiggler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nicsy.cheese.jiggler.data.Bookmark
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.layout.JigglerGridLayout

class BookmarkActivity : ComponentActivity() {

    private lateinit var rvBookmarks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: BookmarkAdapter
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        prefs = AppPreferences(this)
        rvBookmarks = findViewById(R.id.rvBookmarks)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener { finish() }

        adapter = BookmarkAdapter(
            onItemClick = { applyBookmark(it) },
            onDeleteClick = { deleteBookmark(it) }
        )

        rvBookmarks.layoutManager = LinearLayoutManager(this)
        rvBookmarks.adapter = adapter

        refreshList()
    }

    private fun refreshList() {
        val bookmarks = prefs.getBookmarks()
        adapter.submitList(bookmarks)
        tvEmpty.visibility = if (bookmarks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun applyBookmark(bookmark: Bookmark) {
        prefs.speedMultiplier = bookmark.speedMultiplier
        prefs.brightnessPercent = bookmark.brightnessPercent
        prefs.timerType = bookmark.timerType
        prefs.durationMinutes = bookmark.durationMinutes
        prefs.targetHour = bookmark.targetHour
        prefs.targetMinute = bookmark.targetMinute
        prefs.startHour = bookmark.startHour
        prefs.startMinute = bookmark.startMinute
        prefs.endHour = bookmark.endHour
        prefs.endMinute = bookmark.endMinute
        prefs.jiggleMode = try { JigglerGridLayout.JiggleMode.valueOf(bookmark.jiggleMode) } catch(e: Exception) { JigglerGridLayout.JiggleMode.BASIC }
        prefs.tileType = try {
            // Handle migration from STRIPE_VERTICAL to STRIPE_HORIZONTAL
            val type = if (bookmark.tileType == "STRIPE_VERTICAL") "STRIPE_HORIZONTAL" else bookmark.tileType
            JigglerGridLayout.TileType.valueOf(type)
        } catch(e: Exception) {
            JigglerGridLayout.TileType.BASIC
        }
        prefs.isStealthEnabled = bookmark.isStealthEnabled
        prefs.stealthActiveSec = bookmark.stealthActiveSec
        prefs.stealthRestSec = bookmark.stealthRestSec

        Toast.makeText(this, getString(R.string.bookmark_applied), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        prefs.removeBookmark(bookmark.id)
        refreshList()
    }

    inner class BookmarkAdapter(
        private val onItemClick: (Bookmark) -> Unit,
        private val onDeleteClick: (Bookmark) -> Unit
    ) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

        private var items = listOf<Bookmark>()

        fun submitList(newItems: List<Bookmark>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvDetails.text = "${item.timerType} | ${item.jiggleMode}"
            holder.itemView.setOnClickListener { onItemClick(item) }
            holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvBookmarkName)
            val tvDetails: TextView = view.findViewById(R.id.tvBookmarkDetails)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }
    }
}
