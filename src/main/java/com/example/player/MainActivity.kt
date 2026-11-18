package com.example.player

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
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

object P {
    const val PLAY = "M8 5v14l11-7z"
    const val PAUSE = "M6 5h4v14H6zM14 5h4v14h-4z"
    const val NEXT = "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"
    const val PREV = "M6 6h2v12H6zm3.5 6l8.5 6V6z"
    const val SHUFFLE = "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z"
    const val REPEAT = "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z"
    const val REPEAT_ONE = "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4zm-4-2V9h-1l-2 1v1h1.5v4H13z"
    const val MENU = "M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"
    const val SHARE = "M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"
    const val DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
    const val ADD = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"
    const val CLOSE = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
    const val CHECK = "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
    const val TUNE = "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z"
    const val NOTE = "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
}

class Ic(ctx: Context, d: String, private var color: Int, private val k: Float = 0.55f) : View(ctx) {
    private var path: Path = PathParser.createPathFromPathData(d)!!
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setIcon(d: String) { path = PathParser.createPathFromPathData(d)!!; invalidate() }
    fun tint(c: Int) { color = c; invalidate() }
    override fun onDraw(c: Canvas) {
        paint.color = color
        val s = minOf(width, height) * k / 24f
        c.save()
        c.translate((width - 24 * s) / 2f, (height - 24 * s) / 2f)
        c.scale(s, s)
        c.drawPath(path, paint)
        c.restore()
    }
}

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

    private val accents = arrayOf("#1DB954", "#FF2E93", "#00E5FF", "#FF6A00", "#A855F7")
    private val accentNames = arrayOf("Зелёный", "Розовый", "Голубой", "Оранжевый", "Фиолетовый")

    private var all = listOf<Track>()
    private var shown = listOf<Track>()
    private var tab: String? = null
    private val pls = LinkedHashMap<String, MutableList<Long>>()
    private val sel = LinkedHashSet<Long>()
    private var selecting = false
    private var pendingDel: List<Long> = emptyList()
    private lateinit var prefs: SharedPreferences
    private var player: Player? = null
    private val ui = Handler(Looper.getMainLooper())
    private var pi = 0
    private var amoled = false
    private var c1 = 0
    private var onAcc = Color.BLACK
    private var base = Color.BLACK
    private var dragging = false
    private var coverId = -2L

    private val white = Color.parseColor("#FFFFFF")
    private val grey = Color.parseColor("#B3B3B3")

    private lateinit var topBar: LinearLayout
    private lateinit var tabsBox: LinearLayout
    private lateinit var listView: ListView
    private lateinit var nowTitle: TextView
    private lateinit var nowArtist: TextView
    private lateinit var seek: SeekBar
    private lateinit var curTv: TextView
    private lateinit var durTv: TextView
    private lateinit var playIc: Ic
    private lateinit var shufIc: Ic
    private lateinit var repIc: Ic
    private lateinit var coverImg: ImageView
    private lateinit var coverNote: Ic

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun fmt(ms: Long): String { val s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60) }
    private fun rr(color: Int, r: Int) = GradientDrawable().apply { cornerRadius = dp(r).toFloat(); setColor(color) }
    private fun mix(c: Int, f: Float) = Color.rgb(
        (Color.red(c) * f + Color.red(base) * (1 - f)).toInt(),
        (Color.green(c) * f + Color.green(base) * (1 - f)).toInt(),
        (Color.blue(c) * f + Color.blue(base) * (1 - f)).toInt())
    private fun uriOf(id: Long) = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

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
            val ctx = this@MainActivity
            val outer = FrameLayout(ctx).apply { setPadding(dp(20), dp(6), dp(18), dp(6)) }
            val row = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
            val badge = FrameLayout(ctx).apply { background = rr(Color.parseColor("#2A2A2A"), 6) }
            badge.addView(Ic(ctx, if (on && player?.isPlaying == true) P.PLAY else P.NOTE, if (on) c1 else grey, 0.5f), FrameLayout.LayoutParams(-1, -1))
            row.addView(badge, LinearLayout.LayoutParams(dp(50), dp(50)))
            val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(ctx).apply {
                text = t.title; textSize = 16f; setSingleLine(); ellipsize = TextUtils.TruncateAt.END
                setTextColor(if (on) c1 else white)
                typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            })
            col.addView(TextView(ctx).apply { text = t.artist; textSize = 13f; setSingleLine(); ellipsize = TextUtils.TruncateAt.END; setTextColor(grey) })
            row.addView(col, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(14) })
            if (selecting) {
                val picked = sel.contains(t.id)
                val box = FrameLayout(ctx).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        if (picked) setColor(c1) else { setColor(Color.TRANSPARENT); setStroke(dp(2), Color.parseColor("#7A7A7A")) }
                    }
                }
                if (picked) box.addView(Ic(ctx, P.CHECK, onAcc, 0.7f), FrameLayout.LayoutParams(-1, -1))
                row.addView(box, LinearLayout.LayoutParams(dp(26), dp(26)).apply { leftMargin = dp(10) })
            }
            outer.addView(row)
            return outer
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = getSharedPreferences("p", MODE_PRIVATE)
        pi = prefs.getInt("acc", 0).coerceIn(0, accents.size - 1)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == RESULT_OK) afterDelete(pendingDel)
    }

    override fun onBackPressed() {
        if (selecting) endSelect() else super.onBackPressed()
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
        playIc.setIcon(if (p.isPlaying) P.PAUSE else P.PLAY)
        shufIc.tint(if (p.shuffleModeEnabled) c1 else grey)
        repIc.setIcon(if (p.repeatMode == Player.REPEAT_MODE_ONE) P.REPEAT_ONE else P.REPEAT)
        repIc.tint(if (p.repeatMode != Player.REPEAT_MODE_OFF) c1 else grey)
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
                val bmp = contentResolver.loadThumbnail(uriOf(id), Size(600, 600), null)
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
        val col = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST)
        val sel = "(" + MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.MIME_TYPE + " = 'audio/mpeg') AND " + MediaStore.Audio.Media.DURATION + " > 30000"
        try {
            contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, col, sel, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE")?.use { c ->
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
        .setUri(uriOf(t.id))
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

    private fun refreshTop() {
        topBar.removeAllViews()
        fun btn(d: String, f: () -> Unit) = topBar.addView(
            Ic(this, d, white, 0.5f).apply { setOnClickListener { f() } }, LinearLayout.LayoutParams(dp(46), dp(46)))
        if (!selecting) {
            topBar.addView(TextView(this).apply { text = "Плеер"; textSize = 26f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) },
                LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(8) })
            btn(P.MENU) { selecting = true; sel.clear(); refreshTop(); trackAdapter.notifyDataSetChanged() }
            btn(P.TUNE) { settings() }
        } else {
            btn(P.CLOSE) { endSelect() }
            topBar.addView(TextView(this).apply { text = "Выбрано: " + sel.size; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) },
                LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(4) })
            btn(P.CHECK) { if (sel.size == shown.size) sel.clear() else sel.addAll(shown.map { it.id }); refreshTop(); trackAdapter.notifyDataSetChanged() }
            btn(P.ADD) { if (sel.isEmpty()) toast("Сначала выберите песни") else addToPlaylist(sel.toList()) }
            btn(P.SHARE) { if (sel.isEmpty()) toast("Сначала выберите песни") else share(sel.toList()) }
            btn(P.DELETE) { if (sel.isEmpty()) toast("Сначала выберите песни") else confirmDelete(sel.toList()) }
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun endSelect() {
        selecting = false; sel.clear(); refreshTop(); trackAdapter.notifyDataSetChanged()
    }

    private fun toggleSel(t: Track) {
        if (!sel.remove(t.id)) sel.add(t.id)
        refreshTop(); trackAdapter.notifyDataSetChanged()
    }

    private fun plMenu(n: String) {
        AlertDialog.Builder(this).setTitle(n).setItems(arrayOf("Переименовать", "Удалить плейлист")) { _, w ->
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
        val opts = mutableListOf("Выбрать несколько", "Добавить в плейлист", "Поделиться", "Удалить с устройства")
        if (tab != null) opts.add(0, "Убрать из плейлиста")
        AlertDialog.Builder(this).setTitle(t.title).setItems(opts.toTypedArray()) { _, w ->
            when (opts[w]) {
                "Убрать из плейлиста" -> { pls[tab!!]?.remove(t.id); savePls(); refreshList() }
                "Выбрать несколько" -> { selecting = true; sel.clear(); sel.add(t.id); refreshTop(); trackAdapter.notifyDataSetChanged() }
                "Добавить в плейлист" -> addToPlaylist(listOf(t.id))
                "Поделиться" -> share(listOf(t.id))
                "Удалить с устройства" -> confirmDelete(listOf(t.id))
            }
        }.show()
    }

    private fun addToPlaylist(ids: List<Long>) {
        val names = pls.keys.toList()
        val opts = names + "＋ Новый плейлист…"
        AlertDialog.Builder(this).setTitle("Добавить в плейлист").setItems(opts.toTypedArray()) { _, w ->
            if (w < names.size) {
                val l = pls[names[w]]!!
                for (id in ids) if (!l.contains(id)) l.add(id)
                savePls(); toast("Добавлено: " + ids.size); endSelect()
            } else ask("Новый плейлист", "") { n -> pls[n] = ids.toMutableList(); savePls(); refreshTabs(); toast("Плейлист создан"); endSelect() }
        }.show()
    }

    private fun share(ids: List<Long>) {
        val i = if (ids.size == 1) Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uriOf(ids[0]))
        else Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(ids.map { uriOf(it) }))
        i.type = "audio/*"
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(i, "Поделиться"))
    }

    private fun confirmDelete(ids: List<Long>) {
        AlertDialog.Builder(this).setTitle("Удалить с устройства?")
            .setMessage("Файлов: " + ids.size + ". Это нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ -> doDelete(ids) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun doDelete(ids: List<Long>) {
        val uris = ids.map { uriOf(it) }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                pendingDel = ids
                val pi = MediaStore.createDeleteRequest(contentResolver, uris)
                startIntentSenderForResult(pi.intentSender, 77, null, 0, 0, 0)
            } else {
                for (u in uris) contentResolver.delete(u, null, null)
                afterDelete(ids)
            }
        } catch (e: Exception) { toast("Не удалось удалить файл") }
    }

    private fun afterDelete(ids: List<Long>) {
        val cur = player?.currentMediaItem?.mediaId?.toLongOrNull()
        if (cur != null && ids.contains(cur)) { player?.stop(); player?.clearMediaItems() }
        endSelect(); loadTracks()
    }

    private fun ask(title: String, init: String, ok: (String) -> Unit) {
        val e = EditText(this).apply { setText(init) }
        AlertDialog.Builder(this).setTitle(title).setView(e)
            .setPositiveButton("Готово") { _, _ -> val n = e.text.toString().trim(); if (n.isNotEmpty()) ok(n) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun settings() {
        AlertDialog.Builder(this).setTitle("Внешний вид").setItems(arrayOf("Цвет акцента", "Фон")) { _, w ->
            if (w == 0) {
                AlertDialog.Builder(this).setItems(accentNames) { _, i ->
                    pi = i; prefs.edit().putInt("acc", pi).apply(); rebuild()
                }.show()
            } else {
                AlertDialog.Builder(this).setItems(arrayOf("Тёмный (как в Spotify)", "AMOLED (чёрный)")) { _, i ->
                    amoled = i == 1; prefs.edit().putBoolean("amoled", amoled).apply(); rebuild()
                }.show()
            }
        }.show()
    }

    private fun rebuild() { coverId = -2L; build(); refreshTabs(); refreshList(); updateNow() }

    private fun chip(t: String, on: Boolean, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (on) onAcc else white)
        setPadding(dp(16), dp(8), dp(16), dp(8))
        background = rr(if (on) c1 else Color.parseColor("#2A2A2A"), 20)
        setOnClickListener { f() }
    }

    private fun build() {
        c1 = Color.parseColor(accents[pi])
        onAcc = if (pi == 4) Color.WHITE else Color.BLACK
        base = if (amoled) Color.BLACK else Color.parseColor("#121212")
        val top = mix(c1, if (amoled) 0.18f else 0.40f)
        val panelCol = mix(Color.WHITE, 0.07f)
        window.statusBarColor = top
        window.navigationBarColor = panelCol
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, base, base))
        }
        topBar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(8), dp(8), dp(4)) }
        root.addView(topBar, LinearLayout.LayoutParams(-1, -2))

        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(6), dp(24), dp(14)) }
        val hs = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        tabsBox = LinearLayout(this)
        hs.addView(tabsBox)
        header.addView(hs, LinearLayout.LayoutParams(-1, -2))

        val side = resources.displayMetrics.widthPixels - dp(48)
        val box = FrameLayout(this).apply {
            background = rr(mix(c1, 0.55f), 12)
            clipToOutline = true
            elevation = dp(18).toFloat()
        }
        coverImg = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        coverNote = Ic(this, P.NOTE, Color.argb(200, 255, 255, 255), 0.4f)
        box.addView(coverImg, FrameLayout.LayoutParams(-1, -1))
        box.addView(coverNote, FrameLayout.LayoutParams(-1, -1))
        header.addView(box, LinearLayout.LayoutParams(side, side).apply { topMargin = dp(18) })

        nowTitle = TextView(this).apply { text = "Выберите трек"; textSize = 24f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); setSingleLine(); ellipsize = TextUtils.TruncateAt.END }
        nowArtist = TextView(this).apply { textSize = 15f; setTextColor(grey); setSingleLine(); ellipsize = TextUtils.TruncateAt.END }
        header.addView(nowTitle, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(20) })
        header.addView(nowArtist, LinearLayout.LayoutParams(-1, -2))

        listView = ListView(this).apply {
            divider = ColorDrawable(Color.TRANSPARENT)
            dividerHeight = 0
            selector = ColorDrawable(Color.TRANSPARENT)
            setCacheColorHint(Color.TRANSPARENT)
            isVerticalScrollBarEnabled = false
            addHeaderView(header, null, false)
            setAdapter(trackAdapter)
            setOnItemClickListener { _, _, pos, _ ->
                val i = pos - headerViewsCount
                if (i >= 0 && i < shown.size) { if (selecting) toggleSel(shown[i]) else playFrom(i) }
            }
            setOnItemLongClickListener { _, _, pos, _ ->
                val i = pos - headerViewsCount
                if (i >= 0 && i < shown.size) { if (selecting) toggleSel(shown[i]) else trackMenu(shown[i]) }
                true
            }
        }
        root.addView(listView, LinearLayout.LayoutParams(-1, 0, 1f))

        val r = dp(22).toFloat()
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = GradientDrawable().apply { setColor(panelCol); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) }
            elevation = dp(12).toFloat()
        }
        seek = SeekBar(this).apply {
            progressTintList = ColorStateList.valueOf(c1)
            thumbTintList = ColorStateList.valueOf(white)
            progressBackgroundTintList = ColorStateList.valueOf(Color.argb(70, 255, 255, 255))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, u: Boolean) {}
                override fun onStartTrackingTouch(s: SeekBar) { dragging = true }
                override fun onStopTrackingTouch(s: SeekBar) { dragging = false; player?.seekTo(s.progress * 1000L) }
            })
        }
        curTv = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(grey) }
        durTv = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(grey) }
        val seekRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        seekRow.addView(curTv)
        seekRow.addView(seek, LinearLayout.LayoutParams(0, -2, 1f))
        seekRow.addView(durTv)
        panel.addView(seekRow)

        shufIc = Ic(this, P.SHUFFLE, grey, 0.55f).apply { setOnClickListener { player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } } }
        val prev = Ic(this, P.PREV, white, 0.6f).apply { setOnClickListener { player?.seekToPrevious() } }
        playIc = Ic(this, P.PLAY, Color.BLACK, 0.55f).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.WHITE) }
            setOnClickListener { player?.let { if (it.isPlaying) it.pause() else it.play() } }
        }
        val next = Ic(this, P.NEXT, white, 0.6f).apply { setOnClickListener { player?.seekToNext() } }
        repIc = Ic(this, P.REPEAT, grey, 0.55f).apply {
            setOnClickListener {
                player?.let {
                    it.repeatMode = when (it.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        else -> Player.REPEAT_MODE_OFF
                    }
                }
            }
        }
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        fun cell(v: View, s: Int) = row.addView(v, LinearLayout.LayoutParams(dp(s), dp(s)).apply { leftMargin = dp(8); rightMargin = dp(8) })
        cell(shufIc, 48); cell(prev, 56); cell(playIc, 66); cell(next, 56); cell(repIc, 48)
        panel.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        root.addView(panel, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        refreshTop()
    }
}
