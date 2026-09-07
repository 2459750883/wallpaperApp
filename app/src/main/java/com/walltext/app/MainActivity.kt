package com.walltext.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.walltext.app.databinding.ActivityMainBinding
import com.walltext.app.engine.WallpaperEngine
import com.walltext.app.widget.ThoughtWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private var currentStyle: TextStyle = TextStyle.CLASSIC_WHITE
    private lateinit var adapter: ThoughtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(TextStyle.PREFS_NAME, MODE_PRIVATE)
        currentStyle = TextStyle.fromKey(prefs.getString(TextStyle.KEY_LAST_STYLE, null))

        setupStyleChips()
        setupQuickPhraseChips()
        setupPreview()
        setupHistory()
        setupActions()

        binding.inputEdit.setText("")
        binding.inputEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                renderPreview()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshHistory()
        renderPreview()
        updateStyleChipSelection()
    }

    private fun setupStyleChips() {
        val group = binding.styleChipGroup
        group.removeAllViews()
        TextStyle.values().forEach { style ->
            val chip = Chip(this).apply {
                text = style.displayName
                isCheckable = true
                tag = style.key
            }
            chip.setOnClickListener {
                currentStyle = style
                prefs.edit().putString(TextStyle.KEY_LAST_STYLE, style.key).apply()
                updateStyleChipSelection()
                renderPreview()
            }
            group.addView(chip)
        }
        updateStyleChipSelection()
    }

    private fun updateStyleChipSelection() {
        val group = binding.styleChipGroup
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            chip.isChecked = (chip.tag == currentStyle.key)
        }
    }

    private fun setupQuickPhraseChips() {
        val group = binding.phraseChipGroup
        group.removeAllViews()
        val phrases = resources.getStringArray(R.array.default_phrases)
        phrases.forEach { phrase ->
            val chip = Chip(this).apply { text = phrase }
            chip.setOnClickListener {
                binding.inputEdit.setText(phrase)
                binding.inputEdit.setSelection(phrase.length)
            }
            group.addView(chip)
        }
    }

    private fun setupPreview() {
        binding.previewImage.setBackgroundColor(0xFFE0E0E0.toInt())
    }

    private fun renderPreview() {
        val text = binding.inputEdit.text?.toString().orEmpty()
        if (text.isBlank()) {
            binding.previewImage.setImageDrawable(null)
            return
        }
        lifecycleScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                val dm = resources.displayMetrics
                // 预览图按 9:16 比例，约略壁纸形状
                val w = (dm.widthPixels * 0.7f).toInt()
                val h = (w * 16 / 9)
                WallpaperEngine.render(w, h, text, currentStyle)
            }
            if (bmp != null) {
                binding.previewImage.setImageBitmap(bmp)
            }
        }
    }

    private fun setupHistory() {
        adapter = ThoughtAdapter(
            onApplyClick = { thought ->
                currentStyle = TextStyle.fromKey(thought.styleKey)
                prefs.edit().putString(TextStyle.KEY_LAST_STYLE, currentStyle.key).apply()
                binding.inputEdit.setText(thought.text)
                updateStyleChipSelection()
                renderPreview()
                applyToWallpaper(thought.text)
            },
            onDeleteClick = { thought ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        (application as WallTextApp).database.thoughtDao().deleteById(thought.id)
                    }
                    refreshHistory()
                }
            }
        )
        binding.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.adapter = adapter
    }

    private fun refreshHistory() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                (application as WallTextApp).database.thoughtDao().getRecent()
            }
            adapter.submitList(list)
            binding.historyEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupActions() {
        binding.applyButton.setOnClickListener {
            val text = binding.inputEdit.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) {
                Toast.makeText(this, "请先输入想法", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyToWallpaper(text)
        }
    }

    private fun applyToWallpaper(text: String) {
        binding.applyButton.isEnabled = false
        binding.applyProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                val app = application as WallTextApp
                val dao = app.database.thoughtDao()
                val id = dao.insert(
                    Thought(text = text, styleKey = currentStyle.key)
                )
                dao.trimToLimit()
                WallpaperEngine.renderAndSet(applicationContext, text, currentStyle)
            }
            binding.applyButton.isEnabled = true
            binding.applyProgress.visibility = View.GONE
            if (success) {
                Toast.makeText(this@MainActivity, "已上墙 ✓", Toast.LENGTH_SHORT).show()
                ThoughtWidget.refreshAll(applicationContext)
                refreshHistory()
            } else {
                Toast.makeText(this@MainActivity, "设置失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }
}