package com.example.vintageradioapp.data // Updated package name

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongParser(private val context: Context) {

    suspend fun parseSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        try {
            context.assets.open("ids.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { currentLine ->
                            val parts = currentLine.split(";")
                            if (parts.size == 5) {
                                songs.add(
                                    Song(
                                        decade = parts[0].trim(),
                                        year = parts[1].trim(),
                                        band = parts[2].trim(),
                                        songTitle = parts[3].trim(),
                                        youtubeId = parts[4].trim()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, throw a custom exception or communicate error upwards
        }
        songs
    }
}
