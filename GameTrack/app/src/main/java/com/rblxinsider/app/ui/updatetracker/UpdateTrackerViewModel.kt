package com.rblxinsider.app.ui.updatetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL

data class UpdateItem(
    val title: String,
    val link: String,
    val pubDate: String,
    val description: String
)

sealed class UpdateState {
    object Loading : UpdateState()
    data class Success(val items: List<UpdateItem>) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateTrackerViewModel : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Loading)
    val state: StateFlow<UpdateState> = _state

    init {
        loadUpdates()
    }

    fun loadUpdates() {
        viewModelScope.launch {
            _state.value = UpdateState.Loading
            try {
                val items = mutableListOf<UpdateItem>()
                val url = URL("https://devforum.roblox.com/c/updates/announcements/rss.xml")
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val stream = connection.getInputStream()

                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(stream, "UTF-8")

                var title = ""
                var link = ""
                var pubDate = ""
                var description = ""
                var inItem = false
                var currentTag = ""

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name
                            if (currentTag == "item") inItem = true
                        }
                        XmlPullParser.TEXT -> {
                            if (inItem) {
                                when (currentTag) {
                                    "title" -> title += parser.text
                                    "link" -> link += parser.text
                                    "pubDate" -> pubDate += parser.text
                                    "description" -> description += parser.text
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "item") {
                                items.add(UpdateItem(
                                    title = title.trim(),
                                    link = link.trim(),
                                    pubDate = pubDate.trim(),
                                    description = description.trim()
                                        .replace(Regex("<[^>]*>"), "")
                                        .take(200)
                                ))
                                title = ""; link = ""; pubDate = ""; description = ""
                                inItem = false
                            }
                            currentTag = ""
                        }
                    }
                    eventType = parser.next()
                }
                stream.close()
                _state.value = UpdateState.Success(items)
            } catch (e: Exception) {
                _state.value = UpdateState.Error(e.message ?: "Hata")
            }
        }
    }
}