package com.walltext.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.walltext.app.databinding.ActivityQuickInputBinding
import com.walltext.app.engine.WallpaperEngine
import com.walltext.app.widget.ThoughtWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 桌面小部件点击后弹出的悬浮输入框。
 * 直接输入文字 → 立即渲染为壁纸 → 关闭。
 */
class QuickInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickInputBinding
    private lateinit var prefs: SharedPreferences
    private var currentStyle: TextStyle = TextStyle.CLASSIC_WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(TextStyle.PREFS_NAME, MODE_PRIVATE)
        currentStyle = TextStyle.fromKey(prefs.getString(TextStyle.KEY_LAST_STYLE, null))

        // 自动弹出软键盘
        binding.quickInputEdit.post {
            binding.quickInputEdit.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.quickInputEdit, InputMethodManager.SHOW_IMPLICIT)
        }

        setupStyleChips()
        setupActions()

        binding.quickInputEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupStyleChips() {
        val group = binding.quickStyleChips
        group.removeAllViews()
        TextStyle.values().forEach { style ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = style.displayName
                isCheckable = true
                tag = style.key
            }
            chip.setOnClickListener {
                currentStyle = style
                prefs.edit().putString(TextStyle.KEY_LAST_STYLE, style.key).apply()
                updateStyleChipSelection()
            }
            group.addView(chip)
        }
        updateStyleChipSelection()
    }

    private fun updateStyleChipSelection() {
        val group = binding.quickStyleChips
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as com.google.android.material.chip.Chip
            chip.isChecked = (chip.tag == currentStyle.key)
        }
    }

    private fun setupActions() {
        binding.quickCancelButton.setOnClickListener { finish() }
        binding.quickConfirmButton.setOnClickListener {
            val text = binding.quickInputEdit.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) {
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.quickConfirmButton.isEnabled = false
            binding.quickProgress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    val app = application as WallTextApp
                    val dao = app.database.thoughtDao()
                    dao.insert(Thought(text = text, styleKey = currentStyle.key))
                    dao.trimToLimit()
                    WallpaperEngine.renderAndSet(applicationContext, text, currentStyle)
                }
                if (success) {
                    ThoughtWidget.refreshAll(applicationContext)
                    finish()
                } else {
                    binding.quickConfirmButton.isEnabled = true
                    binding.quickProgress.visibility = View.GONE
                    Toast.makeText(this@QuickInputActivity, "设置失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}