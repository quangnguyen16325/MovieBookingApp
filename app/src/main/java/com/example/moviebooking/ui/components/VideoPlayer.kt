package com.example.moviebooking.ui.components

import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.regex.Pattern

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Trạng thái để theo dõi lỗi
    var isVideoUnavailable by remember { mutableStateOf(false) }

    // Trích xuất video ID từ URL YouTube
    val videoId = extractYouTubeVideoId(videoUrl)

    // Nếu videoId không hợp lệ, hiển thị thông báo lỗi ngay lập tức
    if (videoId == null) {
        VideoErrorView(modifier = modifier)
        return
    }

    Box {
        AndroidView(
            factory = { context ->
                YouTubePlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    enableAutomaticInitialization = false // Kiểm soát khởi tạo thủ công
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                        ) {
                            // Khi video không khả dụng, cập nhật trạng thái và hiển thị thông báo
                            isVideoUnavailable = true
                            val errorMessage = when (error) {
                                PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "Video not found"
                                PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> "Video cannot be embedded"
                                else -> "Video not available"
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    })
                    // Liên kết vòng đời với LifecycleOwner
                    lifecycleOwner.lifecycle.addObserver(this)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f), // Tỷ lệ khung hình 16:9
            onRelease = { playerView ->
                // Giải phóng tài nguyên khi Composable bị hủy
                playerView.release()
            }
        )

        // Hiển thị giao diện lỗi nếu video không khả dụng
        if (isVideoUnavailable) {
            VideoErrorView(modifier = modifier)
        }
    }
}

// Hàm trích xuất video ID từ URL YouTube
private fun extractYouTubeVideoId(url: String): String? {
    val youtubeRegex = Pattern.compile(
        "(?:(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/))([\\w-]{11})"
    )
    val matcher = youtubeRegex.matcher(url)
    return if (matcher.find()) {
        matcher.group(1) // Trả về video ID
    } else {
        null
    }
}

// Composable để hiển thị thông báo lỗi
@Composable
private fun VideoErrorView(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Video not available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}