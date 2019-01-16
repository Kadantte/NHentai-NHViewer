package personal.ttd.nhviewer.api

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Adapter
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import personal.ttd.nhviewer.DebugTag.TAG
import personal.ttd.nhviewer.api.NHapi.userAgent
import personal.ttd.nhviewer.comic.Comic
import personal.ttd.nhviewer.file.FeedReaderContract
import personal.ttd.nhviewer.file.Storage
import java.util.HashMap

class MyApi {
    //static methods
    companion object {
        private val baseUrl = "https://nhentai.net/language/chinese/"
        private val pagePrefix = "?page="

        fun getPages(mid:String, types:String, totalPage:Int):ArrayList<String>{
            val pages:ArrayList<String> = ArrayList()

            for(i in 1 until totalPage){
                pages.add(NHapi.getPictureLinkByPage(mid, types[i].toString(), i))
            }

            return pages
        }

        fun getThumbLink(mid:String, type:String):String{
            val t = type.get(0).toString()

            return NHapi.getThumbLink(mid, t);
        }

        fun getComicInfoLink(id:String):String{
            return NHapi.getComicInfoLinkById(id)
        }

        private fun getComicsByDocument(doc:Document ): java.util.ArrayList<Comic> {
            val comics: java.util.ArrayList<Comic>
            comics = java.util.ArrayList()

            val galleries = result.getElementsByClass("gallery")

            var count = 1
            for (gallery in galleries) {

                val title = gallery.getElementsByTag("div").get(0).text()
                val thumbLink = gallery.getElementsByTag("img").attr("data-src")
                val id = gallery.getElementsByTag("a").attr("href").split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[2]
                val mid = thumbLink.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[thumbLink.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size - 2]
                //int totalPage = gallery.getElementById("thumbnail-container").childNodeSize();

                val comic = Comic()
                comic.title = title
                comic.thumbLink = thumbLink
                comic.id = id
                comic.mid = mid
                //comic.setTotalPage(totalPage);

                //setted comic properties
                Log.e(TAG, String.format("Finished %d gallery", count++))
                //Log.i(TAG, "setComics: totalPage: " + totalPage);

                comics.add(comic)

            }
            return comics
        }

        //call get comics by document
        fun getComicsBySite(baseUrl: String, page: Int, context: Context): java.util.ArrayList<Comic> {


            val queue = Volley.newRequestQueue(context)
            var doc : Document
            val comics = ArrayList<Comic>()

            val documentRequest = object : StringRequest( //
                    Request.Method.GET, //
                    baseUrl + pagePrefix + page, //
                    { response ->
                        //Log.i(TAG, "onResponse: " + response);
                        doc = Jsoup.parse(response)

                        comics.add(getComicsByDocument(doc))

                        ///TODO finding replacement for below 3 line
//                        adapter.addComic(getComics(doc[0]))
//                        mySwipeRefreshLayout.setRefreshing(false)
//                        adapter.notifyDataSetChanged()
                    }, //
                    { error ->
                        // Error handling
                        println("Houston we have a problem ... !")
                        error.printStackTrace()
                    }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = userAgent
                    return headers
                }
            } //

            // Add the request to the queue...
            queue.add(documentRequest)

            // ... and wait for the document.
            // NOTE: Be aware of user experience here. We don't want to freeze the app...
            queue.addRequestFinishedListener(RequestQueue.RequestFinishedListener<Any> {
                Log.i(TAG, "onRequestFinished: Finished")
            })

            return comics
        }


        ///TODO API unusable -20190115
        /*
            using comicid
            => comicInfoLink
            => request( comicInfoLink )
            <= JSON response
            => comic : Comic > return
         */
        private fun setComicById(comic:Comic, id: String, context: Context) {
            val comicInfoLink = getComicInfoLink(id)
            val queue = Volley.newRequestQueue(context)

            //request for nh's json
            val jsonObjReq = object : JsonObjectRequest(Request.Method.GET,
                    comicInfoLink, null,
                    { response ->
                        //fetching comic pages
                        var mid: String
                        var type: String
                        var totalPage = 0
                        try {
                            mid = response.getString("media_id")
                            totalPage = response.getInt("num_pages")

                            Log.i(TAG, String.format("onResponse: mid: %s, totalPage: %s", mid, totalPage))
                            comic.mid = mid
                            comic.totalPage = totalPage
                            //Log.i(TAG, String.format("onResponse: mid: %s, totalPage: %s, type: %s", mid, totalPage, type))
                            for (i in 1..totalPage) {
                                type = response.getJSONObject("images").getJSONArray("pages").getJSONObject(i - 1).getString("t")
                                comic.addPage(NHapi.getPictureLinkByPage(mid, type, i))
                                Log.i(TAG, String.format("onResponse: mid: %s, totalPage: %s, type: %s", mid, totalPage, type))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }, { error -> Log.i(TAG, String.format("Error: fetching comic pages"))

            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = NHapi.userAgent
                    return headers
                }
            }
            // Add above request to queue...
            queue.add<JSONObject>(jsonObjReq)
            queue.addRequestFinishedListener<Any> { request ->
                Log.i(TAG, "onRequestFinished: Finished")

            }
        }
        fun getComicByMid(mid: String, applicationContext: Context): Comic {
            return Comic();
        }


        /*
        * putting everything needed into database
        */
        fun addToCollection(context: Context, c: Comic) {
            Storage.insertTableCollection(context, c.id)
            Storage.insertTableComic(context, c)
            Storage.insertTableInnerPage(context, c)
        }


        /*
        * History part
        * */
        fun addToHistory(context: Context, c: Comic, p: Int) {
            Storage.insertTableHistory(context, c.id, p)

        }

        fun updateHistory(context: Context, c: Comic, p: Int) {
            Storage.updateTableHistory(context, c.id, p)

        }

        fun getHistory(context: Context) : List<Comic>{
            return Storage.getAllRows(context, FeedReaderContract.FeedEntry.TABLE_HISTORY, FeedReaderContract.FeedEntry.COLUMN_NAME_UPDATE_TIME)
        }

        fun downloadComic(id: Int) {

        }

        //only for transfering data
        fun updateFromJson(context: Context, id:Int) {
            var c: Comic
            c = Comic()

            addToCollection(context, c)
        }

    }
}