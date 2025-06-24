package com.example.vintageradio.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class SongParser(private val context: Context) {

    fun parseSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            context.assets.open("ids.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val parts = line!!.split(";")
                        if (parts.size == 5) {
                            songs.add(
                                Song(
                                    decade = parts[0],
                                    year = parts[1],
                                    band = parts[2],
                                    songTitle = parts[3],
                                    youtubeId = parts[4]
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exception, perhaps return empty list or throw custom exception
        }
        return songs
    }
}
