package edu.seminolestate.albumflippertwo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

//    album ids came from musizmatch.com, which hosts the api used in fetching track lists.
    int albumIds[] = new int[] {10266158,10282885,10285026,10276947,19659084,15496436,10266044,10287445,10265979,
            10277138,10820946,10280365,10294206,21359448,10266226,12659742,10279957,10285332};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
//    Onclick instatiates views, gets spinner, formulates url, gets objects and sets track text view
    public void onClickFindCover(View view) throws Exception {
        ImageView image = findViewById(R.id.covers);
        Spinner cover =  findViewById(R.id.spinner);
        TextView tracks =  findViewById(R.id.track_list);

        int coverNumber = cover.getSelectedItemPosition() +1;
        String coverName = "cover" + coverNumber;
        String albumId = String.valueOf(albumIds[cover.getSelectedItemPosition()]);
        int resID = getResources().getIdentifier(coverName, "drawable", "edu.seminolestate.albumflippertwo");
        image.setImageResource(resID);
        String url = "https://api.musixmatch.com/ws/1.1/album.tracks.get?format=jsonp&callback=callback&album_id=" + albumId + "&apikey=3d698869e3c288022483027587b42d1f";
        JSONArray trackArray = readJsonFromUrl(url);
        try {
            String tracksLists = getTrackObjects(trackArray).toString();
            tracks.setText(tracksLists);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

//This method takes the url and sends it to the internal class for retrieval from the api. The class
//    extends AsyncTask which allows internet connections outside the main thread which is not permitted.
//    this method then drills down into the JSON objects until it comes to the JSON array containing the
//    track list
    public static JSONArray readJsonFromUrl(String url) throws Exception, JSONException {
            try {
                String jsonTextA = new GetTheTracks().execute(url).get(); //calls the doInBackground method
                //in the internal class sending the url as a parameter. the .get() gets the returned string

                String jsonText = jsonTextA.substring(9); //trim unnecessary "callback(" from front of api string

                JSONObject json = new JSONObject(jsonText);//make JsonObject from text
                JSONObject message = json.getJSONObject("message");//drill into first layer
                JSONObject body = message.getJSONObject("body");//drill into second layer
                JSONArray trackList = body.getJSONArray("track_list");//drillinto tracklist and get array

                return trackList;

            } catch (Exception e) {}
            return null;
    }

//  This method takes the JSONArray with the track list. It then loops the array getting the track object,
//    for each track, then from that, gets the track_name object and appends it to the Stringbuilder that
//    is then set as text for the "Track Listing" TextView.
    public static StringBuilder getTrackObjects(JSONArray trackArray) throws IOException, JSONException {
        try {
            StringBuilder tracksList = new StringBuilder();//instantiate a sb for TrackList TV
            for (int i = 0; i < trackArray.length(); ++i) {//loop through number of tracks is JSONArray
                JSONObject tracks = trackArray.getJSONObject(i);//get JSONObject[i]
                JSONObject trackItem = tracks.getJSONObject("track");//fetch the track there
                String trackName = trackItem.get("track_name").toString();//from that track, fetch the track name
                tracksList.append(trackName);//add it to the sb
                tracksList.append("\n");//add new line to sb
            }
            return tracksList;
        } catch (Exception e) {
        }
        return null;
    }

    //  readAll takes string of characters from url reader  input stream and adds to stringbuilder
    @NotNull
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
// internal class to take internet access out of main thread this reads the data from the url, open an inputstream
// and reads it in. Sends back a JSON string with the album data
//  Using external internet connection required adding <uses-permission android:name="android.permission.INTERNET"/>
// To AndroidManifest.xml.
 class GetTheTracks extends AsyncTask<String, Void, String> {

    @Override
     protected String doInBackground(String... url)  {
         String trackList = url[0]; //takes first url on list (only one parameter in this case)
         String jsonTextA;
         try {
             InputStream is = new URL(trackList).openStream(); //create input stream
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));//make br
             jsonTextA = MainActivity.readAll(rd);//calls readAll method in main class to read it into the string

         } catch (Exception e) {
             jsonTextA = e.toString();
         }
         return jsonTextA; //returns JSON string to main class
     }

     @Override
     protected void onPostExecute(String result)  {
         super.onPostExecute(result);

     }
 }

