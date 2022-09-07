package com.example.rssfeed

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import java.net.URL
import kotlin.properties.Delegates


//hold data for the entry feed
class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageUrl: String = ""

    override fun toString(): String {
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            summary = $summary
            imageUrl = $imageUrl
        """.trimIndent()
    }
}



class MainActivity : AppCompatActivity() {
    private val Tag = "MainActivity" //Logging tag

    private val downloadData by lazy { DownloadData(this, findViewById(R.id.xmlListView)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(Tag, "onCreate Called")
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(Tag, "onCreate: done")
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData.cancel(true)
    }

    //Define the object that uses the async call
    companion object {
        //This is for async calls
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>(){
            private val Tag = "Download Data"

            var propContext : Context by Delegates.notNull()
            var propListView : ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                Log.d(Tag, "onPostExecute: paramter is $result")
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

//                val arrayAdapter = ArrayAdapter<FeedEntry>(propContext, R.layout.list_item, parseApplications.applications)
//                propListView.adapter = arrayAdapter
                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter

            }
            override fun doInBackground(vararg url: String?): String {
                Log.d(Tag, "doInBackground: Starts with ${url[0]}")
                //Download the rss feed from the url passed to doInBackground
                val rssFeed = downloadXML(url[0])
                //If rssFeed is empty after download log an error
                if (rssFeed.isEmpty()) {
                    Log.e(Tag, "doInBackground: Error downloading")
                }
                //Return the XML
                return rssFeed
            }
            //Reads the URL
            private fun downloadXML(urlPath: String?): String{
                //Not good for huge files
                return URL(urlPath).readText()
            }
        }
    }
}