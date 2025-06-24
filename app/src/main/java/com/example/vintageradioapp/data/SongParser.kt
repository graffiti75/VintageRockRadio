package com.example.vintageradioapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SongParser(private val context: Context) {

    suspend fun parseSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        try {
            context.assets.open("ids.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        val parts = line.split(";")
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
        } catch (e: Exception) {
            // Log error or throw a custom exception
            e.printStackTrace()
        }
        songs
    }
}
