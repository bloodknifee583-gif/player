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
import android.graphics.RadialGradient
import android.graphics.Shader
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
import android.view.MotionEvent
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
    const val DOWN = "M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"
    const val COPY = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"
    const val DRAG = "M20 9H4v2h16V9zM4 15h16v-2H4v2z"
    const val STAR = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
    const val GLOBE = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"
    const val BACK = "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"
    const val RELOAD = "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"
    const val PIP = "M19 7h-8v6h8V7zm2-4H3c-1.1 0-2 .9-2 2v14c0 1.1.9 1.98 2 1.98h18c1.1 0 2-.88 2-1.98V5c0-1.1-.9-2-2-2zm0 16.01H3V4.98h18v14.03z"
    const val PALETTE = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 5-5 0-4.42-4.03-8-9-8zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"
    const val SORT = "M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z"
    const val BOLT = "M7 2v11h3v9l7-12h-4l4-8z"
    const val EQ = "M10 20h4V4h-4v16zm-6 0h4v-8H4v8zM16 9v11h4V9h-4z"
    const val DOWNLOAD = "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"
    const val UPLOAD = "M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"
    const val INFO = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"
    const val EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
    const val PERSON = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
    const val ALBUM = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 14.5c-2.49 0-4.5-2.01-4.5-4.5S9.51 7.5 12 7.5s4.5 2.01 4.5 4.5-2.01 4.5-4.5 4.5zm0-5.5c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1z"
    const val FOLDER = "M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"
    const val HISTORY = "M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"
    const val CHART = "M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"
    const val PLAYLIST = "M3 10h11v2H3v-2zm0-4h11v2H3V6zm0 8h7v2H3v-2zm13-1v8l6-4-6-4z"
    const val QUEUE_ADD = "M14 10H2v2h12v-2zm0-4H2v2h12V6zm4 8v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zM2 16h8v-2H2v2z"
    const val FASTF = "M4 18l8.5-6L4 6v12zm9-12v12l8.5-6L13 6z"
    const val SEARCH = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
    const val HEART = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
    const val TIMER = "M15 1H9v2h6V1zm-4 13h2V8h-2v6zm8.03-6.61l1.42-1.42c-.43-.51-.9-.99-1.41-1.41l-1.42 1.42C16.07 4.74 14.12 4 12 4c-4.97 0-9 4.03-9 9s4.02 9 9 9 9-4.03 9-9c0-2.12-.74-4.07-1.97-5.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"
    const val REPLAY = "M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"
    const val FORWARD = "M4 13c0 4.42 3.58 8 8 8s8-3.58 8-8h-2c0 3.31-2.69 6-6 6s-6-2.69-6-6 2.69-6 6-6v4l5-5-5-5v4c-4.42 0-8 3.58-8 8z"
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
    private fun parse(d: String): Path = try { PathParser.createPathFromPathData(d) ?: Path() } catch (e: Exception) { Path() }
    private var path: Path = parse(d)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setIcon(d: String) { path = parse(d); invalidate() }
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

class Aura(ctx: Context) : View(ctx) {
    var color = Color.GREEN
    var baseColor = Color.BLACK
    var active = false
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var t = 0f
    override fun onDraw(c: Canvas) {
        c.drawColor(baseColor)
        t += if (active) 0.014f else 0.003f
        val w = width.toFloat()
        val h = height.toFloat()
        for (i in 0 until 3) {
            val a = t + i * 2.1f
            val cx = w * (0.5f + 0.38f * kotlin.math.sin(a * (1f + i * 0.3f)))
            val cy = h * (0.30f + i * 0.22f + 0.15f * kotlin.math.cos(a * (0.8f + i * 0.25f)))
            val r = w * (0.60f + 0.14f * kotlin.math.sin(a * 2f))
            p.shader = RadialGradient(cx, cy, r, Color.argb(if (i == 0) 120 else 80, Color.red(color), Color.green(color), Color.blue(color)), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            c.drawCircle(cx, cy, r, p)
        }
        postInvalidateOnAnimation()
    }
}

object Audio { @Volatile var session = 0; var exo: ExoPlayer? = null }

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val p = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        Audio.session = p.audioSessionId
        Audio.exo = p
        p.setSkipSilenceEnabled(getSharedPreferences("p", MODE_PRIVATE).getBoolean("skip", false))
        p.addListener(object : Player.Listener { override fun onAudioSessionIdChanged(audioSessionId: Int) { Audio.session = audioSessionId } })
        session = MediaSession.Builder(this, p).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session
    override fun onDestroy() {
        Audio.exo = null
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }
}

class MainActivity : Activity() {
    data class Track(val id: Long, val title: String, val artist: String, val album: String = "", val folder: String = "")

    class Theme(val name: String, val bg: String, val acc: String, val dark: Boolean = true)
    private val themes = listOf(
        Theme("Spotify", "#121212", "#1DB954"), Theme("AMOLED", "#000000", "#1DB954"), Theme("Неон", "#0A0416", "#FF2E93"),
        Theme("Циан", "#05141A", "#00E5FF"), Theme("Закат", "#1A0C08", "#FF6A00"), Theme("Фиолет", "#0F0818", "#A855F7"),
        Theme("Лайм", "#0B1100", "#B6FF00"), Theme("Океан", "#050F1F", "#3B82F6"), Theme("Рубин", "#150507", "#FF1744"),
        Theme("Золото", "#15110A", "#FFC107"), Theme("Мята", "#07140F", "#2DD4BF"), Theme("Сакура", "#1A0F14", "#FF8FB1"),
        Theme("Лаванда", "#120F1F", "#B39DDB"), Theme("Кофе", "#1A130E", "#D7A86E"), Theme("Лес", "#0A140C", "#7CB342"),
        Theme("Графит", "#1C1D21", "#9AA5B1"), Theme("Светлая", "#F5F5F7", "#1DB954", false), Theme("Бумага", "#F4EFE6", "#D2691E", false), Theme("Свой", "#121212", "#1DB954")
    )

    private var all = listOf<Track>()
    private var shown = listOf<Track>()
    private var tab: String? = null
    private val pls = LinkedHashMap<String, MutableList<Long>>()
    private val sel = LinkedHashSet<Long>()
    private var selecting = false
    private var pendingDel: List<Long> = emptyList()
    private val FAV = "FAV"
    private val favs = LinkedHashSet<Long>()
    private var query = ""
    private var sortMode = 0
    private val REC = "REC"
    private val TOP = "TOP"
    private val HIST = "HIST"
    private var fKind = ""
    private var fVal = ""
    private val plays = HashMap<Long, Int>()
    private val hist = ArrayList<Long>()
    private var rootV: View? = null
    private var fadeSec = 0
    private var skipSil = false
    private val daily = HashMap<String, Long>()
    private var saveCnt = 0
    private var dlReceiver: android.content.BroadcastReceiver? = null
    private var npRefresh: (() -> Unit)? = null
    private var npDrag = false
    private var coverBmp: android.graphics.Bitmap? = null
    private var dragIdx = -1
    private val dupAll = HashSet<Long>()
    private var panelV: View? = null
    private val webs = HashMap<String, android.webkit.WebView>()
    private var webKind = "yt"
    private val web: android.webkit.WebView? get() = webs[webKind]
    private var urlEt: EditText? = null
    private var customAcc = Color.parseColor("#1DB954")
    private var customBg = Color.parseColor("#121212")
    private var ytMode = false
    private var customView: View? = null
    private var customCb: android.webkit.WebChromeClient.CustomViewCallback? = null
    private var restored = false
    private var sleepRun: Runnable? = null
    private lateinit var speedTv: TextView
    private lateinit var heartIc: Ic
    private lateinit var prefs: SharedPreferences
    private var player: Player? = null
    private val ui = Handler(Looper.getMainLooper())
    private var ti = 0
    private var dark = true
    private var panelCol = 0
    private var card = 0
    private var faint = 0
    private var c1 = 0
    private var onAcc = Color.BLACK
    private var base = Color.BLACK
    private var dragging = false
    private var coverId = -2L

    private var white = Color.parseColor("#FFFFFF")
    private var grey = Color.parseColor("#B3B3B3")

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
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (player?.playWhenReady == true) mediaItem?.mediaId?.toLongOrNull()?.let { countPlay(it) }
        }
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
            outer.alpha = if (i == dragIdx) 0.5f else 1f
            val row = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
            val badge = FrameLayout(ctx).apply { background = rr(card, 6) }
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
            if (favs.contains(t.id)) row.addView(Ic(ctx, P.HEART, c1, 0.7f), LinearLayout.LayoutParams(dp(24), dp(24)).apply { leftMargin = dp(8) })
            val curTab = tab
            if (!selecting && curTab != null && curTab != FAV && curTab != REC && curTab != TOP && curTab != HIST) row.addView(Ic(ctx, P.DRAG, grey, 0.7f), LinearLayout.LayoutParams(dp(40), dp(40)).apply { leftMargin = dp(6) })
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
        ti = prefs.getInt("theme", 0).coerceIn(0, themes.size - 1)
        customAcc = prefs.getInt("cacc", customAcc)
        customBg = prefs.getInt("cbg", customBg)
        fadeSec = prefs.getInt("fade", 0)
        skipSil = prefs.getBoolean("skip", false)
        try { val o = JSONObject(prefs.getString("daily", "{}") ?: "{}"); for (k in o.keys()) daily[k] = o.getLong(k) } catch (e: Exception) { }
        sortMode = prefs.getInt("sort", 0)
        try { (prefs.getString("favs", "") ?: "").split(",").filter { it.isNotEmpty() }.forEach { favs.add(it.toLong()) } } catch (e: Exception) { }
        try { val o = JSONObject(prefs.getString("plays", "{}") ?: "{}"); for (k in o.keys()) plays[k.toLong()] = o.getInt(k) } catch (e: Exception) { }
        try { (prefs.getString("hist", "") ?: "").split(",").filter { it.isNotEmpty() }.forEach { hist.add(it.toLong()) } } catch (e: Exception) { }
        loadPls()
        build()
        val perms = mutableListOf(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_GRANTED) loadTracks()
        else requestPermissions(perms.toTypedArray(), 1)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val f = MediaController.Builder(this, token).buildAsync()
        f.addListener({ player = f.get(); player?.addListener(listener); updateNow(); tryRestore() }, ContextCompat.getMainExecutor(this))
        tick()
        fadeTick()
        val rc = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) { ui.postDelayed({ loadTracks() }, 1500) }
        }
        dlReceiver = rc
        ContextCompat.registerReceiver(this, rc, android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onRequestPermissionsResult(code: Int, p: Array<out String>, r: IntArray) {
        super.onRequestPermissionsResult(code, p, r)
        loadTracks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77 && resultCode == RESULT_OK) afterDelete(pendingDel)
        val u = data?.data
        if (requestCode == 78 && resultCode == RESULT_OK && u != null) applyBackup(u)
    }

    override fun onBackPressed() {
        if (customView != null) { customCb?.onCustomViewHidden(); ytChrome.onHideCustomView() }
        else if (ytMode) { if (web?.canGoBack() == true) web?.goBack() else setYt(false) }
        else if (selecting) endSelect() else super.onBackPressed()
    }

    override fun onStop() { saveLast(); saveDaily(); super.onStop() }

    override fun onDestroy() {
        ui.removeCallbacksAndMessages(null)
        for (w in webs.values) w.destroy()
        dlReceiver?.let { try { unregisterReceiver(it) } catch (e: Exception) { } }
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
        if (player?.isPlaying == true) {
            val k = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
            daily[k] = (daily[k] ?: 0L) + 500L
            if (++saveCnt % 20 == 0) saveDaily()
        }
        npRefresh?.invoke()
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
        heartIc.tint(if (favs.contains(m?.mediaId?.toLongOrNull() ?: -1L)) c1 else grey)
        val sp = p.playbackParameters.speed
        speedTv.text = (if (sp % 1f == 0f) sp.toInt().toString() else sp.toString()) + "×"
        speedTv.setTextColor(if (sp != 1f) c1 else grey)
        trackAdapter.notifyDataSetChanged()
        loadCover()
        npRefresh?.invoke()
    }

    private fun loadCover() {
        val id = player?.currentMediaItem?.mediaId?.toLongOrNull() ?: -1L
        if (id == coverId) return
        coverId = id
        animateSwitch()
        coverImg.setImageDrawable(null)
        coverBmp = null
        npRefresh?.invoke()
        coverNote.visibility = View.VISIBLE
        tintTop(null)
        if (id < 0 || Build.VERSION.SDK_INT < 29) return
        Thread {
            try {
                val bmp = contentResolver.loadThumbnail(uriOf(id), Size(600, 600), null)
                runOnUiThread { if (coverId == id) { coverImg.setImageBitmap(bmp); coverBmp = bmp; npRefresh?.invoke(); coverNote.visibility = View.GONE; tintTop(android.graphics.Bitmap.createScaledBitmap(bmp, 1, 1, true).getPixel(0, 0)) } }
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
        val col = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA)
        val sel = "(" + MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.MIME_TYPE + " = 'audio/mpeg') AND " + MediaStore.Audio.Media.DURATION + " > 30000"
        try {
            contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, col, sel, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE")?.use { c ->
                while (c.moveToNext()) {
                    val art = c.getString(2) ?: ""
                    list.add(Track(c.getLong(0), c.getString(1) ?: "Без названия", if (art == "<unknown>") "" else art, c.getString(3) ?: "", (c.getString(4) ?: "").substringBeforeLast('/', "").substringAfterLast('/')))
                }
            }
        } catch (e: Exception) { }
        all = list
        if (list.isEmpty()) Toast.makeText(this, "Музыка не найдена. Проверьте доступ к музыке в настройках приложения.", Toast.LENGTH_LONG).show()
        refreshTabs(); refreshList(); tryRestore()
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
        var l = when {
            cur == null -> when (sortMode) {
                1 -> all.sortedBy { it.artist.lowercase() }
                2 -> all.sortedByDescending { it.id }
                else -> all
            }
            cur == FAV -> all.filter { favs.contains(it.id) }
            cur == REC -> all.sortedByDescending { it.id }.take(50)
            cur == TOP -> all.filter { (plays[it.id] ?: 0) > 0 }.sortedByDescending { plays[it.id] ?: 0 }.take(50)
            cur == HIST -> hist.mapNotNull { id -> all.find { it.id == id } }
            else -> (pls[cur] ?: mutableListOf<Long>()).mapNotNull { id -> all.find { it.id == id } }
        }
        if (query.isNotBlank()) l = l.filter { (it.title + " " + it.artist).contains(query, true) }
        if (fKind.isNotEmpty()) l = l.filter { when (fKind) { "artist" -> it.artist == fVal; "album" -> it.album == fVal; "dup" -> dupAll.contains(it.id); else -> it.folder == fVal } }
        shown = l
        trackAdapter.notifyDataSetChanged()
    }

    private fun refreshTabs() {
        tabsBox.removeAllViews()
        fun add(v: View) { tabsBox.addView(v, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) }) }
        if (query.isNotBlank()) add(ichip(P.CLOSE, "«" + query + "»", true) { query = ""; refreshTabs(); refreshList() })
        add(chip("Все треки", tab == null) { tab = null; refreshTabs(); refreshList() })
        add(ichip(P.HEART, "Избранное", tab == FAV) { tab = FAV; refreshTabs(); refreshList() })
        add(ichip(P.PLAYLIST, "Обзор", false) { browse() })
        if (fKind.isNotEmpty()) add(ichip(P.CLOSE, fVal, true) { fKind = ""; fVal = ""; refreshTabs(); refreshList() })
        if (tab == REC || tab == TOP || tab == HIST) add(ichip(P.CLOSE, autoName(tab!!), true) { tab = null; refreshTabs(); refreshList() })
        for (n in pls.keys.toList()) {
            val c = chip(n, tab == n) { tab = n; refreshTabs(); refreshList() }
            c.setOnLongClickListener { plMenu(n); true }
            add(c)
        }
        add(ichip(P.ADD, "Плейлист", false) {
            ask("Новый плейлист", "") { n -> pls[n] = mutableListOf(); savePls(); tab = n; refreshTabs(); refreshList() }
        })
    }

    private fun refreshTop() {
        topBar.removeAllViews()
        fun btn(d: String, f: () -> Unit) = topBar.addView(
            Ic(this, d, white, 0.5f).apply { setOnClickListener { f() } }, LinearLayout.LayoutParams(dp(46), dp(46)))
        if (ytMode) {
            btn(P.BACK) { if (web?.canGoBack() == true) web?.goBack() else setYt(false) }
            if (webKind == "yt") {
                topBar.addView(TextView(this).apply { text = "YouTube"; textSize = 22f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) },
                    LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(4) })
                btn(P.RELOAD) { web?.reload() }
                btn(P.PIP) { pip() }
            } else {
                val et = EditText(this).apply {
                    setSingleLine(); setTextColor(white); setHintTextColor(grey); hint = "Поиск или адрес"; textSize = 14f
                    imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_GO
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
                    setPadding(dp(14), 0, dp(14), 0)
                    background = rr(card, 20)
                    setText(web?.url ?: "")
                    setOnEditorActionListener { v, _, _ -> openUrl(v.text.toString()); true }
                }
                urlEt = et
                topBar.addView(et, LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(4); rightMargin = dp(4) })
                btn(P.RELOAD) { web?.reload() }
                btn(P.STAR) { bookmarks() }
            }
            btn(P.CLOSE) { setYt(false) }
            return
        }
        if (!selecting) {
            topBar.addView(TextView(this).apply { text = "Плеер"; textSize = 26f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) },
                LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(8) })
            val yt = FrameLayout(this).apply { setOnClickListener { openWeb("yt") } }
            yt.addView(Ic(this, P.PLAY, Color.WHITE, 0.55f).apply { background = rr(Color.parseColor("#FF0033"), 8) }, FrameLayout.LayoutParams(dp(34), dp(24), Gravity.CENTER))
            topBar.addView(yt, LinearLayout.LayoutParams(dp(48), dp(46)))
            btn(P.GLOBE) { openWeb("br") }
            btn(P.SEARCH) { ask("Поиск", query) { q -> query = q; refreshTabs(); refreshList() } }
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

    private fun row(icon: String, label: String, f: () -> Unit) = Triple(icon, label, f)

    private fun present(d: android.app.Dialog, box: LinearLayout) {
        d.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        d.setContentView(ScrollView(this).apply { addView(box) })
        d.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.6f)
        }
        d.show()
    }

    private fun sheetBox(title: String): LinearLayout {
        val r = dp(24).toFloat()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(16), dp(10), dp(24))
            background = GradientDrawable().apply { setColor(panelCol); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) }
        }
        box.addView(TextView(this).apply { text = title; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); setSingleLine(); ellipsize = TextUtils.TruncateAt.END; setPadding(dp(14), 0, dp(14), dp(10)) })
        return box
    }

    private fun sheet(title: String, rows: List<Triple<String, String, () -> Unit>>) {
        val d = android.app.Dialog(this)
        val box = sheetBox(title)
        for ((ic, label, f) in rows) {
            val line = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), 0, dp(14), 0); setOnClickListener { d.dismiss(); f() } }
            line.addView(Ic(this, ic, c1, 0.6f), LinearLayout.LayoutParams(dp(40), dp(52)))
            line.addView(TextView(this).apply { text = label; textSize = 16f; setTextColor(white); setSingleLine(); ellipsize = TextUtils.TruncateAt.END }, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(10) })
            box.addView(line, LinearLayout.LayoutParams(-1, dp(52)))
        }
        present(d, box)
    }

    private fun themeSheet() {
        val d = android.app.Dialog(this)
        val box = sheetBox("Темы")
        var line: LinearLayout? = null
        themes.forEachIndexed { i, th ->
            if (i % 3 == 0) { line = LinearLayout(this); box.addView(line, LinearLayout.LayoutParams(-1, -2)) }
            val cu = i == themes.size - 1
            val bgc = if (cu) customBg else Color.parseColor(th.bg)
            val ac = if (cu) customAcc else Color.parseColor(th.acc)
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(dp(6), dp(6), dp(6), dp(8))
                setOnClickListener { d.dismiss(); if (cu) colorEditor() else { ti = i; prefs.edit().putInt("theme", i).apply(); rebuild() } }
            }
            val sw = FrameLayout(this).apply {
                background = GradientDrawable().apply { cornerRadius = dp(16).toFloat(); setColor(bgc); setStroke(dp(if (i == ti) 3 else 1), if (i == ti) ac else Color.argb(70, 128, 128, 128)) }
            }
            sw.addView(View(this).apply { background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(ac) } }, FrameLayout.LayoutParams(dp(28), dp(28)).apply { gravity = Gravity.CENTER })
            cell.addView(sw, LinearLayout.LayoutParams(-1, dp(64)))
            cell.addView(TextView(this).apply { text = th.name; textSize = 12f; setTextColor(white); gravity = Gravity.CENTER; setSingleLine() }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(4) })
            line!!.addView(cell, LinearLayout.LayoutParams(0, -2, 1f))
        }
        present(d, box)
    }

    private fun plMenu(n: String) {
        sheet(n, listOf(
            row(P.EDIT, "Переименовать") { ask("Название", n) { nn -> val l = pls.remove(n) ?: mutableListOf(); pls[nn] = l; if (tab == n) tab = nn; savePls(); refreshTabs(); refreshList() } },
            row(P.DELETE, "Удалить плейлист") { pls.remove(n); if (tab == n) tab = null; savePls(); refreshTabs(); refreshList() }
        ))
    }

    private fun trackMenu(t: Track) {
        val rows = ArrayList<Triple<String, String, () -> Unit>>()
        if (tab != null && tab != FAV) rows.add(row(P.CLOSE, "Убрать из плейлиста") { pls[tab!!]?.remove(t.id); savePls(); refreshList() })
        rows.add(row(P.FASTF, "Играть следующей") { enqueue(t, true) })
        rows.add(row(P.QUEUE_ADD, "В конец очереди") { enqueue(t, false) })
        rows.add(row(P.HEART, if (favs.contains(t.id)) "Убрать из избранного" else "В избранное") { toggleFav(t.id) })
        rows.add(row(P.CHECK, "Выбрать несколько") { selecting = true; sel.clear(); sel.add(t.id); refreshTop(); trackAdapter.notifyDataSetChanged() })
        rows.add(row(P.ADD, "Добавить в плейлист") { addToPlaylist(listOf(t.id)) })
        rows.add(row(P.SHARE, "Поделиться") { share(listOf(t.id)) })
        rows.add(row(P.INFO, "Информация") { info(t) })
        rows.add(row(P.DELETE, "Удалить с устройства") { confirmDelete(listOf(t.id)) })
        sheet(t.title, rows)
    }

    private fun addToPlaylist(ids: List<Long>) {
        val rows = ArrayList<Triple<String, String, () -> Unit>>()
        for (n in pls.keys.toList()) rows.add(row(P.PLAYLIST, n) {
            val l = pls[n]!!
            for (id in ids) if (!l.contains(id)) l.add(id)
            savePls(); toast("Добавлено: " + ids.size); endSelect()
        })
        rows.add(row(P.ADD, "Новый плейлист") { ask("Новый плейлист", "") { n -> pls[n] = ids.toMutableList(); savePls(); refreshTabs(); toast("Плейлист создан"); endSelect() } })
        sheet("Добавить в плейлист", rows)
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
        if (fKind == "dup") { fKind = ""; fVal = "" }
        endSelect(); loadTracks()
    }

    private fun ask(title: String, init: String, ok: (String) -> Unit) {
        val e = EditText(this).apply { setText(init) }
        AlertDialog.Builder(this).setTitle(title).setView(e)
            .setPositiveButton("Готово") { _, _ -> val n = e.text.toString().trim(); if (n.isNotEmpty()) ok(n) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun settings() {
        sheet("Настройки", listOf(
            row(P.PALETTE, "Темы") { themeSheet() },
            row(P.SORT, "Сортировка") { sortSheet() },
            row(P.BOLT, "Скорость воспроизведения") { speedDialog() },
            row(P.TIMER, "Таймер сна") { sleepDialog() },
            row(P.GLOBE, "Бесплатная музыка") { onlineSheet() },
            row(P.CHART, "Итоги недели и года") { recap() },
            row(P.BOLT, "Плавный переход между треками") { fadeSheet() },
            row(P.NEXT, if (skipSil) "Пропуск тишины: включён" else "Пропуск тишины: выключен") { toggleSkip() },
            row(P.UPLOAD, "Резервная копия в файл") { backup() },
            row(P.DOWNLOAD, "Восстановить из файла") { restore() },
            row(P.COPY, "Найти дубликаты") { findDups() },
            row(P.UPLOAD, "Экспорт плейлистов") { exportPls() },
            row(P.DOWNLOAD, "Импорт плейлистов") { importPls() }
        ))
    }

    private fun sortSheet() {
        val n = listOf("По названию", "По исполнителю", "Сначала новые")
        sheet("Сортировка", n.mapIndexed { i, t -> row(P.SORT, t) { sortMode = i; prefs.edit().putInt("sort", i).apply(); refreshList() } })
    }

    private fun speedDialog() {
        val v = floatArrayOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
        val n = listOf("0.5×", "0.75×", "1× обычная", "1.25×", "1.5×", "2×")
        sheet("Скорость", n.mapIndexed { i, t -> row(P.BOLT, t) { player?.setPlaybackSpeed(v[i]); updateNow() } })
    }

    private fun sleepDialog() {
        val mins = intArrayOf(15, 30, 45, 60, 0)
        val n = listOf("15 минут", "30 минут", "45 минут", "1 час", "Выключить таймер")
        sheet("Таймер сна", n.mapIndexed { i, t ->
            row(P.TIMER, t) {
                sleepRun?.let { ui.removeCallbacks(it) }
                sleepRun = null
                if (mins[i] > 0) {
                    val r = Runnable { player?.pause(); toast("Таймер сна: музыка остановлена") }
                    sleepRun = r
                    ui.postDelayed(r, mins[i] * 60000L)
                    toast("Музыка остановится через " + mins[i] + " мин")
                } else toast("Таймер выключен")
            }
        })
    }

    private fun tryRestore() {
        if (restored) return
        val p = player ?: return
        if (all.isEmpty()) return
        restored = true
        if (p.currentMediaItem != null) return
        val idx = all.indexOfFirst { it.id == prefs.getLong("last", -1L) }
        if (idx < 0) return
        p.setMediaItems(all.map { item(it) }, idx, prefs.getLong("pos", 0L))
        p.prepare()
    }

    private fun saveLast() {
        val p = player ?: return
        val id = p.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        prefs.edit().putLong("last", id).putLong("pos", p.currentPosition).apply()
    }

    private fun toggleFav(id: Long) {
        if (!favs.remove(id)) favs.add(id)
        prefs.edit().putString("favs", favs.joinToString(",")).apply()
        updateNow(); refreshList()
    }

    private fun enqueue(t: Track, next: Boolean) {
        val p = player ?: return
        if (p.mediaItemCount == 0) { p.setMediaItem(item(t)); p.prepare(); p.play(); return }
        if (next) p.addMediaItem(p.currentMediaItemIndex + 1, item(t)) else p.addMediaItem(item(t))
        toast(if (next) "Сыграет следующей" else "Добавлено в очередь")
    }

    private fun info(t: Track) {
        var msg = t.title + "\n" + t.artist
        try {
            val c = contentResolver.query(uriOf(t.id), arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA), null, null, null)
            if (c != null) {
                if (c.moveToFirst()) {
                    msg = "Файл: " + c.getString(0) + "\nФормат: " + c.getString(2) + "\nРазмер: " + String.format("%.1f МБ", c.getLong(1) / 1048576.0) + "\nДлительность: " + fmt(c.getLong(3)) + "\nПуть: " + c.getString(4)
                }
                c.close()
            }
        } catch (e: Exception) { }
        AlertDialog.Builder(this).setTitle("Информация").setMessage(msg).setPositiveButton("OK", null).show()
    }

    private fun tintTop(px: Int?) {
        val t = if (px == null) mix(c1, if (dark) 0.38f else 0.30f) else mix(px, if (dark) 0.50f else 0.35f)
        rootV?.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(t, base, base))
        window.statusBarColor = t
    }

    private fun countPlay(id: Long) {
        plays[id] = (plays[id] ?: 0) + 1
        hist.remove(id)
        hist.add(0, id)
        while (hist.size > 50) hist.removeAt(hist.size - 1)
        val o = JSONObject()
        for ((k, v) in plays) o.put(k.toString(), v)
        prefs.edit().putString("plays", o.toString()).putString("hist", hist.joinToString(",")).apply()
    }

    private fun autoName(k: String) = when (k) { REC -> "Недавно добавленные"; TOP -> "Часто слушаемые"; else -> "История" }

    private fun pickGroup(kind: String, values: List<String>) {
        val g = values.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().toList().sortedBy { it.first.lowercase() }
        if (g.isEmpty()) { toast("Пусто"); return }
        val icon = if (kind == "artist") P.PERSON else if (kind == "album") P.ALBUM else P.FOLDER
        sheet("Выберите", g.map { e -> row(icon, e.first + " (" + e.second + ")") { fKind = kind; fVal = e.first; tab = null; refreshTabs(); refreshList() } })
    }

    private fun browse() {
        fun go(k: String) { tab = k; refreshTabs(); refreshList() }
        sheet("Обзор", listOf(
            row(P.PERSON, "Исполнители") { pickGroup("artist", all.map { it.artist }) },
            row(P.ALBUM, "Альбомы") { pickGroup("album", all.map { it.album }) },
            row(P.FOLDER, "Папки") { pickGroup("folder", all.map { it.folder }) },
            row(P.NOTE, "Недавно добавленные") { go(REC) },
            row(P.CHART, "Часто слушаемые") { go(TOP) },
            row(P.HISTORY, "История") { go(HIST) },
            row(P.INFO, "Статистика") { stats() }
        ))
    }

    private fun stats() {
        val top = plays.entries.sortedByDescending { it.value }.take(10).mapNotNull { e -> all.find { it.id == e.key }?.let { it.title + " — " + e.value } }
        val msg = "Всего прослушиваний: " + plays.values.sum() + "\n\n" + (if (top.isEmpty()) "Пока пусто" else top.joinToString("\n"))
        AlertDialog.Builder(this).setTitle("Статистика").setMessage(msg).setPositiveButton("OK", null).show()
    }

    private fun exportPls() {
        val o = JSONObject()
        for ((k, v) in pls) o.put(k, JSONArray(v.mapNotNull { id -> all.find { it.id == id }?.let { it.artist + "|" + it.title } }))
        val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, o.toString())
        startActivity(Intent.createChooser(i, "Экспорт плейлистов"))
    }

    private fun importPls() {
        ask("Вставьте текст экспорта", "") { txt ->
            try {
                val o = JSONObject(txt)
                for (k in o.keys()) {
                    val a = o.getJSONArray(k)
                    val ids = ArrayList<Long>()
                    for (j in 0 until a.length()) { val key = a.getString(j); all.find { it.artist + "|" + it.title == key }?.let { ids.add(it.id) } }
                    pls[k] = ids
                }
                savePls(); refreshTabs(); toast("Плейлисты импортированы")
            } catch (e: Exception) { toast("Неверный текст") }
        }
    }

    private fun ichip(icon: String, t: String, on: Boolean, f: () -> Unit): View {
        val lay = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(10), dp(4), dp(16), dp(4))
            background = rr(if (on) c1 else card, 20)
            setOnClickListener { f() }
        }
        lay.addView(Ic(this, icon, if (on) onAcc else white, 0.9f), LinearLayout.LayoutParams(dp(22), dp(22)))
        lay.addView(TextView(this).apply { text = t; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(if (on) onAcc else white) }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(4) })
        return lay
    }

    private val ytChrome = object : android.webkit.WebChromeClient() {
        override fun onShowCustomView(v: View, cb: android.webkit.WebChromeClient.CustomViewCallback) {
            customView = v
            customCb = cb
            (window.decorView as ViewGroup).addView(v, ViewGroup.LayoutParams(-1, -1))
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        override fun onHideCustomView() {
            customView?.let { (window.decorView as ViewGroup).removeView(it) }
            customView = null
            window.decorView.systemUiVisibility = if (dark) 0 else (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 0x10)
        }
    }

    private fun mkWeb(kind: String): android.webkit.WebView {
        val w = android.webkit.WebView(this)
        w.settings.javaScriptEnabled = true
        w.settings.domStorageEnabled = true
        w.settings.mediaPlaybackRequiresUserGesture = false
        w.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                if (kind == "br" && webKind == "br") urlEt?.setText(url ?: "")
            }
        }
        w.webChromeClient = ytChrome
        w.loadUrl(if (kind == "yt") "https://m.youtube.com" else "https://www.google.com")
        (listView.parent as? LinearLayout)?.addView(w, LinearLayout.LayoutParams(-1, 0, 1f))
        return w
    }

    private fun openWeb(kind: String) {
        webKind = kind
        if (kind == "yt") player?.pause()
        setYt(true)
    }

    private fun setYt(on: Boolean) {
        ytMode = on
        if (on && web == null) webs[webKind] = mkWeb(webKind)
        listView.visibility = if (on) View.GONE else View.VISIBLE
        panelV?.visibility = if (on) View.GONE else View.VISIBLE
        for ((k, w) in webs) w.visibility = if (on && k == webKind) View.VISIBLE else View.GONE
        refreshTop()
    }

    private fun openUrl(t: String) {
        val q = t.trim()
        if (q.isEmpty()) return
        val url = when {
            q.startsWith("http://") || q.startsWith("https://") -> q
            q.contains(".") && !q.contains(" ") -> "https://" + q
            else -> "https://www.google.com/search?q=" + android.net.Uri.encode(q)
        }
        web?.loadUrl(url)
        urlEt?.clearFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(urlEt?.windowToken, 0)
    }

    private fun saveBm(l: List<Pair<String, String>>) {
        prefs.edit().putString("bm", l.joinToString("\n") { it.first + "|" + it.second }).apply()
    }

    private fun bookmarks() {
        val list = ArrayList<Pair<String, String>>()
        (prefs.getString("bm", "") ?: "").split("\n").filter { it.contains("|") }.forEach { list.add(Pair(it.substringBefore("|"), it.substringAfter("|"))) }
        if (list.isEmpty()) {
            list.add(Pair("Google", "https://www.google.com"))
            list.add(Pair("YouTube", "https://m.youtube.com"))
            list.add(Pair("Википедия", "https://ru.wikipedia.org"))
            list.add(Pair("GitHub", "https://github.com"))
        }
        val rows = ArrayList<Triple<String, String, () -> Unit>>()
        rows.add(row(P.ADD, "Добавить эту страницу") {
            val u = web?.url ?: ""
            val t = web?.title ?: u
            if (u.isNotEmpty()) { list.add(Pair(t.replace("|", " ").replace("\n", " "), u)); saveBm(list); toast("Закладка добавлена") }
        })
        for (b in list) rows.add(row(P.STAR, b.first) { web?.loadUrl(b.second) })
        rows.add(row(P.DELETE, "Очистить закладки") { prefs.edit().remove("bm").apply(); toast("Закладки очищены") })
        sheet("Закладки", rows)
    }

    private fun colorEditor() {
        val d = android.app.Dialog(this)
        val box = sheetBox("Свои цвета")
        val ah = FloatArray(3)
        val bh = FloatArray(3)
        Color.colorToHSV(customAcc, ah)
        Color.colorToHSV(customBg, bh)
        val prev = FrameLayout(this)
        val dot = View(this)
        prev.addView(dot, FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER))
        fun upd() {
            customAcc = Color.HSVToColor(ah)
            customBg = Color.HSVToColor(bh)
            prev.background = rr(customBg, 16)
            dot.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(customAcc) }
        }
        fun slider(label: String, arr: FloatArray, i: Int, top: Int, scale: Float) {
            box.addView(TextView(this).apply { text = label; textSize = 13f; setTextColor(grey); setPadding(dp(14), dp(8), dp(14), 0) })
            box.addView(SeekBar(this).apply {
                this.max = top
                progress = (arr[i] * scale).toInt()
                progressTintList = ColorStateList.valueOf(c1)
                thumbTintList = ColorStateList.valueOf(c1)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar, p: Int, u: Boolean) { arr[i] = p / scale; upd() }
                    override fun onStartTrackingTouch(s: SeekBar) {}
                    override fun onStopTrackingTouch(s: SeekBar) {}
                })
            })
        }
        box.addView(prev, LinearLayout.LayoutParams(-1, dp(72)).apply { bottomMargin = dp(6) })
        slider("Акцент: оттенок", ah, 0, 360, 1f)
        slider("Акцент: насыщенность", ah, 1, 100, 100f)
        slider("Акцент: яркость", ah, 2, 100, 100f)
        slider("Фон: оттенок", bh, 0, 360, 1f)
        slider("Фон: насыщенность", bh, 1, 100, 100f)
        slider("Фон: яркость", bh, 2, 100, 100f)
        upd()
        box.addView(ichip(P.CHECK, "Применить", true) {
            d.dismiss()
            ti = themes.size - 1
            prefs.edit().putInt("cacc", customAcc).putInt("cbg", customBg).putInt("theme", ti).apply()
            rebuild()
        }, LinearLayout.LayoutParams(-2, -2).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(14) })
        present(d, box)
    }

    private fun pip() {
        try {
            if (Build.VERSION.SDK_INT >= 26) enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().setAspectRatio(android.util.Rational(16, 9)).build())
            else toast("Окошко поверх приложений работает на Android 8 и новее")
        } catch (e: Exception) { toast("Не удалось открыть окошко") }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (ytMode && webKind == "yt") pip()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        topBar.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
    }

    private fun reorder(a: Int, b: Int) {
        val cur = tab ?: return
        val m = shown.map { it.id }.toMutableList()
        val x = m.removeAt(a)
        m.add(b, x)
        pls[cur] = m
        savePls(); refreshList()
    }

    private fun findDups() {
        val groups = HashMap<String, MutableList<Track>>()
        for (t in all) groups.getOrPut(t.title.trim().lowercase() + "|" + t.artist.trim().lowercase()) { mutableListOf() }.add(t)
        val dups = groups.values.filter { it.size > 1 }
        if (dups.isEmpty()) { toast("Дубликатов не найдено"); return }
        dupAll.clear(); sel.clear()
        for (g in dups) {
            val sorted = g.sortedBy { it.id }
            for (t in sorted) dupAll.add(t.id)
            for (t in sorted.drop(1)) sel.add(t.id)
        }
        fKind = "dup"; fVal = "Дубликаты"; tab = null; selecting = true
        refreshTabs(); refreshList(); refreshTop()
        toast("Лишних копий: " + sel.size + ". Проверьте и нажмите на корзину")
    }

    private fun nowPlaying() {
        val d = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val aura = Aura(this).apply { color = c1; baseColor = base }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(16), dp(24), dp(24)) }
        val heart = Ic(this, P.HEART, grey, 0.6f).apply { setOnClickListener { player?.currentMediaItem?.mediaId?.toLongOrNull()?.let { toggleFav(it) } } }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(Ic(this, P.DOWN, white, 0.7f).apply { setOnClickListener { d.dismiss() } }, LinearLayout.LayoutParams(dp(46), dp(46)))
        top.addView(TextView(this).apply { text = "Сейчас играет"; textSize = 14f; setTextColor(grey); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(heart, LinearLayout.LayoutParams(dp(46), dp(46)))
        col.addView(top)
        val side = minOf(resources.displayMetrics.widthPixels - dp(48), (resources.displayMetrics.heightPixels * 0.42f).toInt())
        val cover = FrameLayout(this).apply { background = rr(mix(c1, 0.55f), 18); clipToOutline = true; elevation = dp(20).toFloat() }
        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        val note = Ic(this, P.NOTE, Color.argb(200, 255, 255, 255), 0.4f)
        cover.addView(img, FrameLayout.LayoutParams(-1, -1))
        cover.addView(note, FrameLayout.LayoutParams(-1, -1))
        col.addView(cover, LinearLayout.LayoutParams(side, side).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(20) })
        val title = TextView(this).apply { textSize = 24f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); setSingleLine(); ellipsize = TextUtils.TruncateAt.END; gravity = Gravity.CENTER }
        val artist = TextView(this).apply { textSize = 15f; setTextColor(grey); setSingleLine(); ellipsize = TextUtils.TruncateAt.END; gravity = Gravity.CENTER }
        col.addView(title, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(28) })
        col.addView(artist, LinearLayout.LayoutParams(-1, -2))
        val cur = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(grey) }
        val dur = TextView(this).apply { text = "0:00"; textSize = 12f; setTextColor(grey) }
        val sk = SeekBar(this).apply {
            progressTintList = ColorStateList.valueOf(c1)
            thumbTintList = ColorStateList.valueOf(white)
            progressBackgroundTintList = ColorStateList.valueOf(faint)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, u: Boolean) {}
                override fun onStartTrackingTouch(s: SeekBar) { npDrag = true }
                override fun onStopTrackingTouch(s: SeekBar) { npDrag = false; player?.seekTo(s.progress * 1000L) }
            })
        }
        val sr = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        sr.addView(cur)
        sr.addView(sk, LinearLayout.LayoutParams(0, -2, 1f))
        sr.addView(dur)
        col.addView(sr, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(18) })
        val shuf = Ic(this, P.SHUFFLE, grey, 0.55f).apply { setOnClickListener { player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } } }
        val prev = Ic(this, P.PREV, white, 0.6f).apply { setOnClickListener { player?.seekToPrevious() } }
        val play = Ic(this, P.PLAY, base, 0.55f).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(white) }
            setOnClickListener { player?.let { if (it.isPlaying) it.pause() else it.play() } }
        }
        val next = Ic(this, P.NEXT, white, 0.6f).apply { setOnClickListener { player?.seekToNext() } }
        val rep = Ic(this, P.REPEAT, grey, 0.55f).apply {
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
        for ((v, sz) in listOf(Pair<View, Int>(shuf, 48), Pair<View, Int>(prev, 56), Pair<View, Int>(play, 70), Pair<View, Int>(next, 56), Pair<View, Int>(rep, 48)))
            row.addView(v, LinearLayout.LayoutParams(dp(sz), dp(sz)).apply { leftMargin = dp(8); rightMargin = dp(8) })
        col.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })

        var shown0: android.graphics.Bitmap? = null
        npRefresh = {
            val p = player
            if (p != null) {
                val m = p.currentMediaItem
                title.text = m?.mediaMetadata?.title ?: "Выберите трек"
                artist.text = m?.mediaMetadata?.artist ?: ""
                play.setIcon(if (p.isPlaying) P.PAUSE else P.PLAY)
                shuf.tint(if (p.shuffleModeEnabled) c1 else grey)
                rep.setIcon(if (p.repeatMode == Player.REPEAT_MODE_ONE) P.REPEAT_ONE else P.REPEAT)
                rep.tint(if (p.repeatMode != Player.REPEAT_MODE_OFF) c1 else grey)
                heart.tint(if (favs.contains(m?.mediaId?.toLongOrNull() ?: -1L)) c1 else grey)
                aura.active = p.isPlaying
                if (!npDrag && p.duration > 0) {
                    sk.max = (p.duration / 1000).toInt()
                    sk.progress = (p.currentPosition / 1000).toInt()
                    cur.text = fmt(p.currentPosition)
                    dur.text = fmt(p.duration)
                }
                val b = coverBmp
                if (b !== shown0) {
                    shown0 = b
                    if (b != null) { img.setImageBitmap(b); note.visibility = View.GONE } else { img.setImageDrawable(null); note.visibility = View.VISIBLE }
                }
            }
        }
        val gd = android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                val dx = e2.x - (e1?.x ?: e2.x)
                val dy = e2.y - (e1?.y ?: e2.y)
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy) && kotlin.math.abs(dx) > dp(80)) {
                    if (dx < 0) player?.seekToNext() else player?.seekToPrevious()
                    return true
                }
                if (dy > dp(120)) { d.dismiss(); return true }
                return false
            }
        })
        val frame = FrameLayout(this)
        frame.addView(aura, FrameLayout.LayoutParams(-1, -1))
        frame.addView(col, FrameLayout.LayoutParams(-1, -1))
        frame.setOnTouchListener { _, ev -> gd.onTouchEvent(ev); true }
        d.setContentView(frame)
        d.setOnDismissListener { npRefresh = null }
        d.show()
        npRefresh?.invoke()
    }

    private fun saveDaily() {
        val o = JSONObject()
        for ((k, v) in daily) o.put(k, v)
        prefs.edit().putString("daily", o.toString()).apply()
    }

    private fun fadeTick() {
        val p = player
        if (p != null) {
            if (fadeSec <= 0) { if (p.volume != 1f) p.volume = 1f }
            else if (p.isPlaying && p.duration > 0) {
                val f = fadeSec * 1000f
                val pos = p.currentPosition.toFloat()
                val left = p.duration - pos
                p.volume = minOf(1f, pos / f, left / f).coerceIn(0.03f, 1f)
            }
        }
        ui.postDelayed({ fadeTick() }, 120)
    }

    private fun fadeSheet() {
        sheet("Плавный переход", listOf(0, 2, 4, 6, 8).map { sec ->
            row(P.BOLT, if (sec == 0) "Выключен" else sec.toString() + " сек") {
                fadeSec = sec; prefs.edit().putInt("fade", sec).apply()
                toast(if (sec == 0) "Плавный переход выключен" else "Переход: " + sec + " сек")
            }
        })
    }

    private fun toggleSkip() {
        skipSil = !skipSil
        prefs.edit().putBoolean("skip", skipSil).apply()
        Audio.exo?.setSkipSilenceEnabled(skipSil)
        toast(if (skipSil) "Тишина будет пропускаться" else "Пропуск тишины выключен")
    }

    private fun animateSwitch() {
        (coverImg.parent as? View)?.let { it.scaleX = 0.88f; it.scaleY = 0.88f; it.alpha = 0.3f; it.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(380).start() }
        nowTitle.translationX = dp(36).toFloat(); nowTitle.alpha = 0f
        nowTitle.animate().translationX(0f).alpha(1f).setDuration(300).start()
        nowArtist.alpha = 0f
        nowArtist.animate().alpha(1f).setDuration(450).start()
    }

    private fun hours(ms: Long): String = String.format("%d ч %02d мин", ms / 3600000, (ms / 60000) % 60)

    private fun recap() {
        val fk = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val days = ArrayList<Long>()
        val labels = ArrayList<String>()
        var week = 0L
        for (i in 6 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val ms = daily[fk.format(c.time)] ?: 0L
            days.add(ms); week += ms
            labels.add(c.get(java.util.Calendar.DAY_OF_MONTH).toString())
        }
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
        val yearMs = daily.filter { it.key.startsWith(year) }.values.sum()
        val topTrack = plays.entries.maxByOrNull { it.value }?.let { e -> all.find { it.id == e.key }?.title }
        val byArtist = HashMap<String, Int>()
        for ((id, n) in plays) all.find { it.id == id }?.let { if (it.artist.isNotEmpty()) byArtist[it.artist] = (byArtist[it.artist] ?: 0) + n }
        val topArtist = byArtist.entries.maxByOrNull { it.value }?.key
        val box = sheetBox("Итоги")
        fun line(t: String) = box.addView(TextView(this).apply { text = t; textSize = 16f; setTextColor(white); setPadding(dp(14), dp(4), dp(14), dp(4)) })
        line("За 7 дней: " + hours(week))
        line("За " + year + " год: " + hours(yearMs))
        line("Любимый трек: " + (topTrack ?: "пока нет"))
        line("Любимый исполнитель: " + (topArtist ?: "пока нет"))
        val bars = LinearLayout(this).apply { gravity = Gravity.BOTTOM; setPadding(dp(14), dp(14), dp(14), 0) }
        val mx = maxOf(1L, days.maxOrNull() ?: 1L)
        for (i in days.indices) {
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL }
            col.addView(View(this).apply { background = rr(c1, 4) }, LinearLayout.LayoutParams(dp(18), maxOf(dp(4), (dp(90) * days[i] / mx).toInt())))
            col.addView(TextView(this).apply { text = labels[i]; textSize = 11f; setTextColor(grey); gravity = Gravity.CENTER })
            bars.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        }
        box.addView(bars, LinearLayout.LayoutParams(-1, -2))
        present(android.app.Dialog(this), box)
    }

    private fun backup() {
        val o = JSONObject()
        for ((k, v) in prefs.all) o.put(k, v)
        val text = o.toString()
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val cv = android.content.ContentValues()
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "player-backup.json")
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
                val u = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                if (u != null) {
                    contentResolver.openOutputStream(u)?.use { it.write(text.toByteArray()) }
                    toast("Копия сохранена: Загрузки, player-backup.json")
                    return
                }
            }
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Резервная копия"))
        } catch (e: Exception) { toast("Не удалось сохранить копию") }
    }

    private fun restore() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), 78)
    }

    private fun applyBackup(u: android.net.Uri) {
        try {
            val txt = contentResolver.openInputStream(u)?.bufferedReader()?.use { it.readText() } ?: return
            val o = JSONObject(txt)
            val e = prefs.edit()
            for (k in o.keys()) {
                val v = o.get(k)
                if (k == "last" || k == "pos") { if (v is Number) e.putLong(k, v.toLong()); continue }
                when (v) {
                    is Int -> e.putInt(k, v)
                    is Long -> e.putLong(k, v)
                    is Boolean -> e.putBoolean(k, v)
                    is String -> e.putString(k, v)
                }
            }
            e.apply()
            toast("Копия восстановлена")
            recreate()
        } catch (ex: Exception) { toast("Не удалось прочитать файл") }
    }

    private fun httpGet(u: String): String {
        val c = java.net.URL(u).openConnection() as java.net.HttpURLConnection
        c.setRequestProperty("User-Agent", "PlayerApp/1.0")
        c.connectTimeout = 15000
        c.readTimeout = 20000
        return try { c.inputStream.bufferedReader().use { it.readText() } } finally { c.disconnect() }
    }

    private fun js(o: JSONObject, k: String): String = if (o.isNull(k)) "" else o.optString(k, "").trim('[', ']', '"')

    private fun download(url: String, name: String) {
        try {
            val safe = name.filter { it.isLetterOrDigit() || " .-_()".contains(it) }.ifEmpty { "track.mp3" }
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val r = android.app.DownloadManager.Request(android.net.Uri.parse(url))
                .setTitle(safe)
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_MUSIC, "FreeMusic/" + safe)
                .addRequestHeader("User-Agent", "PlayerApp/1.0")
            r.allowScanningByMediaScanner()
            dm.enqueue(r)
            toast("Скачивание: " + safe)
        } catch (e: Exception) { toast("Не удалось начать скачивание") }
    }

    private fun onlineSheet() {
        sheet("Бесплатная музыка", listOf(
            row(P.SEARCH, "Поиск в Internet Archive") { ask("Что искать?", "") { searchIA(it) } },
            row(P.SEARCH, "Поиск в Openverse (CC-музыка)") { ask("Что искать?", "") { searchOV(it) } },
            row(P.GLOBE, "Free Music Archive (сайт)") { openWeb("br"); web?.loadUrl("https://freemusicarchive.org") }
        ))
    }

    private fun searchIA(q: String) {
        toast("Ищу…")
        Thread {
            try {
                val query = "(" + q + ") AND mediatype:audio AND (licenseurl:*creativecommons* OR licenseurl:*publicdomain*)"
                val j = JSONObject(httpGet("https://archive.org/advancedsearch.php?q=" + android.net.Uri.encode(query) + "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=creator&rows=20&output=json"))
                val docs = j.getJSONObject("response").getJSONArray("docs")
                val rows = ArrayList<Triple<String, String, () -> Unit>>()
                for (k in 0 until docs.length()) {
                    val o = docs.getJSONObject(k)
                    val id = o.getString("identifier")
                    val cr = js(o, "creator")
                    rows.add(row(P.FOLDER, js(o, "title").ifEmpty { id } + (if (cr.isNotEmpty()) " — " + cr else "")) { iaFiles(id) })
                }
                runOnUiThread { if (rows.isEmpty()) toast("Ничего не найдено") else sheet("Internet Archive", rows) }
            } catch (e: Exception) { runOnUiThread { toast("Нет связи или ошибка поиска") } }
        }.start()
    }

    private fun iaFiles(id: String) {
        toast("Загружаю список…")
        Thread {
            try {
                val files = JSONObject(httpGet("https://archive.org/metadata/" + id)).getJSONArray("files")
                val rows = ArrayList<Triple<String, String, () -> Unit>>()
                for (k in 0 until files.length()) {
                    val n = files.getJSONObject(k).optString("name", "")
                    if (n.lowercase().endsWith(".mp3") && rows.size < 40) {
                        rows.add(row(P.DOWNLOAD, n.substringAfterLast('/')) { download("https://archive.org/download/" + id + "/" + android.net.Uri.encode(n, "/"), n.substringAfterLast('/')) })
                    }
                }
                runOnUiThread { if (rows.isEmpty()) toast("В этой записи нет mp3") else sheet("Выберите файл", rows) }
            } catch (e: Exception) { runOnUiThread { toast("Не удалось получить список файлов") } }
        }.start()
    }

    private fun searchOV(q: String) {
        toast("Ищу…")
        Thread {
            try {
                val res = JSONObject(httpGet("https://api.openverse.org/v1/audio/?q=" + android.net.Uri.encode(q) + "&page_size=20")).getJSONArray("results")
                val rows = ArrayList<Triple<String, String, () -> Unit>>()
                for (k in 0 until res.length()) {
                    val o = res.getJSONObject(k)
                    val url = js(o, "url")
                    if (url.isEmpty()) continue
                    val title = js(o, "title").ifEmpty { "track" }
                    val ext = js(o, "filetype").ifEmpty { "mp3" }
                    val lic = "CC " + js(o, "license").uppercase() + " " + js(o, "license_version")
                    rows.add(row(P.DOWNLOAD, title + " · " + lic) { download(url, title + "." + ext) })
                }
                runOnUiThread { if (rows.isEmpty()) toast("Ничего не найдено") else sheet("Openverse", rows) }
            } catch (e: Exception) { runOnUiThread { toast("Нет связи или ошибка поиска") } }
        }.start()
    }

    private fun rebuild() { coverId = -2L; build(); refreshTabs(); refreshList(); updateNow() }

    private fun chip(t: String, on: Boolean, f: () -> Unit) = TextView(this).apply {
        text = t; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (on) onAcc else white)
        setPadding(dp(16), dp(8), dp(16), dp(8))
        background = rr(if (on) c1 else card, 20)
        setOnClickListener { f() }
    }

    private fun build() {
        val th = themes[ti]
        val cu = ti == themes.size - 1
        c1 = if (cu) customAcc else Color.parseColor(th.acc)
        base = if (cu) customBg else Color.parseColor(th.bg)
        dark = if (cu) androidx.core.graphics.ColorUtils.calculateLuminance(customBg) < 0.5 else th.dark
        onAcc = if (androidx.core.graphics.ColorUtils.calculateLuminance(c1) > 0.45) Color.BLACK else Color.WHITE
        white = Color.parseColor(if (dark) "#FFFFFF" else "#191919")
        grey = Color.parseColor(if (dark) "#B3B3B3" else "#6B6B6B")
        card = if (dark) mix(Color.WHITE, 0.12f) else mix(Color.BLACK, 0.08f)
        faint = if (dark) Color.argb(70, 255, 255, 255) else Color.argb(50, 0, 0, 0)
        panelCol = if (dark) mix(Color.WHITE, 0.07f) else Color.WHITE
        val top = mix(c1, if (!dark) 0.30f else if (th.bg == "#000000") 0.18f else 0.38f)
        window.statusBarColor = top
        window.navigationBarColor = panelCol
        window.decorView.systemUiVisibility = if (dark) 0 else (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 0x10)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, base, base))
        }
        topBar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(8), dp(8), dp(4)) }
        rootV = root
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
        box.setOnClickListener { nowPlaying() }
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
            setOnTouchListener { _, ev ->
                val cur = tab
                val inPl = cur != null && cur != FAV && cur != REC && cur != TOP && cur != HIST && !selecting
                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (inPl && ev.x > width - dp(72)) {
                            val i = pointToPosition(ev.x.toInt(), ev.y.toInt()) - headerViewsCount
                            if (i >= 0 && i < shown.size) { dragIdx = i; requestDisallowInterceptTouchEvent(true); true } else false
                        } else false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (dragIdx >= 0) {
                            val i = pointToPosition(width / 2, ev.y.toInt()) - headerViewsCount
                            if (i >= 0 && i < shown.size && i != dragIdx) { reorder(dragIdx, i); dragIdx = i }
                            true
                        } else false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { val was = dragIdx >= 0; dragIdx = -1; trackAdapter.notifyDataSetChanged(); was }
                    else -> false
                }
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
            progressBackgroundTintList = ColorStateList.valueOf(faint)
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
        playIc = Ic(this, P.PLAY, base, 0.55f).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(white) }
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
        val row2 = LinearLayout(this).apply { gravity = Gravity.CENTER }
        val back10 = Ic(this, P.REPLAY, grey, 0.6f).apply { setOnClickListener { player?.let { it.seekTo(maxOf(0L, it.currentPosition - 10000L)) } } }
        heartIc = Ic(this, P.HEART, grey, 0.6f).apply { setOnClickListener { player?.currentMediaItem?.mediaId?.toLongOrNull()?.let { toggleFav(it) } } }
        speedTv = TextView(this).apply { text = "1×"; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; setTextColor(grey); gravity = Gravity.CENTER; setOnClickListener { speedDialog() } }
        val timer = Ic(this, P.TIMER, grey, 0.6f).apply { setOnClickListener { sleepDialog() } }
        val fwd10 = Ic(this, P.FORWARD, grey, 0.6f).apply { setOnClickListener { player?.let { it.seekTo(it.currentPosition + 10000L) } } }
        for (v in listOf<View>(back10, heartIc, speedTv, timer, fwd10)) row2.addView(v, LinearLayout.LayoutParams(dp(52), dp(44)))
        panel.addView(row2, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        root.addView(panel, LinearLayout.LayoutParams(-1, -2))
        panelV = panel
        for ((k, w) in webs) { (w.parent as? ViewGroup)?.removeView(w); root.addView(w, LinearLayout.LayoutParams(-1, 0, 1f)); w.visibility = if (ytMode && k == webKind) View.VISIBLE else View.GONE }
        listView.visibility = if (ytMode) View.GONE else View.VISIBLE
        panel.visibility = if (ytMode) View.GONE else View.VISIBLE
        setContentView(root)
        refreshTop()
    }
}
