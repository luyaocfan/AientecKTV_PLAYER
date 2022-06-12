package com.aientec.ktv_vod.module

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.core.database.getStringOrNull
import com.aientec.ktv_vod.structure.Singer
import com.aientec.structure.Album
import com.aientec.structure.Track

internal class KtvDatabaseReader private constructor() {

      companion object {
            private var instance: KtvDatabaseReader? = null

            private const val TAG: String = "DB"

            private val trackType: Array<Pair<Int, String>> = arrayOf(
                  Pair(0, "教育訓練"),
                  Pair(1, "國語"),
                  Pair(2, "台語"),
                  Pair(3, "粵語"),
                  Pair(4, "日語"),
                  Pair(5, "英語"),
                  Pair(6, "兒歌"),
                  Pair(8, "菲律賓"),
                  Pair(9, "韓語"),
                  Pair(10, "義大利"),
                  Pair(11, "西班牙"),
                  Pair(12, "法語"),
                  Pair(13, "印尼"),
                  Pair(14, "馬來西亞"),
                  Pair(16, "越南"),
                  Pair(17, "阿根廷"),
                  Pair(18, "客家"),
                  Pair(19, "舞曲"),
                  Pair(20, "戲曲"),
                  Pair(21, "音檢"),
                  Pair(22, "1次開機帶"),
                  Pair(23, "2次開機帶"),
                  Pair(24, "關機帶"),
                  Pair(25, "火警"),
            )

            fun getInstance(): KtvDatabaseReader {
                  if (instance == null)
                        instance = KtvDatabaseReader()
                  return instance!!
            }
      }

      private var database: SQLiteDatabase? = null

      private lateinit var singerList: List<String>


      fun openDatabase(filePath: String): Boolean {
            return try {
                  database = SQLiteDatabase.openDatabase(filePath, null, 0)
                  initDatabase()
                  true
            } catch (e: SQLiteException) {
                  e.printStackTrace()
                  false
            }
      }


      /**
       * TODO
       *
       * @param filterCondition Sqlite where command
       * @return list of tracks
       */
      fun getTracks(filterCondition: String? = null): List<Track>? {
            val db = database ?: return null

            val list: ArrayList<Track> = ArrayList()

            val cursor: Cursor = db.rawQuery(
                  "select Id, SongNo, SongName, Singer, Lyricist, Composer, SongFile, BPMClass,  (select name from t_track_type where Id = t_song.LangType) as Language, SongImg from t_song where Id>64" + if (filterCondition != null) " and ($filterCondition)" else "",
                  null
            )

//        Log.d("Trace", "Query count : ${cursor.count}")

            while (cursor.moveToNext()) {
                  list.add(
                        Track(
                              cursor.getInt(0),
                              cursor.getStringOrNull(1) ?: "",
                              cursor.getStringOrNull(2) ?: "",
                              cursor.getStringOrNull(3) ?: "",
                              cursor.getStringOrNull(4) ?: "",
                              cursor.getStringOrNull(5) ?: "",
                              cursor.getStringOrNull(8) ?: "",
                              cursor.getStringOrNull(9) ?: "",
                              Track.State.NONE,
                              "",
                              cursor.getStringOrNull(6) ?: "",
                              cursor.getInt(7)
                        )
                  )
            }

            Log.d(TAG, "List : ${list.size}")

            cursor.close()

            return list
      }

      @Deprecated("")
      fun getIdleTracks(): List<Track>? {
            val db = database ?: return null

            val list: ArrayList<Track> = ArrayList()

            val cursor: Cursor = db.rawQuery(
                  "select Id, SongFile, SongName, Singer, Lyricist, Composer  from t_song where Id<62",
                  null
            )

            val ignoreId: ArrayList<Int> = ArrayList<Int>().apply {
                  add(34)
                  add(38)
                  add(39)
            }

            while (cursor.moveToNext()) {
                  if (ignoreId.contains(cursor.getInt(0))) continue

                  list.add(
                        Track(
                              cursor.getInt(0),
                              cursor.getString(1),
                              cursor.getString(2),
                              cursor.getString(3),
                              cursor.getString(4),
                              cursor.getString(5)
                        )
                  )
            }

            cursor.close()

            return list
      }

      fun getAlbums(type: Int): List<Album>? {
            val db: SQLiteDatabase = database ?: return null

            val cursor: Cursor = db.rawQuery(
                  "select Id, SheetName, SheetTitle, SheetImg from t_songsheet where SheetType = $type",
                  null
            )

            val list: ArrayList<Album> = ArrayList()

            while (cursor.moveToNext()) {
                  list.add(Album().apply {
                        this.id = cursor.getInt(0)
                        this.name = cursor.getString(1)
                        this.title = cursor.getString(2)
                        this.cover = cursor.getString(3)
                  })
            }

            cursor.close()
            return list
      }

      fun getSingerList(): List<Singer>? {
            val db: SQLiteDatabase = database ?: return null

            val cursor: Cursor = db.rawQuery("select Id, SingerName from t_singer", null)

            val list: ArrayList<Singer> = ArrayList()

            while (cursor.moveToNext())
                  list.add(Singer(cursor.getInt(0), cursor.getString(1)))

            cursor.close()

            return list
      }

      fun searchSingers(type: Int, filterKey: String = "", filterType: Int = -1): List<Singer>? {
            val db: SQLiteDatabase = database ?: return null

            var whereCondition: String = "Sex = $type"

            if (!filterKey.isNullOrEmpty()) {
                  val fType: String = when (filterType) {
                        1 -> "Phonetic"
                        2 -> "Pinyin"
                        else -> "SingerName"
                  }
                  whereCondition += " and $fType like '${filterKey}%'"
            }

            val cursor =
                  db.rawQuery("select Id, SingerName from t_singer where $whereCondition", null)


            val list: ArrayList<Singer> = ArrayList()

            while (cursor.moveToNext())
                  list.add(Singer(cursor.getInt(0), cursor.getString(1)))


            cursor.close()

            return list
      }

      fun searchTracks(filterKey: String = "", filterType: Int = -1): List<Track>? {
            val db: SQLiteDatabase = database ?: return null

            val condition: String = if (!filterKey.isNullOrEmpty()) {
                  when (filterType) {
                        1 -> "where SongNo in (select SongNo from songcode where  CodeType like 'C01' and Code like '${filterKey}%' )"
                        2 -> "where SongNo in (select SongNo from songcode where  CodeType like 'C08' and Code like '${filterKey}%' )"
                        else -> "where Id > 62 and SongName like '${filterKey}%'"
                  }
            } else {
                  "where Id > 62"
            }

            val cursor = db.rawQuery(
                  "select Id, SongNo, SongName, Singer, Lyricist, Composer, SongFile, BPMClass, TImeLen from t_song $condition",
                  null
            )

            val list: ArrayList<Track> = ArrayList()

            while (cursor.moveToNext()) {
                  list.add(
                        Track(
                              cursor.getInt(0),
                              cursor.getString(1),
                              cursor.getString(2),
                              cursor.getString(3),
                              "",
                              "",
                              "",
                              "",
                              Track.State.NONE,
                              "",
                              cursor.getString(6),
                              cursor.getInt(7),
                              cursor.getInt(8)
                        )
                  )
            }

            cursor.close()

            return list
      }

      fun getSingerTracks(id: Int): List<Track>? {
            val db: SQLiteDatabase = database ?: return null

            val condition: String = "where SingerId = $id"

            val cursor = db.rawQuery(
                  "select Id, SongNo, SongName, Singer, Lyricist, Composer, SongFile, BPMClass, TImeLen from t_song $condition",
                  null
            )

            val list: ArrayList<Track> = ArrayList()

            while (cursor.moveToNext()) {
                  list.add(
                        Track(
                              cursor.getInt(0),
                              cursor.getString(1),
                              cursor.getString(2),
                              cursor.getString(3),
                              "",
                              "",
                              "",
                              "",
                              Track.State.NONE,
                              "",
                              cursor.getString(6),
                              cursor.getInt(7),
                              cursor.getInt(8)
                        )
                  )
            }

            cursor.close()

            return list
      }

      fun getAlbumTracks(id: Int): List<Track>? {
            val db: SQLiteDatabase = database ?: return null

            val cursor = db.rawQuery(
                  "select Id, SongNo, SongName, Singer, Lyricist, Composer, SongFile, BPMClass, TImeLen from t_song where Id in (select SongId from  t_songsheetlist where SheetId=$id)",
                  null
            )

            val list: ArrayList<Track> = ArrayList()

            while (cursor.moveToNext()) {
                  list.add(
                        Track(
                              cursor.getInt(0),
                              cursor.getString(1),
                              cursor.getString(2),
                              cursor.getString(3),
                              "",
                              "",
                              "",
                              "",
                              Track.State.NONE,
                              "",
                              cursor.getString(6),
                              cursor.getInt(7),
                              cursor.getInt(8)
                        )
                  )
            }

            cursor.close()

            return list
      }

      fun getTrackLists(): List<Album>? {
            val db: SQLiteDatabase = database ?: return null

            val list: ArrayList<Album> = ArrayList()

            val sql: String =
                  "select Id, SheetName, SheetTitle from t_songsheet"

            val cursor: Cursor = db.rawQuery(
                  sql, null
            )

            var subCursor: Cursor

            while (cursor.moveToNext()) {
                  val id: Int = cursor.getInt(0)

                  val name: String = cursor.getString(1)

                  list.add(Album(id, name).apply {
                        subCursor = db.rawQuery(
                              "select Id, SongNo, SongName, Singer, Lyricist, Composer, SongFile, BPMClass from t_song where Id in (select SongId from  t_songsheetlist where SheetId=$id)",
                              null
                        )

                        tracks = ArrayList()
                        while (subCursor.moveToNext()) {
                              (tracks as ArrayList<Track>).add(
                                    Track(
                                          subCursor.getInt(0),
                                          subCursor.getString(1),
                                          subCursor.getString(2),
                                          subCursor.getString(3),
                                          "",
                                          "",
                                          "",
                                          "",
                                          Track.State.NONE,
                                          "",
                                          subCursor.getString(6),
                                          subCursor.getInt(7)
                                    )
                              )
                        }

                        subCursor.close()
                  })

            }

            cursor.close()

            return list
      }

      fun getSearchKeys(key: String, size: Int, searchType: Int, keyType: Int): List<String>? {
            val db = database ?: return null

            val list: ArrayList<String> = ArrayList()

            val cursor: Cursor

            if (keyType == 3) return null

            if (searchType < 4) {
                  val fType: String = if (keyType == 1) "Phonetic" else "Pinyin"
                  cursor =
                        db.rawQuery(
                              "select distinct substr($fType,${size + 1},1) from t_singer where  $fType like  '${key}_%' and Sex = $searchType",
                              null
                        )
            } else {
                  Log.d(TAG, "cmd : $key, length : $size")

                  val fType: String = if (keyType == 1) "C01" else "C08"

                  cursor =
                        db.rawQuery(
                              "select distinct substr(Code,${size + 1},1) from songcode where CodeType like '$fType' and Code like  '${key}_%'",
                              null
                        )
            }

            while (cursor.moveToNext()) {
                  Log.d("Trace", "Key : ${cursor.getString(0)}")
                  if (cursor.getString(0).isNotBlank()) {

                        list.add(cursor.getString(0))
                  }
            }
            cursor.close()

            return list
      }

      private fun initDatabase() {
            val db = database ?: return

            singerList = ArrayList<String>().apply {

                  val cursor: Cursor =
                        db.rawQuery("select distinct singer from t_song where t_song.Id > 62", null)

                  while (cursor.moveToNext()) {
                        this.add(cursor.getString(0))
                  }

                  cursor.close()

                  val testCursor =
                        db.rawQuery("select Id, SingerName from t_singer where Sex = 2", null)

                  Log.d("Trace", "Test count : ${testCursor.count}")

                  testCursor.close()
            }

      }

}