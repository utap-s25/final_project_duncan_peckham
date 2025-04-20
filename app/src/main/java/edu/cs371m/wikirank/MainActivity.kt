package edu.cs371m.wikirank

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import edu.cs371m.wikirank.databinding.ActionBarBinding
import edu.cs371m.wikirank.databinding.ActivityMainBinding
import edu.cs371m.wikirank.ui.MainViewModel

class MainActivity : AppCompatActivity() {
    // This allows us to do better testing
    private val viewModel: MainViewModel by viewModels()
    private var actionBarBinding: ActionBarBinding? = null
    companion object {
        var globalDebug = true
    }

    private fun initDebug() {
        if(globalDebug) {
            assets.list("")?.forEach {
                Log.d(javaClass.simpleName, "Asset file: $it" )
            }
        }
    }

    private fun initActionBar(actionBar: ActionBar){
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBarBinding =ActionBarBinding.inflate(layoutInflater)
        actionBar.customView = actionBarBinding?.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDebug()
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        val navController = findNavController(R.id.main_frame)

        viewModel.setArticleOneTitle("Boston")
        viewModel.setArticleTwoTitle("New York City")

        //action bar logic primarily from the reddit hw
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        supportActionBar?.let{
            initActionBar(it)
        }
        activityMainBinding.toolbar.setupWithNavController(navController, appBarConfiguration)

    }
}
