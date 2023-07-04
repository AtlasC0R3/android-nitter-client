package click.rightmouse.notreallynitter.corescrape;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Utils {
    Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public static String parseDate(String title){
        // TODO: go from "title" attribute of date elements, to an actual date and time element that isn't dumb.
        // https://github.com/dgnsrekt/nitter_scraper/blob/2da2cf9dca66c7ff9b02c06fccf1cfad772f14a5/nitter_scraper/tweets.py#L21
        return title;
    }

    public static String[] getCursors(Document page) {
        // do some processing on the given document to get two strings
        String next = null;
        String prev = null;
    
        Elements showMore = page.select("div.show-more > a");
        if(showMore.first() != null) {
            // pagination is present, we just need to understand if it's next or previous page
            for(Element e : showMore) {
                if(e.text().contains("new")) {
                    // we have found the "previous" page
                    prev = e.attr("href");  // retrieve the previous link
                    if(prev.contains("?cursor=")) prev = prev.replace("?cursor=", "");  // if it has a cursor link, use that
                    else {
                        // otherwise either Nitter has gone through some changes, or there simply isn't any.
                        // perhaps we just need to return to the parent page.
                        prev = "parent";
                    }
                } else if(e.text().contains("more")) {
                    // we have instead found the "next" page
                    next = e.attr("href");
                    if(next.contains("?cursor=")) next = next.replace("?cursor=", "");  // if it has a cursor link, use that
                    else {
                        // otherwise either Nitter has gone through some changes, or there simply isn't any.
                        // perhaps we just need to return to the parent page.
                        next = "parent";
                    }
                }
            }
            return new String[] {prev, next};
        }
        else return null;
    }
    
    public static Pair<String, String> genNitterUrl(String uri){
        String instance = getNitterInstance();
        Log.i("core", "Fetching " + instance + "/" + uri);
        if(uri.startsWith("/")) uri = uri.replaceFirst("/", "");
        return new Pair<>(String.format("https://%s/%s", instance, uri), instance);
    }

    public void getJsoupDocument(String url, final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Document doc = Jsoup.parse(response);
                    callback.onSuccess(doc);
                }, error -> {
                    try {
                        callback.onError(error);
                    } catch (VolleyError e) {
                        throw new RuntimeException(e);
                    }
                });
        queue.add(stringRequest);
    }
    public interface VolleyCallback {
        void onSuccess(Document result);
        void onError(VolleyError error) throws VolleyError;
    }

    public void getNitterPage(String uri) {
        // don't use this, I'm going to have a McFucking heart attack.
        RequestQueue queue = Volley.newRequestQueue(context);

        final Document[] doc = {null};

        String url = String.valueOf(genNitterUrl(uri));

        getJsoupDocument(url, new VolleyCallback() {
            @Override
            public void onSuccess(Document result) {
                doc[0] = result;
            }

            @Override
            public void onError(VolleyError error) throws VolleyError {
                throw error;
            }
        });

        // return Jsoup.connect(strong).cookie("hlsPlayback", "on").get();
    }

    public static String getNitterInstance(){
        // List of random Nitter instances
        final ArrayList<String> instances = new ArrayList<>(Arrays.asList(
                "nitter.absturztau.be",
                "nitter.it",
                "nitter.net",
                "nitter.moomoo.me",
                "nitter.hostux.net",
                "nitter.projectsegfau.lt"));

        // Create a new instance of the Random class
        Random rand = new Random();

        // Generate a random number between 0 and the size of the ArrayList - 1
        int randomIndex = rand.nextInt(instances.size());

        // Get the item at that randomly generated index
        return instances.get(randomIndex);
    }
}
