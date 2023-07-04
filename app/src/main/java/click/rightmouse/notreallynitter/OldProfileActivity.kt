package click.rightmouse.notreallynitter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import click.rightmouse.notreallynitter.corescrape.Profile
import click.rightmouse.notreallynitter.corescrape.Utils
import com.android.volley.VolleyError
// import com.bumptech.glide.Glide
import org.jsoup.nodes.Document

class OldProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oldprofile)

        Log.i("FUCK", "SHIT")

        // ATTENTION: This was auto-generated to handle app links.
        val appIntents: Intent = intent
        val appLinkAction: String? = appIntents.action
        val appLinkData: Uri? = appIntents.data
        val extras = appIntents.extras
        Log.i("dhjkgbdfbhjdfgbhj", "dfgbjfhjbhjdfg")

        // Define widget variables here
        val avatarView = findViewById<ImageView>(R.id.avatar)
        val fullNameText = findViewById<TextView>(R.id.fullName)
        val handleText = findViewById<TextView>(R.id.handleName)
        val descriptionText = findViewById<TextView>(R.id.descriptionText)

        // Fix avatar size
        // avatarView.layoutParams.height = avatarView.width

        val utils = Utils(this)

        val handle: String?

        if(appLinkData != null){
            // the activity has been launched by the user triggering a URL.
            TODO("Go from URL to handle.")
        } else if (extras != null){
            // we probably got launched by the app itself, as the data would only have been passed
            // by the app itself
            handle = extras.getString("handle")
            //The key argument here must match that used in the other activity
            Log.i("FUCK", handle.toString())
        } else {
            // how on fuck did this activity get launched
            handle = "masterbootrec"  // because it NEEdsto be I NJITIALIZED.
        }

        utils.getJsoupDocument(
            Utils.genNitterUrl(handle).first,
            object : Utils.VolleyCallback{
                override fun onSuccess(result: Document?) {
                    // handle how to do profile stuff
                    val profile = Profile().fromDocument(result)
                    Log.i("fuck", profile.toString())

                    fullNameText.text = profile.username
                    handleText.text = profile.handle
                    descriptionText.text = profile.biography

                    // Glide.with(this@OldProfileActivity)
                        // .load("https://placekitten.com/1280/1280")
                        // .load(Utils.genNitterUrl(profile.profilePhotoURL))
                        // .into(avatarView)

                    // TODO("Implement more profile stats")
                    // TODO("Load posts")
                }

                override fun onError(error: VolleyError?) {
                    TODO("Haven't implemented error handling.")
                }
            })
    }
}