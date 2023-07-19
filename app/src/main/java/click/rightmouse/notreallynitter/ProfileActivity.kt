package click.rightmouse.notreallynitter

import android.app.assist.AssistContent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import click.rightmouse.notreallynitter.corescrape.Profile
import click.rightmouse.notreallynitter.corescrape.Tweet
import click.rightmouse.notreallynitter.corescrape.Utils
import click.rightmouse.notreallynitter.ui.theme.NitterishTheme
import coil.compose.AsyncImage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class ProfileActivity : ComponentActivity() {
    private var profileUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appIntents: Intent = intent
        val extras = appIntents.extras

        val resultData: String? = extras?.getString("profile")

        // TODO: handle loading the profile in this Activity

        val resultDocument = Jsoup.parse(resultData)
        resultDocument.outputSettings().prettyPrint(false)

        var latestResultDocument: Document? = null
        // Will be used later on in the program's life to store the last loaded Document.
        // Unless we figure out a way to extract straight away the cursors.
        // But that requires effort and I don't want to do that.
        // TODO: Find some sort of way to store the cursors rather than the whole document.

        val profile = Profile().fromDocument(resultDocument)
        val instance = extras?.getString("instance") ?: "nitter.net"
        profileUrl = extras?.getString("url")

        // Allow app to draw under status/navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)




//       window.setFlags(
//           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//       )
        // Uncomment this if you don't fucking care about the time at the top of your screen.
        // In other words, the status bar. Doing this disables the transparent-ish background
        // that is normally applied to it, and just makes it completely transparent.

        val tweets: MutableList<Tweet> = mutableStateListOf()
        tweets.addAll(Tweet.fromPage(resultDocument))

        val utils = Utils(this@ProfileActivity)

        setContent {
            NitterishTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(resultData == null) Text("god damn something bad happened")
                    else {
                        val listState = rememberLazyListState()

                        // val stupidShit = remember{
                        //     mutableStateOf(tweets.size)
                        // }

                        LazyColumn(
                            state = listState
                        ){
                            item{
                                GenerateProfile(profile, instance)
                            }
                            items(items = tweets) {tweet ->
                                GeneratePost(tweet, profile, instance)
                                Spacer(Modifier.height(8.dp))
                            }
                            item{
                                Spacer(Modifier.navigationBarsPadding())
                            }

//                            val reachedEnd =
//                                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ==
//                                            tweets.lastIndex) &&
//                                            (hasNotAlreadyWarned)
//
//
//                            if(reachedEnd){
//                                Toast.makeText(this@ProfileActivity, "We should load more posts.", Toast.LENGTH_SHORT).show()
//                                hasNotAlreadyWarned = false
//                            }
                        }

                        val shouldStatusBarBeOpaque by remember {
                            derivedStateOf {
                                listState.firstVisibleItemIndex > 0
                            }
                        }

                        // val lastVisibleItemIndex = remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.last().index } }

                        val loadMorePosts by remember{
                            derivedStateOf {
                                (listState.firstVisibleItemIndex) ==    // shows first visible item
                                // (listState.layoutInfo.visibleItemsInfo.last().index) ==
                                        (
                                        tweets.size       // how many tweets there are.
                                                - (       // subtract by
                                                5         // five posts
                                                + 1       // whilst taking into account the bottom navbar padding
                                                + 1       // as well as the top profile overview
                                                )
                                        )
                            }
                        }

                        if(shouldStatusBarBeOpaque){
//                            window.setFlags(
//                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                            )  Really inefficient.
                            Log.i("LOG", "So we really need to find a way to set the status bar's opacity.")
                        }

                        if(loadMorePosts){
                            Log.i("tweedle", "We should load more posts. There are currently ${tweets.size} posts.")
//                            Toast.makeText(this, "We should load more posts.", Toast.LENGTH_SHORT).show()

                            // TODO: Move this to separate function.
                            val cursors = Utils.getCursors(latestResultDocument ?: resultDocument)
                            val nextUp = cursors[1]
                            val urlToFetch = "$profileUrl?cursor=$nextUp"
                            /*utils.getJsoupDocument(
                                urlToFetch,
                                object : Utils.VolleyCallback{
                                    override fun onSuccess(result: Document?) {
                                        val newTweets = Tweet.fromPage(result)
                                        tweets.addAll(newTweets)
                                        latestResultDocument = result
                                    }
                                })*/

                            utils.getOkHttpUrl(
                                urlToFetch,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        // TODO: Implement error handling here too.
                                        Toast.makeText(
                                            this@ProfileActivity,
                                            "Failed to fetch new posts.",
                                            Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        latestResultDocument = Jsoup.parse(response.body!!.string())

                                        val newTweets = Tweet.fromPage(latestResultDocument)
                                        tweets.addAll(newTweets)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
//        val cursors = Utils.getCursors(resultDocument)
//        val nextUp = cursors[1]
//        val urltofetch = Utils.genNitterUrl(profile.handle.replace("@", "") + "?cursor=$nextUp")
//        utils.getJsoupDocument(
//            urltofetch,
//            object : Utils.VolleyCallback{
//                override fun onSuccess(result: Document?) {
//                    val newTweets = Tweet.fromPage(result)
//                    tweets.addAll(newTweets)
//                }
//
//                override fun onError(error: VolleyError?) {
//                    TODO("Haven't implemented error handling.")
//                }
//            })
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)
        if(!profileUrl.isNullOrBlank()) outContent.webUri = Uri.parse(profileUrl)
    }
}


@Composable
fun GenerateProfile(user: Profile, instanceToUse: String){
    val rowModifier: Modifier = Modifier

    // Box(Modifier.wrapContentHeight(Alignment.Top)){
    BoxWithConstraints(modifier=Modifier.fillMaxSize()){
        val horizontallyCramped = maxWidth < 350.dp
        val canDrawBanner = user.bannerPhotoURL != "" && !horizontallyCramped

        if (canDrawBanner) {
            AsyncImage(
                // model = Utils.genNitterUrl(user.bannerPhotoURL.replace("https%3A%2F%2Fpbs.twimg.com%2F", "")),
                model = "https://$instanceToUse${
                    user.bannerPhotoURL.replace(
                        "https%3A%2F%2Fpbs.twimg.com%2F",
                        ""
                    )
                }",
                // sorry for the confusing mess.
                contentDescription = "Banner",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            bottomStartPercent = 10,
                            bottomEndPercent = 10
                        )
                    )
                    .fillMaxWidth()
                    .height(192.dp)
                    .drawWithCache(drawFadeOnImage())
                // .align(Alignment.BottomEnd)
                // .fillMaxHeight()
            )
        }

        val pfpModifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(20))
            .size(128.dp)
        val pfpURL = "https://$instanceToUse${user.profilePhotoURL}"

        if(horizontallyCramped){

            // Horizontally cramped profile view
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp)
            ){
                Spacer(
                    Modifier
                        .statusBarsPadding()
                        .height(8.dp))

                AsyncImage(model = pfpURL, contentDescription = null, modifier = pfpModifier)

                GenerateProfileName(user, false)
                Spacer(Modifier.height(8.dp))
                GenerateUserBiography(user, 8)

            }

        } else{

            // Not cramped view; full screen view

            Column {
                if(canDrawBanner) Spacer(Modifier.height(150.dp))
                // if the banner is drawn, put a spacer so that the profile view (pfp, name, bio...)
                // kinda overlaps the banner itself
                else Spacer(
                    Modifier
                        .statusBarsPadding()
                        .height(8.dp))
                // otherwise, just draw the inset to leave room for the status bar
                // as well as an additional 8dp

                Row(rowModifier) {
                    AsyncImage(
                        model = pfpURL,
                        contentDescription = null,
                        modifier = pfpModifier
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        GenerateProfileName(user)

                        GenerateUserBiography(user)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateUserBiography(user: Profile, leftPadding: Int = 0) {
    if (user.biography != "") {
        Surface(
            shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp,
            modifier = Modifier.padding(leftPadding.dp, 0.dp, 8.dp, 8.dp)
        ) {
            Text(
                text = user.biography,
                modifier = Modifier.padding(all = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GenerateProfileName(
    user: Profile,
    bigSize: Boolean = true
) {
    if(bigSize){
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenerateUserDisplayName(user)
        }
    } else {
        GenerateUserDisplayName(user, true)
    }

    Text(
        text = user.handle,
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(2.dp)
    )
}

@Composable
private fun GenerateUserDisplayName(
    user: Profile,
    centerText: Boolean = false
) {
    val randomSnark = arrayOf(
        " piece of shit", ", whatever that means now", " virgin", " spender of 8$",
        " important person", " \"programming socks\" owner", " unfortunate account", " cupholder",
        "ededdeeeddiiideverififed", "™", "n't", " celebrity someone @ twitter has heard of",
        " nyanya purrr meow prrrr~"
    ).random()

    val textAlign = if(centerText) TextAlign.Center else null

    Text(
        text = user.username,
        color = MaterialTheme.colorScheme.primary,
        textAlign = textAlign,
        style = when (user.biography) {
            "" -> MaterialTheme.typography.headlineLarge
            else -> MaterialTheme.typography.headlineMedium
        },
    )
    if (user.verified) {
        Text(
            "(verified$randomSnark)",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun drawFadeOnImage(
    fadeColor: Color = Color.Black
): CacheDrawScope.() -> DrawResult =
    {
        val gradient = Brush.verticalGradient(
            colors = listOf(Color.Transparent, fadeColor),
            startY = (size.height * 0.7).toFloat(),
            endY = size.height
        )
        onDrawWithContent {
            drawContent()
            drawRect(gradient, blendMode = BlendMode.Multiply)
        }
    }


@Composable
fun GeneratePost(post: Tweet, originalProfile: Profile, instanceToUse: String){
    // You know, I could use BoxWithConstraints here like I did with the Profile view.
    // But I won't; because the performance of those aren't negligible,
    // and because we wouldn't really hide a lot, or save much space, then I just won't.
    // I'll keep this as is. At least for now.

    Surface(shape = MaterialTheme.shapes.medium, tonalElevation =4.dp, modifier=Modifier.padding(horizontal = 8.dp, vertical=4.dp)){
        Column(Modifier.padding(4.dp)) {
            val authorShape: RoundedCornerShape
            var annotatedHeader: AnnotatedString? = null

            if(post.pinned){
                annotatedHeader = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Pinned tweet")
                    }
                }
            }

            if(post.retweet){
                annotatedHeader = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(originalProfile.username ?: originalProfile.handle)
                    }

                    append(" retweeted")
                }
            }

            if(annotatedHeader != null) {
                Surface(
                    // shape = MaterialTheme.shapes.small,
                    shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
                    // tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        annotatedHeader,
                        Modifier.padding(6.dp)
                    )
                }

                authorShape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
            } else{
                authorShape = RoundedCornerShape(8.dp)
            }

            Surface(
                shape = authorShape,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.inverseSurface
            ) {
                Row(modifier = Modifier.padding(2.dp)){
                    if(post.retweet){
                        AsyncImage(
                            // model = Utils.genNitterUrl(post.avatarUrl),
                            model = "https://$instanceToUse${post.avatarUrl}",
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(20))
                                .size(36.dp)
                        )
                    }

                    Column(Modifier.padding(4.dp)) {
                        Text(post.fullname, style=MaterialTheme.typography.headlineSmall)
                        // Spacer(Modifier.height(4.dp))
                        Text(post.username, style=MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Text(post.content, Modifier.padding(4.dp))

            if(post.attachments.isNotEmpty()){
                // TODO: handle attachments
                // Lazy staggered grids won't work, I think because of nested Lazy items.
                // (This LazyGrid will be inside a LazyColumn element)
                // So we might have to do it some manual way. Woo-hoo, Columns and Rows it is.

                // Twitter has an attachment limit: 4, no matter the type.
                // If 1, just display the preview. No big deal.
                // If 2, display them, split in half, in a Row.
                // If 3, have 2 columns. The first column is a row of the two first, the last column
                //       will have the last one.
                // If 4, two columns, each having a row containing two attachments (2 * 2 = 4)

                AsyncImage(
                    model = post.attachments[0].url, contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "...well, attachments WOULD be here, but that's not implemented yet.",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
