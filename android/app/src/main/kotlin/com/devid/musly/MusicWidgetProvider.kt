package com.devid.musly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import android.util.Log
import java.util.concurrent.TimeUnit

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val TAG = "MusicWidgetProvider"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.music_widget)

            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_cover, pendingIntent)
            views.setOnClickPendingIntent(R.id.content_layout, pendingIntent)

            views.setOnClickPendingIntent(R.id.widget_play_pause, getPendingIntent(context, "ACTION_PLAY_PAUSE"))
            views.setOnClickPendingIntent(R.id.widget_prev, getPendingIntent(context, "ACTION_PREV"))
            views.setOnClickPendingIntent(R.id.widget_next, getPendingIntent(context, "ACTION_NEXT"))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context, action.hashCode(), intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun formatTime(ms: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        fun updateWithMetadata(context: Context, metadata: MediaMetadataCompat?, playbackState: PlaybackStateCompat?) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.music_widget)
                
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_cover, pendingIntent)
                views.setOnClickPendingIntent(R.id.content_layout, pendingIntent)

                if (metadata != null) {
                    views.setTextViewText(R.id.widget_title, metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                    views.setTextViewText(R.id.widget_artist, metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                    
                    val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    val position = playbackState?.position ?: 0
                    
                    if (duration > 0) {
                        views.setProgressBar(R.id.widget_progress, 100, ((position * 100) / duration).toInt(), false)
                        views.setTextViewText(R.id.widget_time_current, formatTime(position))
                        views.setTextViewText(R.id.widget_time_total, formatTime(duration))
                    } else {
                        views.setProgressBar(R.id.widget_progress, 100, 0, false)
                        views.setTextViewText(R.id.widget_time_current, "0:00")
                        views.setTextViewText(R.id.widget_time_total, "0:00")
                    }

                    val art = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
                    if (art != null) {
                        views.setImageViewBitmap(R.id.widget_cover, art)
                    } else {
                        views.setImageViewResource(R.id.widget_cover, R.mipmap.ic_launcher)
                    }
                }

                val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
                views.setImageViewResource(R.id.widget_play_pause, 
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)

                views.setOnClickPendingIntent(R.id.widget_play_pause, getPendingIntent(context, "ACTION_PLAY_PAUSE"))
                views.setOnClickPendingIntent(R.id.widget_prev, getPendingIntent(context, "ACTION_PREV"))
                views.setOnClickPendingIntent(R.id.widget_next, getPendingIntent(context, "ACTION_NEXT"))
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        val musicServiceIntent = Intent(context, MusicService::class.java).apply {
            action = intent.action
        }
        
        when (intent.action) {
            "ACTION_PLAY_PAUSE", "ACTION_PREV", "ACTION_NEXT" -> {
                context.startService(musicServiceIntent)
            }
        }
    }
}
