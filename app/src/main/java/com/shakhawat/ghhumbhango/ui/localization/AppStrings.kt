package com.shakhawat.ghhumbhango.ui.localization

import androidx.compose.runtime.compositionLocalOf
import com.shakhawat.ghhumbhango.domain.model.AppLanguage

/**
 * Universal app strings interface supporting Bengali and English.
 */
interface StringsDefinition {
    // Navigation
    val navHome: String
    val navSaved: String
    val navHistory: String
    val navSettings: String

    // Common Buttons & Dialogs
    val ok: String
    val cancel: String
    val close: String
    val gotIt: String
    val confirm: String
    val back: String
    val delete: String
    val save: String

    // Settings Screen
    val settingsTitle: String
    val offlineMapTitle: String
    val offlineMapSubtitle: String
    val offlineMapDialogTitle: String
    val offlineMapDialogText: String

    val alarmSoundTitle: String
    val alarmSoundDialogTitle: String
    val soundDefault: String
    val soundBusHorn: String
    val soundGentleBell: String
    val soundTrainWhistle: String
    val soundCustom: String
    val chooseRingtone: String
    val chooseAudioFile: String
    val customAlarmNotSet: String
    val changeSound: String

    val defaultDistanceTitle: String
    fun defaultDistanceSubtitle(meters: Int): String
    val selectDistanceDialogTitle: String
    fun distanceMeters(meters: Int): String

    val adaptiveAlarmTitle: String
    val adaptiveAlarmSubtitle: String
    val adaptiveAlarmDialogTitle: String
    val adaptiveAlarmDialogText: String

    val locationSettingsTitle: String
    val locationSettingsSubtitle: String

    val batteryOptimizationTitle: String
    val batteryOptimizationSubtitle: String
    val batteryOptimizationDialogTitle: String
    val batteryOptimizationDialogText: String
    val goToSettingsBtn: String

    val languageTitle: String
    val languageSubtitle: String
    val selectLanguageDialogTitle: String

    val themeModeTitle: String
    fun themeModeSubtitle(mode: com.shakhawat.ghhumbhango.domain.model.AppThemeMode): String
    val themeModeDialogTitle: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String

    val aboutAppTitle: String
    val aboutAppSubtitle: String
    val aboutAppDialogTitle: String
    val aboutAppDialogText: String

    val privacyPolicyTitle: String
    val privacyPolicySubtitle: String
    val privacyPolicyDialogTitle: String
    val privacyPolicyDialogText: String

    val helpFeedbackTitle: String
    val helpFeedbackSubtitle: String
    val helpFeedbackDialogTitle: String
    val helpFeedbackDialogText: String

    // Home Screen
    val homeGreeting: String
    val appTagline: String
    val destinationCardTitle: String
    val searchPlaceholder: String
    val pickFromMap: String
    val selectedDestination: String
    val change: String
    val clear: String
    val alarmDistanceHeader: String
    val quickStopsHeader: String
    val startJourney: String
    val journeyActive: String
    val gpsActive: String

    // Set Alarm Screen
    val setAlarmTitle: String
    val distanceFromCurrentLocation: String
    val whenToAlarmHeader: String
    val alarmSoundHeader: String
    val startJourneyTrackingBtn: String

    // Destination Search Screen
    val searchDestinationTitle: String
    val searchInputHint: String
    val recentSearchesHeader: String
    val mapResultsHeader: String
    val popularStopsHeader: String
    val noResultsFound: String

    // History Screen
    val historyScreenTitle: String
    val tabHistory: String
    val tabPresets: String
    val tabSavedStops: String
    val restartJourney: String
    val clearAllHistory: String
    val addNewPreset: String
    val statusSuccess: String
    val statusCancelled: String
    val noHistoryFound: String
    val noPresetsFound: String
    val noSavedStopsFound: String

    // Active Journey & Alarm
    val destinationArrivedTitle: String
    val oneKmAlertTitle: String
    fun oneKmAlertContent(destinationName: String): String
    val remainingDistance: String
    val dismissAlarm: String
    val snoozeAlarm: String
    val alarmAndVibrationRinging: String
    val snoozeTwoMinutes: String
    fun remainingText(text: String): String

    // Common & Extra
    val yourLocation: String
    val currentLocation: String
    val defaultLocationUttara: String
    val destinationLabel: String
    val sourceLabel: String
    val recentJourneysTitle: String
    val noRecentJourneysTitle: String
    val noRecentJourneysSubtitle: String
    val startNewJourneyBtn: String
    val soundPlaying: String
    val menu: String
    val add: String

    // Filter Chips
    val filterAll: String
    val filterRecent: String
    val filterHome: String
    val filterWork: String
    val filterSaved: String

    // Map pick
    val pickOnMapTitle: String
    val pickOnMapSubtitle: String
    val confirmDestination: String
    val mapLayers: String

    // Tracking Metrics & Actions
    val estimatedTime: String
    val speed: String
    val endJourney: String
    val endJourneyConfirmTitle: String
    val endJourneyConfirmText: String
}

object BengaliStrings : StringsDefinition {
    override val navHome = "হোম"
    override val navSaved = "সেভড"
    override val navHistory = "ইতিহাস"
    override val navSettings = "সেটিংস"

    override val ok = "ঠিক আছে"
    override val cancel = "বাতিল"
    override val close = "বন্ধ করুন"
    override val gotIt = "বুঝেছি"
    override val confirm = "নিশ্চিত করুন"
    override val back = "ফিরে যান"
    override val delete = "মুছে ফেলুন"
    override val save = "সংরক্ষণ করুন"

    override val settingsTitle = "সেটিংস"
    override val offlineMapTitle = "অফলাইন ম্যাপ"
    override val offlineMapSubtitle = "ম্যাপ ডাউনলোড ও ব্যবস্থাপনা"
    override val offlineMapDialogTitle = "অফলাইন ম্যাপ ক্যাশ"
    override val offlineMapDialogText = "GhumBhango ইতিমধ্যে আপনার ব্রাউজ করা সমস্ত ম্যাপ টাইলস এবং বাংলাদেশের প্রধান বাস স্টপেজসমূহ অফলাইনে সেভ করে রাখে, যাতে ইন্টারনেট ছাড়াই ট্র্যাক করা যায়।"

    override val alarmSoundTitle = "অ্যালার্ম সাউন্ড"
    override val alarmSoundDialogTitle = "ডিফল্ট অ্যালার্ম সাউন্ড"
    override val soundDefault = "ডিফল্ট অ্যালার্ম"
    override val soundBusHorn = "বাস হর্ন ও মেলোডি"
    override val soundGentleBell = "মৃদু ঘণ্টার ধ্বনি"
    override val soundTrainWhistle = "ট্রেন হুইসেল"
    override val soundCustom = "কাস্টম অডিও / রিংটোন"
    override val chooseRingtone = "সিস্টেম রিংটোন"
    override val chooseAudioFile = "অডিও ফাইল (MP3/স্টোরেজ)"
    override val customAlarmNotSet = "ফোন থেকে অডিও বা রিংটোন বাছুন"
    override val changeSound = "পরিবর্তন"

    override val defaultDistanceTitle = "ডিফল্ট দূরত্ব"
    override fun defaultDistanceSubtitle(meters: Int) = "$meters মিটার আগে"
    override val selectDistanceDialogTitle = "ডিফল্ট দূরত্ব নির্বাচন করুন"
    override fun distanceMeters(meters: Int) = "$meters মিটার আগে"

    override val adaptiveAlarmTitle = "ধাপে ধাপে তীব্রতা বৃদ্ধি (Adaptive)"
    override val adaptiveAlarmSubtitle = "গন্তব্য যত কাছে আসবে সাউন্ড ও কম্পন তত বাড়বে"
    override val adaptiveAlarmDialogTitle = "স্মার্ট এডাপ্টিভ অ্যালার্ম"
    override val adaptiveAlarmDialogText = "এই ফিচারটি চালু রাখলে গন্তব্যের দূরত্বের উপর ভিত্তি করে অ্যালার্ম বাজবে:\n\n• গন্তব্যের শুরুতে (যেমন: ৪০০ মিটারে): মৃদু সাউন্ড ও হালকা কম্পন\n• মাঝামাঝি দূরত্বে (যেমন: ২০০-৩০০ মিটারে): মাঝারি তীব্রতা\n• খুব কাছে (১০০ মিটারের মধ্যে): সর্বোচ্চ তীব্রতায় সাউন্ড ও ভাইব্রেশন\n\nবন্ধ থাকলে সাধারণ নিয়ম অনুযায়ী শুরু থেকেই পূর্ণ তীব্রতায় অ্যালার্ম বাজবে।"

    override val locationSettingsTitle = "লোকেশন সেটিংস"
    override val locationSettingsSubtitle = "উচ্চ নির্ভুলতা (High Accuracy)"

    override val batteryOptimizationTitle = "ব্যাটারি অপটিমাইজেশন"
    override val batteryOptimizationSubtitle = "নির্দেশনা দেখুন"
    override val batteryOptimizationDialogTitle = "ব্যাটারি অপটিমাইজেশন"
    override val batteryOptimizationDialogText = "ব্যাকগ্রাউন্ডে ফোন লক থাকলেও যাতে নির্ভুল সময়ে অ্যালার্ম বাজে, সেজন্য ঘুমে ভাঙ্গো অ্যাপকে 'Unrestricted Battery' বা ব্যাটারি অপটিমাইজেশন মুক্ত রাখুন।"
    override val goToSettingsBtn = "সেটিংসে যান"

    override val languageTitle = "ভাষা"
    override val languageSubtitle = "বাংলা"
    override val selectLanguageDialogTitle = "ভাষা নির্বাচন করুন"

    override val themeModeTitle = "থিম মোড"
    override fun themeModeSubtitle(mode: com.shakhawat.ghhumbhango.domain.model.AppThemeMode): String = mode.getLocalizedName(AppLanguage.BANGLA)
    override val themeModeDialogTitle = "থিম নির্বাচন করুন"
    override val themeSystem = "সিস্টেম ডিফল্ট"
    override val themeLight = "লাইট মোড"
    override val themeDark = "ডার্ক মোড"

    override val aboutAppTitle = "অ্যাপ সম্পর্কে"
    override val aboutAppSubtitle = "ভার্সন ১.০.০"
    override val aboutAppDialogTitle = "ঘুমভাঙ্গো (GhumBhango)"
    override val aboutAppDialogText = "ভার্সন: ১.০.০\n\nবাস বা ট্রেনে ঘুমিয়ে গন্তব্য মিস হওয়ার ভয় আর নেই! ঘূমভাঙ্গো জিপিএস লোকেশন ট্র্যাক করে নির্দিষ্ট দূরত্বে পৌঁছে উচ্চ শব্দে অ্যালার্ম বাজিয়ে আপনাকে জাগিয়ে দেয়।\n\nডেভেলপার: Shakhawat Hossain"

    override val privacyPolicyTitle = "গোপনীয়তা নীতি"
    override val privacyPolicySubtitle = "তথ্য সংগ্রহ ও ব্যবহার"
    override val privacyPolicyDialogTitle = "গোপনীয়তা নীতি"
    override val privacyPolicyDialogText = "আপনার গোপনীয়তা আমাদের কাছে অত্যন্ত গুরুত্বপূর্ণ। GhumBhango শুধুমাত্র আপনার ফোন ডিভাইসে অ্যালার্ম বাজানোর জন্য লাইভ জিপিএস লোকেশন ব্যবহার করে। আপনার কোনো লোকেশন ডেটা সার্ভারে সংরক্ষণ বা শেয়ার করা হয় না।"

    override val helpFeedbackTitle = "সহায়তা ও ফিডব্যাক"
    override val helpFeedbackSubtitle = "যোগাযোগ করুন ও পরামর্শ দিন"
    override val helpFeedbackDialogTitle = "সহায়তা ও ফিডব্যাক"
    override val helpFeedbackDialogText = "আপনার যেকোনো মতামত, পরামর্শ বা সমস্যার কথা জানাতে আমাদের সাথে সরাসরি যোগাযোগ করুন।\n\nইমেইল: support@ghumbhango.app"

    override val homeGreeting = "যাত্রায় নির্ভার থাকুন"
    override val appTagline = "ঘুম আসুক, স্টপ মিস নয়!"
    override val destinationCardTitle = "গন্তব্য নির্বাচন করুন"
    override val searchPlaceholder = "কোথায় নামবেন? বাসস্টপ বা গন্তব্য খুঁজুন…"
    override val pickFromMap = "ম্যাপ থেকে স্থান নির্বাচন"
    override val selectedDestination = "নির্বাচিত গন্তব্য"
    override val change = "পরিবর্তন"
    override val clear = "মুছুন"
    override val alarmDistanceHeader = "অ্যালার্মের দূরত্ব"
    override val quickStopsHeader = "জনপ্রিয় ও সাম্প্রতিক স্টপেজ"
    override val startJourney = "যাত্রা শুরু করুন"
    override val journeyActive = "যাত্রা চলমান"
    override val gpsActive = "জিপিএস সক্রিয়"

    override val setAlarmTitle = "অ্যালার্ম নির্ধারণ"
    override val distanceFromCurrentLocation = "বর্তমান অবস্থান থেকে দূরত্ব"
    override val whenToAlarmHeader = "কত দূরে পৌঁছালে অ্যালার্ম বাজবে?"
    override val alarmSoundHeader = "অ্যালার্ম সাউন্ড"
    override val startJourneyTrackingBtn = "যাত্রা শুরু করুন"

    override val searchDestinationTitle = "গন্তব্য খুঁজুন"
    override val searchInputHint = "কোথায় যাবেন? লিখুন..."
    override val recentSearchesHeader = "সাম্প্রতিক অনুসন্ধান"
    override val mapResultsHeader = "মানচিত্রের ফলাফল"
    override val popularStopsHeader = "জনপ্রিয় স্টপেজ"
    override val noResultsFound = "কোনো ফলাফল পাওয়া যায়নি"

    override val historyScreenTitle = "যাত্রার ইতিহাস"
    override val tabHistory = "ইতিহাস"
    override val tabPresets = "রুট প্রিসেট"
    override val tabSavedStops = "সংরক্ষিত স্টপ"
    override val restartJourney = "আবার শুরু করুন"
    override val clearAllHistory = "ইতিহাস পরিষ্কার করুন"
    override val addNewPreset = "নতুন প্রিসেট যোগ করুন"
    override val statusSuccess = "সফল"
    override val statusCancelled = "বাতিল"
    override val noHistoryFound = "কোনো যাত্রার ইতিহাস নেই"
    override val noPresetsFound = "কোনো রুট প্রিসেট সংরক্ষিত নেই"
    override val noSavedStopsFound = "কোনো সংরক্ষিত স্টপ নেই"

    override val destinationArrivedTitle = "আপনার গন্তব্য এসে গেছে!"
    override val oneKmAlertTitle = "📍 গন্তব্য ১ কিমি দূরে!"
    override fun oneKmAlertContent(destinationName: String) = "আপনি $destinationName থেকে ১ কিমি দূরে আছেন। নামার প্রস্তুতি নিন।"
    override val remainingDistance = "অবশিষ্ট দূরত্ব"
    override val dismissAlarm = "অ্যালার্ম বন্ধ করুন"
    override val snoozeAlarm = "২ মিনিট স্নুজ"
    override val alarmAndVibrationRinging = "অ্যালার্ম এবং ভাইব্রেশন বাজছে"
    override val snoozeTwoMinutes = "২ মিনিট পরে আবার বাজান (স্নুজ)"
    override fun remainingText(text: String) = "আর $text"

    override val yourLocation = "আপনার অবস্থান"
    override val currentLocation = "বর্তমান লোকেশন"
    override val defaultLocationUttara = "উত্তরা, ঢাকা"
    override val destinationLabel = "গন্তব্য"
    override val sourceLabel = "শুরুর স্থান"
    override val recentJourneysTitle = "সাম্প্রতিক যাত্রা"
    override val noRecentJourneysTitle = "কোনো সাম্প্রতিক যাত্রা নেই"
    override val noRecentJourneysSubtitle = "আপনার পূর্ববর্তী যাত্রার কোনো তথ্য নেই। নিচের বাটনে চাপ দিয়ে যাত্রা শুরু করুন।"
    override val startNewJourneyBtn = "নতুন যাত্রা শুরু করুন"
    override val soundPlaying = "প্রিভিউ বাজছে"
    override val menu = "মেনু"
    override val add = "যোগ করুন"

    override val filterAll = "সকল"
    override val filterRecent = "সাম্প্রতিক"
    override val filterHome = "বাসা"
    override val filterWork = "কাজ"
    override val filterSaved = "সেভড"

    override val pickOnMapTitle = "ম্যাপে নির্বাচন করুন"
    override val pickOnMapSubtitle = "মানচিত্র থেকে সরাসরি স্থান নির্বাচন করুন"
    override val confirmDestination = "গন্তব্য নিশ্চিত করুন"
    override val mapLayers = "ম্যাপ লেয়ার"

    override val estimatedTime = "আনুমানিক সময়"
    override val speed = "গতি"
    override val endJourney = "যাত্রা শেষ"
    override val endJourneyConfirmTitle = "যাত্রা শেষ করবেন?"
    override val endJourneyConfirmText = "আপনি কি সত্যিই বর্তমান যাত্রা শেষ করতে চান? ট্র্যাকিং এবং অ্যালার্ম বন্ধ হয়ে যাবে।"
}

object EnglishStrings : StringsDefinition {
    override val navHome = "Home"
    override val navSaved = "Saved"
    override val navHistory = "History"
    override val navSettings = "Settings"

    override val ok = "OK"
    override val cancel = "Cancel"
    override val close = "Close"
    override val gotIt = "Got it"
    override val confirm = "Confirm"
    override val back = "Back"
    override val delete = "Delete"
    override val save = "Save"

    override val settingsTitle = "Settings"
    override val offlineMapTitle = "Offline Maps"
    override val offlineMapSubtitle = "Map downloads & cache management"
    override val offlineMapDialogTitle = "Offline Map Cache"
    override val offlineMapDialogText = "GhumBhango automatically caches all browsed map tiles and major transit bus stops offline, so you can track your journey without an active internet connection."

    override val alarmSoundTitle = "Alarm Sound"
    override val alarmSoundDialogTitle = "Default Alarm Sound"
    override val soundDefault = "Default Alarm"
    override val soundBusHorn = "Bus Horn & Melody"
    override val soundGentleBell = "Gentle Bell Tone"
    override val soundTrainWhistle = "Train Whistle"
    override val soundCustom = "Custom Audio / Ringtone"
    override val chooseRingtone = "System Ringtone"
    override val chooseAudioFile = "Audio File (MP3/Storage)"
    override val customAlarmNotSet = "Select audio or ringtone from device"
    override val changeSound = "Change"

    override val defaultDistanceTitle = "Default Distance"
    override fun defaultDistanceSubtitle(meters: Int) = "$meters meters before"
    override val selectDistanceDialogTitle = "Select Default Distance"
    override fun distanceMeters(meters: Int) = "$meters meters before"

    override val adaptiveAlarmTitle = "Adaptive Alarm Intensity"
    override val adaptiveAlarmSubtitle = "Increases sound & vibration closer to destination"
    override val adaptiveAlarmDialogTitle = "Smart Adaptive Alarm"
    override val adaptiveAlarmDialogText = "When enabled, alarm volume and vibration will gradually increase as you approach your destination:\n\n• Outer range (e.g. 400m): Gentle sound & light vibration\n• Mid range (e.g. 200m - 300m): Moderate intensity\n• Close range (within 100m): Full blast sound & vibration\n\nWhen disabled, the alarm rings at full volume immediately."

    override val locationSettingsTitle = "Location Settings"
    override val locationSettingsSubtitle = "High Accuracy (GPS)"

    override val batteryOptimizationTitle = "Battery Optimization"
    override val batteryOptimizationSubtitle = "View Setup Instructions"
    override val batteryOptimizationDialogTitle = "Battery Optimization"
    override val batteryOptimizationDialogText = "To ensure the alarm rings on time even when your screen is locked, please set GhumBhango to 'Unrestricted' battery optimization in device settings."
    override val goToSettingsBtn = "Go to Settings"

    override val languageTitle = "Language"
    override val languageSubtitle = "English"
    override val selectLanguageDialogTitle = "Select Language"

    override val themeModeTitle = "Theme Mode"
    override fun themeModeSubtitle(mode: com.shakhawat.ghhumbhango.domain.model.AppThemeMode): String = mode.getLocalizedName(AppLanguage.ENGLISH)
    override val themeModeDialogTitle = "Select Theme Mode"
    override val themeSystem = "System Default"
    override val themeLight = "Light Mode"
    override val themeDark = "Dark Mode"

    override val aboutAppTitle = "About App"
    override val aboutAppSubtitle = "Version 1.0.0"
    override val aboutAppDialogTitle = "GhumBhango"
    override val aboutAppDialogText = "Version: 1.0.0\n\nNever miss your bus or train stop while taking a nap! GhumBhango continuously monitors GPS distance and rings a loud alarm right before reaching your destination.\n\nDeveloper: Shakhawat Hossain"

    override val privacyPolicyTitle = "Privacy Policy"
    override val privacyPolicySubtitle = "Data collection and usage"
    override val privacyPolicyDialogTitle = "Privacy Policy"
    override val privacyPolicyDialogText = "Your privacy is paramount. GhumBhango processes live GPS location locally on your phone exclusively for sounding your travel alarm. Your location data is never uploaded to any remote servers or shared with third parties."

    override val helpFeedbackTitle = "Help & Feedback"
    override val helpFeedbackSubtitle = "Contact support & suggestions"
    override val helpFeedbackDialogTitle = "Help & Feedback"
    override val helpFeedbackDialogText = "We'd love to hear your feedback or assist with any issues.\n\nEmail: support@ghumbhango.app"

    override val homeGreeting = "Travel Worry-Free"
    override val appTagline = "Sleep peacefully, never miss your stop!"
    override val destinationCardTitle = "Select Destination"
    override val searchPlaceholder = "Where to? Search bus stop or place…"
    override val pickFromMap = "Pick Location from Map"
    override val selectedDestination = "Selected Destination"
    override val change = "Change"
    override val clear = "Clear"
    override val alarmDistanceHeader = "Alarm Distance"
    override val quickStopsHeader = "Popular & Recent Stops"
    override val startJourney = "Start Journey"
    override val journeyActive = "Journey in Progress"
    override val gpsActive = "GPS Active"

    override val setAlarmTitle = "Set Alarm"
    override val distanceFromCurrentLocation = "Distance from current location"
    override val whenToAlarmHeader = "Alarm Proximity Distance"
    override val alarmSoundHeader = "Alarm Sound"
    override val startJourneyTrackingBtn = "Start Journey"

    override val searchDestinationTitle = "Search Destination"
    override val searchInputHint = "Where are you heading? Type place..."
    override val recentSearchesHeader = "Recent Searches"
    override val mapResultsHeader = "Map Results"
    override val popularStopsHeader = "Popular Bus Stops"
    override val noResultsFound = "No results found"

    override val historyScreenTitle = "Journey History"
    override val tabHistory = "History"
    override val tabPresets = "Route Presets"
    override val tabSavedStops = "Saved Stops"
    override val restartJourney = "Restart Journey"
    override val clearAllHistory = "Clear History"
    override val addNewPreset = "Add New Preset"
    override val statusSuccess = "Completed"
    override val statusCancelled = "Cancelled"
    override val noHistoryFound = "No journey history yet"
    override val noPresetsFound = "No saved route presets"
    override val noSavedStopsFound = "No saved stops"

    override val destinationArrivedTitle = "You Have Arrived!"
    override val oneKmAlertTitle = "📍 Destination 1 km Away!"
    override fun oneKmAlertContent(destinationName: String) = "You are 1 km away from $destinationName. Please get ready."
    override val remainingDistance = "Remaining Distance"
    override val dismissAlarm = "Dismiss Alarm"
    override val snoozeAlarm = "Snooze 2 Mins"
    override val alarmAndVibrationRinging = "Alarm and vibration ringing"
    override val snoozeTwoMinutes = "Snooze for 2 minutes"
    override fun remainingText(text: String) = "$text remaining"

    override val yourLocation = "Your Location"
    override val currentLocation = "Current Location"
    override val defaultLocationUttara = "Uttara, Dhaka"
    override val destinationLabel = "Destination"
    override val sourceLabel = "Source"
    override val recentJourneysTitle = "Recent Journeys"
    override val noRecentJourneysTitle = "No Recent Journeys"
    override val noRecentJourneysSubtitle = "No previous journeys found. Tap below to start your journey."
    override val startNewJourneyBtn = "Start New Journey"
    override val soundPlaying = "Playing preview"
    override val menu = "Menu"
    override val add = "Add"

    override val filterAll = "All"
    override val filterRecent = "Recent"
    override val filterHome = "Home"
    override val filterWork = "Work"
    override val filterSaved = "Saved"

    override val pickOnMapTitle = "Pick on Map"
    override val pickOnMapSubtitle = "Select destination directly from map"
    override val confirmDestination = "Confirm Destination"
    override val mapLayers = "Map Layers"

    override val estimatedTime = "Estimated Time"
    override val speed = "Speed"
    override val endJourney = "End Journey"
    override val endJourneyConfirmTitle = "End Journey?"
    override val endJourneyConfirmText = "Are you sure you want to end this journey? Live tracking and alarm will stop."
}

fun getAppStrings(language: AppLanguage): StringsDefinition {
    return when (language) {
        AppLanguage.BANGLA -> BengaliStrings
        AppLanguage.ENGLISH -> EnglishStrings
    }
}

val LocalAppStrings = compositionLocalOf<StringsDefinition> { BengaliStrings }
val LocalAppLanguage = compositionLocalOf { AppLanguage.BANGLA }
