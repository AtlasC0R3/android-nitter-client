package click.rightmouse.notreallynitter

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.component1
import androidx.core.util.component2
import click.rightmouse.notreallynitter.corescrape.Utils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val utils = Utils(this)

    private fun goToProfile(handle: String?) {
        if(handle == ""){
            Toast.makeText(this, "No handle; nothing to load.", Toast.LENGTH_SHORT).show()
            return
        }

        val newIntent = Intent(this@MainActivity, ProfileActivity::class.java)
        val (url, instance) = Utils.genNitterUrl(handle)

        /*utils.getJsoupDocument(
            url,
            object : Utils.VolleyCallback{
                override fun onSuccess(result: Document?) {
                    // handle how to do profile stuff
                    newIntent.putExtra("profile", result.toString())
                    newIntent.putExtra("instance", instance)
                    newIntent.putExtra("url", url)
                    startActivity(newIntent)
                }
            })*/

        utils.getOkHttpUrl(
            url,
            object: Callback{
                override fun onFailure(call: Call, e: IOException) {
                    TODO("Not yet implemented")
                }

                override fun onResponse(call: Call, response: Response) {
                    newIntent.putExtra("profile", response.body?.string())
                    newIntent.putExtra("instance", instance)
                    newIntent.putExtra("url", url)
                    startActivity(newIntent)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val utils = Utils(this)

        val handleText = findViewById<EditText>(R.id.handleInput)
        val goButton = findViewById<Button>(R.id.goButton)

        val noSpaceFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {  // if there's a whitespace
                    return@InputFilter ""                 // we don't want that
                }
            }
            null                              // cocainer
        }

        handleText.filters = arrayOf(noSpaceFilter)

        // val bullshit = utils.getNitterPage("classicchirpy")
        // Log.i("fuck", bullshit.toString())

        goButton.setOnClickListener { goToProfile(handleText.text.toString()) }
        handleText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    // Handle "Done" action
                    goToProfile(handleText.text.toString())
                    true
                }
                else -> false
            }
        }  // Thanks, ChatGPT, for all this. I shall learn absolutely nothing from it now.
    }
}