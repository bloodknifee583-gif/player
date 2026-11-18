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
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Size
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

    private val presets = arrayOf(
        arrayOf("#FF2E93", "#7C3AED"), arrayOf("#00E5FF", "#3B82F6"), arrayOf("#B6FF00", "#00C853"),
        arrayOf("#FF6A00", "#FF1744"), arrayOf("#A855F7", "#FF2E93")
    )
    private val presetNames = arrayOf("Розово-фиолетовый", "Циан-синий", "Лайм-зелёный", "Оранжево-красный", "Фиолетово-розовый")

    private var all = listOf<Track>()
    private var shown = listOf<Track>()
    private var tab: String? = null
    private val pls = LinkedHashMap<String, MutableList<Long>>()
    private lateinit var prefs: SharedPreferences
    private var player: Player? = null
    private val ui = Handler(Looper.getMainLooper())
    private var pi = 0
    private var amoled = false
    private var c1 = 0
    private var c2 = 0
    private var onAcc = Color.WHITE
    private var dragging = false
    private var coverId = -2L

    private lateinit var tabsBox: LinearLayout
    private lateinit var listView: ListView
    private lateinit var nowTitle: TextView
    private lateinit var nowArtist: TextView
    private lateinit var seek: SeekBar
    private lateinit var curTv: TextView
    private lateinit var durTv: TextView
    private lateinit var playBtn: TextView
    private lateinit var shufBtn: TextView
    private lateinit var repBtn: TextView
    private lateinit var coverImg: ImageView
    private lateinit var coverNote: TextView

    private val soft = Color.parseColor("#D8CCF2")
    private val white = Color.parseColor("#F7F2FF")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun fmt(ms: Long): String { val s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60) }
    private fun grad(a: Int, b: Int, r: Int, o: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR) =
        GradientDrawable(o, intArrayOf(a, b)).apply { cornerRadius = dp(r).toFloat() }

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) { updateNow() }
    }

    private val trackAdapter = object : BaseAdapter() {
        override fun getCount() = shown.size
        override fun getItem(i: Int): Any = shown[i]
        override fun getItemId(i: Int) = shown[i].id
        override fun getView(i: Int, v: View?, p: ViewGroup?): View {
            val t = shown[i]
            val on = player?.currentMediaItem?.mediaId == t.id.toString()
            val outer = FrameLayout(this@MainActivity).apply { setPadding(dp(16), 0, dp(16), 0) }
            val row = LinearLayout(this@MainActivity).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(10), dp(14), dp(10))
                background = GradientDrawable().apply {
                    cornerRadius = dp(18).toFloat()
                    if (on) { setColor(Color.argb(46, Color.red(c1), Color.green(c1), Color.blue(c1))); setStroke(dp(1), c1) }
                    else setColor(Color.argb(22, 255, 255, 255))
                }
            }
            val badge = TextView(this@MainActivity).apply {
                text = if (on && player?.isPlaying == true) "▶" else "♪"
                textSize = 18f; gravity = Gravity.CENTER
                setTextColor(if (on) onAcc else soft)
                background = if (on) grad(c1, c2, 14) else grad(Color.argb(40, 255, 255, 255), Color.argb(20, 255, 255, 255), 14)
            }
            row.addView(badge, LinearLayout.LayoutParams(dp(44), dp(44)))
            val col = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(this@MainActivity).apply {
                text = t.title; textSize = 16f; setSingleLine(); ellipsize = TextUtils.TruncateAt.END
                setTextColor(if (on) c1 else white)
                typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            })
            col.addView(TextView(this@MainActivity).apply { text = t.artist; textSize = 13f; setSingleLine(); setTextColor(soft) })
            row.addView(col, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(12) })
            outer.addView(row)
            return outer
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = getSharedPreferences("p", MODE_PRIVATE)
        pi = prefs.getInt("preset", 0).coerceIn(0, presets.size - 1)
        amoled = prefs.getBoolean("amoled", false)
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
            curTv.text = fmt(p.currentPosition)
            durTv.text = fmt(p.duration)
        }
        ui.postDelayed({ tick() }, 500)
    }

    private fun updateNow() {
        val p = player ?: return
        val m = p.currentMediaItem
        nowTitle.text = m?.mediaMetadata?.title ?: "Выберите трек"
        nowArtist.text = m?.mediaMetadata?.artist ?: ""
        playBtn.text = if (p.isPlaying) "❚❚" else "▶"
        shufBtn.setTextColor(if (p.shuffleModeEnabled) c1 else soft)
        repBtn.setTextColor(if (p.repeatMode != Player.REPEAT_MODE_OFF) c1 else soft)
        repBtn.text = if (p.repeatMode == Player.REPEAT_MODE_ONE) "🔂" else "🔁"
        trackAdapter.notifyDataSetChanged()
        loadCover()
    }

    private fun loadCover() {
        val id = player?.currentMediaItem?.mediaId?.toLongOrNull() ?: -1L
        if (id == coverId) return
        coverId = id
        coverImg.setImageDrawable(null)
        coverNote.visibility = View.VISIBLE
        if (id < 0 || Build.VERSION.SDK_INT < 29) return
        Thread {
            try {
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val bmp = contentResolver.loadThumbnail(uri, Size(600, 600), null)
                runOnUiThread { if (coverId == id) { coverImg.setImageBitmap(bmp); coverNote.visibility = View.GONE } }
            } catch (e: Exception) { }
        }.start()
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
        trackAdapter.notifyDataSetChanged()
    }

    private fun refreshTabs() {
        tabsBox.removeAllViews()
        fun add(v: View) { tabsBox.addView(v, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) }) }
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
        AlertDialog.Builder(this).setTitle("Внешний вид").setItems(arrayOf("Цветовая схема", "Фон")) { _, w ->
            if (w == 0) {
                AlertDialog.Builder(this).setItems(presetNames) { _, i ->
                    pi = i; prefs.edit().putInt("preset", pi).apply(); rebuild()
                }.show()
            } else {
                AlertDialog.Builder(this).setItems(arrayOf("Неон (фиолетовая ночь)", "AMOLED (чёрный)")) { _, i ->
                    amoled = i == 1; prefs.edit().putBoolean("amoled", amoled).apply(); rebuild()
                }.show()
            }
        }.show()
    }

    private fun rebuild() { coverId = -2L; build(); refreshTabs(); refreshList(); updateNow() }

    private fun chip(t: String, on: Boolean, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (on) onAcc else soft)
        setPadding(dp(16), dp(9), dp(16), dp(9))
        background = if (on) grad(c1, c2, 22, GradientDrawable.Orientation.LEFT_RIGHT)
        else GradientDrawable().apply { cornerRadius = dp(22).toFloat(); setColor(Color.argb(28, 255, 255, 255)); setStroke(dp(1), Color.argb(50, 255, 255, 255)) }
        setOnClickListener { f() }
    }

    private fun ic(t: String, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 24f; gravity = Gravity.CENTER; setTextColor(soft)
        setOnClickListener { f() }
    }

    private fun glow(v: View, color: Int, elev: Int) {
        v.elevation = dp(elev).toFloat()
        if (Build.VERSION.SDK_INT >= 28) { v.outlineAmbientShadowColor = color; v.outlineSpotShadowColor = color }
    }

    private fun build() {
        c1 = Color.parseColor(presets[pi][0])
        c2 = Color.parseColor(presets[pi][1])
        onAcc = if (pi == 2) Color.BLACK else Color.WHITE
        val base = if (amoled) Color.BLACK else Color.parseColor("#07030F")
        fun mix(c: Int, f: Float) = Color.rgb(
            (Color.red(c) * f + Color.red(base) * (1 - f)).toInt(),
            (Color.green(c) * f + Color.green(base) * (1 - f)).toInt(),
            (Color.blue(c) * f + Color.blue(base) * (1 - f)).toInt())
        val top = mix(c1, if (amoled) 0.12f else 0.34f)
        val panelCol = mix(c1, if (amoled) 0.06f else 0.14f)
        window.statusBarColor = top
        window.navigationBarColor = panelCol
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, mix(c2, 0.10f), base))
        }

        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(14), dp(20), dp(16)) }
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(this).apply {
            text = "ПЛЕЕР"; textSize = 28f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.12f
            paint.shader = LinearGradient(0f, 0f, dp(150).toFloat(), 0f, c1, c2, Shader.TileMode.CLAMP)
            setShadowLayer(dp(10).toFloat(), 0f, 0f, c1)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(chip("Стиль", false) { settings() })
        header.addView(head)

        val hs = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        tabsBox = LinearLayout(this)
        hs.addView(tabsBox)
        header.addView(hs, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })

        val side = minOf(resources.displayMetrics.widthPixels - dp(100), dp(300))
        val box = FrameLayout(this).apply {
            background = grad(c1, c2, 30)
            clipToOutline = true
        }
        glow(box, c1, 22)
        coverImg = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        coverNote = TextView(this).apply { text = "♪"; textSize = 90f; gravity = Gravity.CENTER; setTextColor(Color.argb(210, 255, 255, 255)) }
        box.addView(coverImg, FrameLayout.LayoutParams(-1, -1))
        box.addView(coverNote, FrameLayout.LayoutParams(-1, -1))
        header.addView(box, LinearLayout.LayoutParams(side, side).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(26) })

        nowTitle = TextView(this).apply { text = "Выберите трек"; textSize = 22f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); setSingleLine(); ellipsize = TextUtils.TruncateAt.END; gravity = Gravity.CENTER }
        nowArtist = TextView(this).apply { textSize = 15f; setTextColor(soft); setSingleLine(); gravity = Gravity.CENTER }
        header.addView(nowTitle, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(26) })
        header.addView(nowArtist, LinearLayout.LayoutParams(-1, -2))

        listView = ListView(this).apply {
            divider = ColorDrawable(Color.TRANSPARENT)
            dividerHeight = dp(8)
            selector = ColorDrawable(Color.TRANSPARENT)
            setCacheColorHint(Color.TRANSPARENT)
            isVerticalScrollBarEnabled = false
            addHeaderView(header, null, false)
            setAdapter(trackAdapter)
            setOnItemClickListener { _, _, pos, _ -> val i = pos - headerViewsCount; if (i >= 0 && i < shown.size) playFrom(i) }
            setOnItemLongClickListener { _, _, pos, _ -> val i = pos - headerViewsCount; if (i >= 0 && i < shown.size) trackMenu(shown[i]); true }
        }
        root.addView(listView, LinearLayout.LayoutParams(-1, 0, 1f))

        val r = dp(28).toFloat()
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(14), dp(20), dp(12))
            background = GradientDrawable().apply { setColor(panelCol); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) }
        }
        glow(panel, c1, 16)
        seek = SeekBar(this).apply {
            progressTintList = ColorStateList.valueOf(c1)
            thumbTintList = ColorStateList.valueOf(c2)
            progressBackgroundTintList = ColorStateList.valueOf(Color.argb(70, 255, 255, 255))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, u: Boolean) {}
                override fun onStartTrackingTouch(s: SeekBar) { dragging = true }
                override fun onStopTrackingTouch(s: SeekBar) { dragging = false; player?.seekTo(s.progress * 1000L) }
            })
        }
        curTv = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(soft) }
        durTv = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(soft) }
        val seekRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        seekRow.addView(curTv)
        seekRow.addView(seek, LinearLayout.LayoutParams(0, -2, 1f))
        seekRow.addView(durTv)
        panel.addView(seekRow)

        shufBtn = ic("🔀") { player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
        val prev = ic("⏮") { player?.seekToPrevious() }
        prev.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.argb(30, 255, 255, 255)) }
        playBtn = ic("▶") { player?.let { if (it.isPlaying) it.pause() else it.play() } }
        playBtn.setTextColor(onAcc)
        playBtn.textSize = 28f
        playBtn.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c1, c2)).apply { shape = GradientDrawable.OVAL }
        glow(playBtn, c1, 14)
        val next = ic("⏭") { player?.seekToNext() }
        next.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.argb(30, 255, 255, 255)) }
        repBtn = ic("🔁") {
            player?.let {
                it.repeatMode = when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        fun cell(v: View, s: Int) = row.addView(v, LinearLayout.LayoutParams(dp(s), dp(s)).apply { leftMargin = dp(6); rightMargin = dp(6) })
        cell(shufBtn, 46); cell(prev, 54); cell(playBtn, 74); cell(next, 54); cell(repBtn, 46)
        panel.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(panel, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }
}
