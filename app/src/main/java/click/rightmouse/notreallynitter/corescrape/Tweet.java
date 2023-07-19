package click.rightmouse.notreallynitter.corescrape;

import android.util.Log;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Tweet {
    public Long tweetId = 0L;
    public String fullname = "";
    public String username = "";
    public String avatarUrl = "";
    public String date = "";
    public boolean retweet = false;
    public boolean pinned = false;
    public String content = "";
    public ArrayList<Profile> attributions = new ArrayList<>();
    public Integer replies = 0;
    public Integer retweets = 0;
    public Integer quotes = 0;
    public Integer likes = 0;
    public ArrayList<Attachment> attachments;
    public Tweet quote = null;
    public String nextCursor = "";
    public static final char[] charsThatAreNotLinks = {'#', '@'};

    // Constructor for initializing all properties to their default values
    public Tweet() {
        this.tweetId = 0L;
        this.fullname = "";
        this.username = "";
        this.avatarUrl = "";
        this.date = "";
        this.retweet = false;
        this.pinned = false;
        this.content = "";
        this.attributions = new ArrayList<>();
        this.replies = 0;
        this.retweets = 0;
        this.likes = 0;
        this.attachments = new ArrayList<>();
        this.quote = null;
    }

    public static ArrayList<Tweet> fromPageString(String page){
        return fromPage(Jsoup.parse(page));
    }

    public static ArrayList<Tweet> fromPage(Document page){
        page.outputSettings().prettyPrint(false);
        return fromElements(page.select("div.timeline-item"));
    }

    public static ArrayList<Tweet> fromElements(Elements timelineItems){
        ArrayList<Tweet> tweets = new ArrayList<>();
        // Tweet tweet;

        for(Element tweetElement : timelineItems){
            // TODO: Hmm, I obviously have no idea what you're doing, but you probably want to declare the "tweet" variable inside the loop to begin with.
            // https://linuxrocks.online/@friend/110093714781302845
            if(tweetElement.hasClass("show-more")) continue;  // if it's one of those "Load newest" buttons, give up
            // tweet = new Tweet();  VERY IMPORTANT line of code, it wipes the previous Tweets from this for loop
                                  // so that we don't get leftover values.
            // FUCKING FUCK FUCK GOD DAMMIT YOU FUCKING CUNT SHIT GOD FUCKING I AM SO FUCKING FED UP WITH COMPUTERS
            // HOLY FUCKING SHIT FUCK YOU FUCK YOU FUCK YOU FUCK YOU FUCK YOU FUCK YOU THAT'S WHY YOU HAD LEFTOVER
            // VALUES BECAUSE WE DIDN'T RESET THE TWEET FOR EVERY ONE OF THEM THERE IS, GOD DAMMIT FOR FUCK'S SAKE
            // TWO MILLION YEARS OF CONSTANT EVOLUTION TO MAKE A FUCKING DUMBASS, WHAT THE FUCK AM I? WHY AM I EVEN
            // HERE? WHAT THE FUCK DO I CONTRIBUTE IF I CAN'T UNDERSTAND MY OWN BENIGN MESS, GOD. FUCKING. DAMMIT.

            Tweet tweet = new Tweet();

            tweet.fromElement(tweetElement);
            tweets.add(tweet);
        }

        return tweets;
    }

    public static String getContentFromContentElement(Element conElement){
        if(conElement != null){  // if there actually IS text in the tweet to get, continue.
            // content = conElement.wholeText();
            // this doesn't include the full link texts, it abbreviates them as per what Nitter displays to the user.
            // the <a href=""> link isn't abbreviated, but I presume to save space and not have extraneously long links take
            // heaps of space, Nitter truncates the display text, and in tandem, what Element.wholeText() gets.

            // wait, brain storm idea: what if we went through the div, and found every anchor element, and instead
            // of doing some really complex things trying to parse those anchor elements, we just modify every anchor
            // so that the text content is replaced by that of the HREF attribute.

            // maybe that's what the code below, courtesy of chatgpt, does?
                    
            // find all <a> elements within the <div> element and modify them to include href attribute
            Elements anchorElements = conElement.select("a");
            for (Element anchor : anchorElements) {
                // NOTE: if we do actually want ANY hyperlinks (to @s and #s) in the Android app, do stuff here!

                // if(!(anchor.text().startsWith("http")))       // if the anchor element wasn't already a URL
                // continue;                                     // go to the next anchor and leave this one alone.
                // the fuck was I thinking?

                // Check if the anchor's text starts with any of the predefined chars using startsWith()
                boolean startsWithPredefinedChar = false;  // if true at the end of this for loop, it does start with an ignored character (#, @)
                for (char c : charsThatAreNotLinks) {      // go through a list of those predefined characters
                    if(anchor.text().startsWith(Character.toString(c))) {  // if the link's text starts with that character
                        startsWithPredefinedChar = true;                   // then we shall ignore this anchor element once we finish this loop
                        break;                                             // ...which is now.
                    }
                }

                if(startsWithPredefinedChar)               // if this anchor text started with one of those characters
                    continue;                              // then ignore this anchor.

                                                                     // this ensures we don't accidentally filter out mentions and hashtags.
                String href = anchor.attr("abs:href");  // get full URL of anchor link
                anchor.attr("href", href);              // set original anchor link to that full URL
                anchor.text(href);                                   // set anchor element's text to the full URL rather than (N/Tw)itter's truncated one
            }
                    
            // get the entire text from the <div> element, with full <a href=""> links included
            // Finally, we get the entire text and HTML of the <div> element (including the modified <a> elements) using the outerHtml() method.
            // String divText = conElement.outerHtml();
                    
            // return conElement.wholeText();
            // return conElement.wholeText().replace("\n         ", "");
            return conElement.text();
            // now that we've taken care of all of the link nonsense, we can finally get the wholeText()
            // as for the janky replace(), I don't fuckin' know, man.
            
        } else return null;  // if the tweet is empty, don't do any of that.
    }

    public void fromElement(Element element){
        attributions = new ArrayList<>();

        Log.i("FUCK", element.toString());

        Element conElement = element.select("div.tweet-content").first();
        Elements pinnedElements = element.select(".pinned");
        Element headerElement = element.select("div.tweet-header").first();
        Element retweetElement = element.select("div.retweet-header").first();
        Element fullnameElement = headerElement.select("a.fullname").first();
        Element usernameElement = headerElement.select("a.username").first();
        Element avatarElement = headerElement.select("a.tweet-avatar > img").first();
        Element dateElement = headerElement.select("span.tweet-date > a").first();

        Elements attributionElements = new Elements();
        attributionElements = element.select("div.tweet-body > a.attribution");
        for(Element e : attributionElements){
            attributions.add(new Profile().fromAttribution(e));
        }

        content = getContentFromContentElement(conElement);

        fullname = fullnameElement.text();
        username = usernameElement.text();
        avatarUrl = avatarElement.attr("src");
        date = dateElement.attr("title");

        Element quoteElement = element.select("div.quote").first();
        if(quoteElement != null){
            quote = new Tweet();
            Element quoteNameRow = quoteElement.selectFirst("div.tweet-name-row");
            Element quoteNames = quoteNameRow.selectFirst("div.fullname-and-username");
            quote.fullname = quoteNames.selectFirst("a.fullname").text();
            quote.username = quoteNames.selectFirst("a.username").text();
            quote.avatarUrl = quoteNames.selectFirst("img.avatar").attr("src");

            Element quoteOriginalDate = quoteNameRow.selectFirst("span.tweet-date > a");
            quote.date = quoteOriginalDate.attr("title");

            // Element quoteText = quoteElement.selectFirst("div.quote-text");
            // if(quoteText != null) quote.content = quoteText.wholeText();
            quote.content = getContentFromContentElement(quoteElement.selectFirst("div.quote-text"));

            Element quoteAttachments = quoteElement.selectFirst("div.quote-media-container > div.attachments");
            if(quoteAttachments != null) quote.attachments = Attachment.loadFromAttachments(quoteAttachments);

            // quote.tweetId = quoteElement.selectFirst("a.quote-link").attr("href");
        }
        
        Element attachmentsDiv = element.selectFirst("div.tweet-body > div.attachments");
        // to not select the attachments from a quote tweet, idea: select only the div.tweet-body's attachments
        // in a way that doesn't include div.quote-media-container's
        if(attachmentsDiv != null) this.attachments = Attachment.loadFromAttachments(attachmentsDiv);
        
        // for every element in div.tweet-stats > 
            // get span.tweet-stat >
                    // span.icon-comment (if counting replies)
                    // span.icon-retweet (if counting retweets)
                    // span.icon-quote   (if counting quote RTs)
                    // span.icon-heart   (if counting likes/hearts)
            // .text() to get the numeric value of that stat
        Elements stats = element.select("div.tweet-stats > span.tweet-stat");
        for(Element stat : stats){
            if(stat.text().isEmpty()) continue;  // if there's no statistic, skip this and go to next stat (if avail.)

            // if(stat.text().contains(",")){     // if the metric contains , (because.. you know... numbers, readability)
            //     stat.text(                     // set the stat's text
            //     stat.text().replace(",", "")   // to the same thing without the commas
            //     );
            // }
            // Don't do this. This'll break everything. FUCK.
            
            int metric;
            try {
                metric = Integer.parseInt(stat.text().replace(",", ""));
            } catch (Exception e) {
                continue;
            }
            switch (stat.selectFirst("div.icon-container > span").attr("class")) {
                case "icon-comment":
                    // replies
                    this.replies = metric;
                    break;
                case "icon-retweet":
                    // retweets
                    this.retweets = metric;
                    break;
                case "icon-quote":
                    // quote RTs
                    this.quotes = metric;
                    break;
                case "icon-heart":
                    // likes
                    this.likes = metric;
                    break;
                default:
                    break;
            }
        }

        // extract tweet IDs from current Tweet element, perhaps from the URL?
        // update: no, screw the URL. using "span.tweet-date > a" we get an anchor with the "HREF" attribute
        // of the current post, and from there we can extract its ID.
        String tweetAnchor = dateElement.attr("href");     // get current tweet's date element (it has an anchor)
        String[] tweetUrlSplit = tweetAnchor.split("/");          // split up the URL to allow getting the last element
        String tweetUrlLast = tweetUrlSplit[tweetUrlSplit.length - 1];  // kinda wack that Java doesn't have any other way of getting the "last item" of an array
        if(tweetUrlLast.contains("#")) tweetUrlLast = tweetUrlLast.split("#")[0];  // sometimes urls end with #m
        this.tweetId = Long.decode(tweetUrlLast);                       // decode the ID (string) to a long
        // this'll pass.

        if(pinnedElements.first() != null) pinned = true;  // if there is a pinned element, it's pinned
        if(retweetElement != null) retweet = true;         // if there is a retweet element, it's a retweet

        // TODO: if there are replies on current tweet's post, also parse them

    }

    // public Tweet fromId(Long id) throws Exception{
        // find a better way to do this. maybe using just the tweet ID whenever we get that going.
        // we do now. come on you dumb fuck just use getNitterPage("nitter.absturztau.be", "i/status/(tweet id)")
        // Document page = Jsoup.connect(url).cookie("hlsPlayback", "on").get();

    //     Document page = Utils.getNitterPage("i/status/" + id.toString());

    //     return fromElement(page.select("div.timeline-item").first());
    // }

    public String parseIntoString(){
        String poopshitter = String.format("""
                Tweet by %s (%s) (https://nitter.absturztau.be%s)
                %s

                pinned? %s; retweet? %s; posted on %s
                %d replies, %d quote RTs, %d likes and %d retweets
                """, this.fullname, this.username, this.avatarUrl,
                this.content,
                this.pinned, this.retweet, this.date,
                this.replies, this.quotes, this.likes, this.retweets
                );
        for(Attachment att : this.attachments) {
            poopshitter = poopshitter + (String.format("\n%s: %s", att.type, att.url));
        }
        for(Profile attrib : this.attributions) {
            poopshitter = poopshitter + String.format("\nattribution: %s (%s) (%s)", attrib.username, attrib.handle, attrib.profilePhotoURL);
        }

        if(this.quote != null) {
            poopshitter = poopshitter + "\n----- QUOTE RETWEET CONTENT -----\n" + this.quote.parseIntoString();
        }

        return poopshitter;
    }
}
