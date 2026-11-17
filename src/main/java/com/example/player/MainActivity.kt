package com.example.player

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.ContentUris
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import org.json.JSONArray
import org.json.JSONObject

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val p = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        session = MediaSession.Builder(this, p).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session
    override fun onDestroy() {
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }
}

class MainActivity : Activity() {
    data class Track(val id: Long, val title: String, val artist: String)

    private var all = listOf<Track>()
    private var shown = listOf<Track>()
    private var tab: String? = null
    private val pls = LinkedHashMap<String, MutableList<Long>>()
    private lateinit var prefs: SharedPreferences
    private var player: Player? = null
    private val ui = Handler(Looper.getMainLooper())
    private var accent = Color.parseColor("#FF5A36")
    private var theme = "dark"
    private var bg = 0
    private var fg = 0
    private var surf = 0
    private var mut = 0
    private var dragging = false

    private lateinit var tabsBox: LinearLayout
    private lateinit var listView: ListView
    private lateinit var nowTitle: TextView
    private lateinit var nowArtist: TextView
    private lateinit var seek: SeekBar
    private lateinit var timeTv: TextView
    private lateinit var playBtn: TextView
    private lateinit var shufBtn: TextView
    private lateinit var repBtn: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun fmt(ms: Long): String { val s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60) }

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) { updateNow() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = getSharedPreferences("p", MODE_PRIVATE)
        accent = prefs.getInt("accent", accent)
        theme = prefs.getString("theme", "dark") ?: "dark"
        loadPls()
        build()
        val perms = mutableListOf(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_GRANTED) loadTracks()
        else requestPermissions(perms.toTypedArray(), 1)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val f = MediaController.Builder(this, token).buildAsync()
        f.addListener({ player = f.get(); player?.addListener(listener); updateNow() }, ContextCompat.getMainExecutor(this))
        tick()
    }

    override fun onRequestPermissionsResult(code: Int, p: Array<out String>, r: IntArray) {
        super.onRequestPermissionsResult(code, p, r)
        loadTracks()
    }

    override fun onDestroy() {
        ui.removeCallbacksAndMessages(null)
        (player as? MediaController)?.release()
        super.onDestroy()
    }

    private fun tick() {
        val p = player
        if (p != null && !dragging && p.duration > 0) {
            seek.max = (p.duration / 1000).toInt()
            seek.progress = (p.currentPosition / 1000).toInt()
            timeTv.text = fmt(p.currentPosition) + " / " + fmt(p.duration)
        }
        ui.postDelayed({ tick() }, 500)
    }

    private fun updateNow() {
        val p = player ?: return
        val m = p.currentMediaItem
        nowTitle.text = m?.mediaMetadata?.title ?: "Выберите трек"
        nowArtist.text = m?.mediaMetadata?.artist ?: ""
        playBtn.text = if (p.isPlaying) "❚❚" else "▶"
        shufBtn.setTextColor(if (p.shuffleModeEnabled) accent else mut)
        repBtn.setTextColor(if (p.repeatMode != Player.REPEAT_MODE_OFF) accent else mut)
        repBtn.text = if (p.repeatMode == Player.REPEAT_MODE_ONE) "🔂" else "🔁"
        (listView.adapter as? BaseAdapter)?.notifyDataSetChanged()
    }

    private fun loadPls() {
        pls.clear()
        try {
            val o = JSONObject(prefs.getString("pls", "{}") ?: "{}")
            for (k in o.keys()) {
                val a = o.getJSONArray(k)
                pls[k] = MutableList(a.length()) { a.getLong(it) }
            }
        } catch (e: Exception) { }
    }

    private fun savePls() {
        val o = JSONObject()
        for ((k, v) in pls) o.put(k, JSONArray(v))
        prefs.edit().putString("pls", o.toString()).apply()
    }

    private fun loadTracks() {
        val list = ArrayList<Track>()
        val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val col = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST)
        val sel = "(" + MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.MIME_TYPE + " = 'audio/mpeg') AND " + MediaStore.Audio.Media.DURATION + " > 30000"
        try {
            contentResolver.query(base, col, sel, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE")?.use { c ->
                while (c.moveToNext()) {
                    val art = c.getString(2) ?: ""
                    list.add(Track(c.getLong(0), c.getString(1) ?: "Без названия", if (art == "<unknown>") "" else art))
                }
            }
        } catch (e: Exception) { }
        all = list
        if (list.isEmpty()) Toast.makeText(this, "Музыка не найдена. Проверьте доступ к музыке в настройках приложения.", Toast.LENGTH_LONG).show()
        refreshTabs(); refreshList()
    }

    private fun item(t: Track): MediaItem = MediaItem.Builder()
        .setMediaId(t.id.toString())
        .setUri(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, t.id))
        .setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).build())
        .build()

    private fun playFrom(i: Int) {
        val p = player ?: return
        p.setMediaItems(shown.map { item(it) }, i, 0L)
        p.prepare()
        p.play()
    }

    private fun refreshList() {
        val cur = tab
        shown = if (cur == null) all else (pls[cur] ?: mutableListOf<Long>()).mapNotNull { id -> all.find { it.id == id } }
        listView.adapter = object : BaseAdapter() {
            override fun getCount() = shown.size
            override fun getItem(i: Int): Any = shown[i]
            override fun getItemId(i: Int) = shown[i].id
            override fun getView(i: Int, v: View?, p: ViewGroup?): View {
                val t = shown[i]
                val on = player?.currentMediaItem?.mediaId == t.id.toString()
                val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(10), dp(14), dp(10)) }
                row.addView(TextView(this@MainActivity).apply {
                    text = t.title; textSize = 16f; setSingleLine(); ellipsize = TextUtils.TruncateAt.END
                    setTextColor(if (on) accent else fg)
                    typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                })
                row.addView(TextView(this@MainActivity).apply { text = t.artist; textSize = 13f; setSingleLine(); setTextColor(mut) })
                return row
            }
        }
    }

    private fun refreshTabs() {
        tabsBox.removeAllViews()
        fun add(v: View) { tabsBox.addView(v, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(6) }) }
        add(chip("Все треки", tab == null) { tab = null; refreshTabs(); refreshList() })
        for (n in pls.keys.toList()) {
            val c = chip(n, tab == n) { tab = n; refreshTabs(); refreshList() }
            c.setOnLongClickListener { plMenu(n); true }
            add(c)
        }
        add(chip("＋ Плейлист", false) {
            ask("Новый плейлист", "") { n -> pls[n] = mutableListOf(); savePls(); tab = n; refreshTabs(); refreshList() }
        })
    }

    private fun plMenu(n: String) {
        AlertDialog.Builder(this).setTitle(n).setItems(arrayOf("Переименовать", "Удалить")) { _, w ->
            if (w == 0) ask("Название", n) { nn ->
                val l = pls.remove(n) ?: mutableListOf()
                pls[nn] = l
                if (tab == n) tab = nn
                savePls(); refreshTabs(); refreshList()
            } else {
                pls.remove(n)
                if (tab == n) tab = null
                savePls(); refreshTabs(); refreshList()
            }
        }.show()
    }

    private fun trackMenu(t: Track) {
        val cur = tab
        if (cur != null) {
            AlertDialog.Builder(this).setTitle(t.title).setItems(arrayOf("Убрать из плейлиста")) { _, _ ->
                pls[cur]?.remove(t.id); savePls(); refreshList()
            }.show()
            return
        }
        val names = pls.keys.toList()
        val opts = names.map { "В плейлист «$it»" } + "Новый плейлист…"
        AlertDialog.Builder(this).setTitle(t.title).setItems(opts.toTypedArray()) { _, w ->
            if (w < names.size) {
                val l = pls[names[w]]!!
                if (!l.contains(t.id)) l.add(t.id)
                savePls()
                Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show()
            } else ask("Новый плейлист", "") { n -> pls[n] = mutableListOf(t.id); savePls(); refreshTabs() }
        }.show()
    }

    private fun ask(title: String, init: String, ok: (String) -> Unit) {
        val e = EditText(this).apply { setText(init) }
        AlertDialog.Builder(this).setTitle(title).setView(e)
            .setPositiveButton("Готово") { _, _ -> val n = e.text.toString().trim(); if (n.isNotEmpty()) ok(n) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun settings() {
        AlertDialog.Builder(this).setTitle("Внешний вид").setItems(arrayOf("Цвет акцента", "Тема")) { _, w ->
            if (w == 0) {
                val names = arrayOf("Оранжевый", "Жёлтый", "Зелёный", "Голубой", "Фиолетовый", "Розовый")
                val cols = arrayOf("#FF5A36", "#FFB400", "#2EC27E", "#19B5D6", "#6C63FF", "#E53EA0")
                AlertDialog.Builder(this).setItems(names) { _, i ->
                    accent = Color.parseColor(cols[i]); prefs.edit().putInt("accent", accent).apply(); rebuild()
                }.show()
            } else {
                val ids = arrayOf("dark", "amoled", "light")
                AlertDialog.Builder(this).setItems(arrayOf("Тёмная", "AMOLED", "Светлая")) { _, i ->
                    theme = ids[i]; prefs.edit().putString("theme", theme).apply(); rebuild()
                }.show()
            }
        }.show()
    }

    private fun rebuild() { build(); refreshTabs(); refreshList(); updateNow() }

    private fun chip(t: String, on: Boolean, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 14f
        setTextColor(if (on) Color.WHITE else fg)
        setPadding(dp(14), dp(9), dp(14), dp(9))
        background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(if (on) accent else surf) }
        setOnClickListener { f() }
    }

    private fun ic(t: String, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 24f; gravity = Gravity.CENTER; setTextColor(fg)
        setOnClickListener { f() }
    }

    private fun build() {
        val light = theme == "light"
        bg = Color.parseColor(if (light) "#F2EFEA" else if (theme == "amoled") "#000000" else "#16151A")
        fg = Color.parseColor(if (light) "#1D1B19" else "#F1EEE9")
        surf = Color.parseColor(if (light) "#FFFFFF" else if (theme == "amoled") "#141416" else "#22212A")
        mut = Color.parseColor(if (light) "#79736C" else "#9B96A2")
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (light) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(dp(16), dp(10), dp(16), dp(8)) }
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(this).apply { text = "Плеер"; textSize = 24f; typeface = Typeface.DEFAULT_BOLD; setTextColor(fg) }, LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(chip("Внешний вид", false) { settings() })
        root.addView(head)

        val hs = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        tabsBox = LinearLayout(this)
        hs.addView(tabsBox)
        root.addView(hs, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10); bottomMargin = dp(6) })

        nowTitle = TextView(this).apply { text = "Выберите трек"; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; setTextColor(fg); setSingleLine(); ellipsize = TextUtils.TruncateAt.END; gravity = Gravity.CENTER }
        nowArtist = TextView(this).apply { textSize = 14f; setTextColor(mut); setSingleLine(); gravity = Gravity.CENTER }
        root.addView(nowTitle, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        root.addView(nowArtist, LinearLayout.LayoutParams(-1, -2))

        seek = SeekBar(this).apply {
            progressTintList = ColorStateList.valueOf(accent)
            thumbTintList = ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, u: Boolean) {}
                override fun onStartTrackingTouch(s: SeekBar) { dragging = true }
                override fun onStopTrackingTouch(s: SeekBar) { dragging = false; player?.seekTo(s.progress * 1000L) }
            })
        }
        timeTv = TextView(this).apply { textSize = 12f; setTextColor(mut); gravity = Gravity.CENTER }
        root.addView(seek, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(timeTv, LinearLayout.LayoutParams(-1, -2))

        shufBtn = ic("🔀") { player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
        val prev = ic("⏮") { player?.seekToPrevious() }
        playBtn = ic("▶") { player?.let { if (it.isPlaying) it.pause() else it.play() } }
        playBtn.setTextColor(Color.WHITE)
        playBtn.textSize = 26f
        playBtn.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(accent) }
        val next = ic("⏭") { player?.seekToNext() }
        repBtn = ic("🔁") {
            player?.let {
                it.repeatMode = when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
        shufBtn.setTextColor(mut); repBtn.setTextColor(mut)
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        fun cell(v: View, s: Int) = row.addView(v, LinearLayout.LayoutParams(dp(s), dp(s)).apply { leftMargin = dp(4); rightMargin = dp(4) })
        cell(shufBtn, 48); cell(prev, 52); cell(playBtn, 68); cell(next, 52); cell(repBtn, 48)
        root.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })

        listView = ListView(this).apply {
            divider = null
            setOnItemClickListener { _, _, i, _ -> playFrom(i) }
            setOnItemLongClickListener { _, _, i, _ -> trackMenu(shown[i]); true }
        }
        root.addView(listView, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(10) })
        setContentView(root)
    }
}
