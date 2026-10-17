package com.shakhawat.ghhumbhango.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shakhawat.ghhumbhango.data.local.dao.JourneyHistoryDao
import com.shakhawat.ghhumbhango.data.local.dao.RoutePresetDao
import com.shakhawat.ghhumbhango.data.local.dao.SavedDestinationDao
import com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity
import com.shakhawat.ghhumbhango.data.local.entity.RoutePresetEntity
import com.shakhawat.ghhumbhango.data.local.entity.SavedDestinationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SavedDestinationEntity::class,
        JourneyHistoryEntity::class,
        RoutePresetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class GhumBhangoDatabase : RoomDatabase() {

    abstract fun savedDestinationDao(): SavedDestinationDao
    abstract fun journeyHistoryDao(): JourneyHistoryDao
    abstract fun routePresetDao(): RoutePresetDao

    companion object {
        @Volatile
        private var INSTANCE: GhumBhangoDatabase? = null

        fun getInstance(context: Context): GhumBhangoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GhumBhangoDatabase::class.java,
                    "ghumbhango.db"
                )
                    .addCallback(PrepopulateCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val defaultPrepopulatedStops = listOf(
    SavedDestinationEntity(
        name = "ফার্মগেট বাস স্টপ",
        latitude = 23.7570,
        longitude = 90.3894,
        address = "তেজগাঁও, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "শাহবাগ মোড়",
        latitude = 23.7384,
        longitude = 90.3957,
        address = "শাহবাগ, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "মিরপুর ১০ গোলচত্বর",
        latitude = 23.8069,
        longitude = 90.3687,
        address = "মিরপুর, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "বিমানবন্দর রেলওয়ে স্টেশন",
        latitude = 23.8519,
        longitude = 90.4079,
        address = "উত্তরা, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "মতিঝিল শাপলা চত্বর",
        latitude = 23.7289,
        longitude = 90.4194,
        address = "মতিঝিল, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "মহাখালী বাস টার্মিনাল",
        latitude = 23.7776,
        longitude = 90.4005,
        address = "মহাখালী, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "গাবতলী বাস টার্মিনাল",
        latitude = 23.7820,
        longitude = 90.3458,
        address = "মিরপুর, গাবতলী, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "সায়েদাবাদ বাস টার্মিনাল",
        latitude = 23.7138,
        longitude = 90.4285,
        address = "যাত্রাবাড়ী, সায়েদাবাদ, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "উত্তরা আজমপুর বাসস্টপ",
        latitude = 23.8687,
        longitude = 90.3986,
        address = "সেক্টর ৩, উত্তরা, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "ধানমন্ডি ৩২ (রাসেল স্কয়ার)",
        latitude = 23.7516,
        longitude = 90.3783,
        address = "মিরপুর রোড, ধানমন্ডি, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "কুড়িল বিশ্বরোড",
        latitude = 23.8166,
        longitude = 90.4217,
        address = "প্রগতি সরণি, কুড়িল, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "কুড়িল বিশ্বরোড বাস স্টেশন",
        latitude = 23.8166,
        longitude = 90.4217,
        address = "কুড়িল বিশ্বরোড, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "খিলক্ষেত বাস স্টপ",
        latitude = 23.8293,
        longitude = 90.4203,
        address = "খিলক্ষেত, ঢাকা",
        isFavorite = true
    ),
    SavedDestinationEntity(
        name = "কমলাপুর প্রধান রেলওয়ে স্টেশন",
        latitude = 23.7317,
        longitude = 90.4258,
        address = "কমলাপুর, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "বনানী কাকলী মোড়",
        latitude = 23.7937,
        longitude = 90.4042,
        address = "বনানী, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "নতুন বাজার বাস স্টপ",
        latitude = 23.7979,
        longitude = 90.4235,
        address = "ভাটারা, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "রামপুরা ব্রিজ",
        latitude = 23.7634,
        longitude = 90.4199,
        address = "রামপুরা, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "মৌচাক মোড়",
        latitude = 23.7460,
        longitude = 90.4120,
        address = "মালিবাগ, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "গুলিস্তান জিরো পয়েন্ট",
        latitude = 23.7265,
        longitude = 90.4125,
        address = "গুলিস্তান, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "যাত্রাবাড়ী মোড়",
        latitude = 23.7104,
        longitude = 90.4348,
        address = "যাত্রাবাড়ী, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "মোহাম্মদপুর বাসস্ট্যান্ড",
        latitude = 23.7553,
        longitude = 90.3587,
        address = "মোহাম্মদপুর, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "শ্যামলী স্কয়ার",
        latitude = 23.7716,
        longitude = 90.3638,
        address = "মিরপুর রোড, শ্যামলী, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "কল্যাণপুর বাস স্টপ",
        latitude = 23.7797,
        longitude = 90.3582,
        address = "কল্যাণপুর, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "উত্তরা হাউজবিল্ডিং",
        latitude = 23.8738,
        longitude = 90.3965,
        address = "সেক্টর ৭, উত্তরা, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "আব্দুল্লাহপুর বাস টার্মিনাল",
        latitude = 23.8841,
        longitude = 90.3976,
        address = "উত্তরা, আব্দুল্লাহপুর, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "টঙ্গী স্টেশন রোড",
        latitude = 23.8997,
        longitude = 90.4042,
        address = "টঙ্গী, গাজীপুর",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "গাজীপুর চান্দনা চৌরাস্তা",
        latitude = 23.9999,
        longitude = 90.3797,
        address = "গাজীপুর",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "সাভার বাস স্ট্যান্ড",
        latitude = 23.8443,
        longitude = 90.2588,
        address = "সাভার, ঢাকা",
        isFavorite = false
    ),
    SavedDestinationEntity(
        name = "ময়মনসিংহ মাসকান্দা বাস টার্মিনাল",
        latitude = 24.7471,
        longitude = 90.4203,
        address = "ময়মনসিংহ",
        isFavorite = false
    )
)

val defaultPrepopulatedPresets = listOf(
    RoutePresetEntity(
        title = "দৈনিক অফিস যাত্রা",
        destinationName = "ফার্মগেট বাস স্টপ",
        destinationAddress = "তেজগাঁও, ঢাকা",
        latitude = 23.7570,
        longitude = 90.3894,
        alarmDistanceMeters = 500,
        category = "WORK"
    ),
    RoutePresetEntity(
        title = "বাসা ফেরা",
        destinationName = "মিরপুর ১০ গোলচত্বর",
        destinationAddress = "মিরপুর, ঢাকা",
        latitude = 23.8069,
        longitude = 90.3687,
        alarmDistanceMeters = 500,
        category = "HOME"
    ),
    RoutePresetEntity(
        title = "বিশ্ববিদ্যালয় / ক্যাম্পাস",
        destinationName = "শাহবাগ মোড়",
        destinationAddress = "শাহবাগ, ঢাকা",
        latitude = 23.7384,
        longitude = 90.3957,
        alarmDistanceMeters = 200,
        category = "STUDY"
    )
)

internal class PrepopulateCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val dbInstance = GhumBhangoDatabase.getInstance(context)
            dbInstance.savedDestinationDao().insertAll(defaultPrepopulatedStops)
            dbInstance.routePresetDao().insertAll(defaultPrepopulatedPresets)
        }
    }
}
