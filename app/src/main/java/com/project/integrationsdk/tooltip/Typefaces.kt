package com.project.integrationsdk.tooltip


import android.content.Context
import android.graphics.Typeface
import android.util.LruCache

object Typefaces {
    private val FONT_CACHE = LruCache<String, Typeface>(4)

    operator fun get(c: Context, assetPath: String): Typeface? {
        synchronized(FONT_CACHE) {
            var typeface = FONT_CACHE.get(assetPath)
            if (typeface == null) {
                try {
                    typeface = Typeface.createFromAsset(c.assets, assetPath)
                    FONT_CACHE.put(assetPath, typeface)
                } catch (e: Exception) {
                    return null
                }
            }
            return typeface
        }
    }
}