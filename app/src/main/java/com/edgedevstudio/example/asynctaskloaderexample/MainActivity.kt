package com.edgedevstudio.example.asynctaskloaderexample

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity()
        , LoaderManager.LoaderCallbacks<Long>
        ,BackgroundProgressInterface {
    val LOADER_ID = 20 // this can be any integer
    val bundleValue: String = "1,000,000" // 1 Million
    var progressTxtView: TextView? = null
    var finalResultTxtView: TextView? = null
    val bundle = Bundle()

    companion object {
        val TAG = "MainActivity"
        val BUNDLE_KEY = "key.to.identify.bundle.value"
    }

    override fun onUpdateProgress(progress: Int) {
        runOnUiThread(Runnable {
            // This will run on the UI thread
            kotlin.run {
                progressTxtView?.setText("Progress = $progress%")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressTxtView = findViewById(R.id.progress)
        finalResultTxtView = findViewById(R.id.final_result)
        val beginButton : Button = findViewById(R.id.begin_async_task_loader)
        beginButton.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "Begin Button Tapped!")
            makeOperationAddNumber()
        })
        val cancelButton : Button = findViewById(R.id.cancel_async_task_loader)
        cancelButton.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "Cancel Button Tapped!")
            cancelBackgroundProcess()
        })

        Log.d(TAG, "OnCreate")

        bundle.putString(BUNDLE_KEY, bundleValue)
        supportLoaderManager.initLoader(LOADER_ID, null, this)
    }

    private fun cancelBackgroundProcess() {
        val loader = supportLoaderManager.getLoader<Long>(LOADER_ID)
        if (loader != null) {
            val isCancelled = loader.cancelLoad()
            if (isCancelled) showToastMsg("Loader Canceled!")
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Long> {
        Log.d(TAG, "Inside onCreateLoader, bundle = $args")
        return AsyncTaskLoaderSubClass(this, args, this)
    }

    override fun onLoadFinished(loader: Loader<Long>?, data: Long?) {
        Log.d(TAG, "Inside onLoadFinished")
        if (data == null || data < 1) {
            Log.d(TAG, "AsyncTaskLoader = cancelled or Bundle = null")
            return
        } else {
            finalResultTxtView?.setText("The Sum of Numbers between 1 to 1 million \n = $data")
            Log.d(TAG, "Final Result = $data")
        }
    }

    override fun onLoaderReset(loader: Loader<Long>?) {}

    private fun makeOperationAddNumber() {
        // this will try to fetch a Loader with ID = LOADER_ID
        val loader: Loader<Long>? = supportLoaderManager.getLoader(LOADER_ID)
        if (loader == null) {
            /* if the Loader with the loaderID not found,
            * Initialize a New Loader with ID = LOADER_ID
            * Pass in a bundle that the AsynTaskLoader will use
            * Also pass the necessary callback which is 'this' because we've implemented it on our activity
            */
            supportLoaderManager.initLoader(LOADER_ID, bundle, this)
        } else {
            /* If the Loader was found with ID = LOADER_ID,
            * Stop whatever it may be doing
            * Restart it
            * Pass in a bundle that the AsynTaskLoader will use
            * Also pass the necessary callback which is 'this' because we've implemented it on our activity
            */
            supportLoaderManager.restartLoader(LOADER_ID, bundle, this)
        }
    }

    fun showToastMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    class AsyncTaskLoaderSubClass
    (context: Context, val args: Bundle?, val progressInterface: BackgroundProgressInterface)
        : AsyncTaskLoader<Long>(context) {

        override fun onStartLoading() {
            super.onStartLoading()
            // can be likened to OnPreExecute in AsyncTask
            forceLoad() // without calling this loadInBackground will not execute
        }

        override fun loadInBackground(): Long {
            Log.d(TAG, "Inside Load In Background")
            val numberString = args?.getString(BUNDLE_KEY)
            if (numberString == null) {
                Log.d(TAG, "Bundle = null")
                return 0
            }
            val number = numberString.replace(",", "").toLong()
            var result: Long = 0
            for (i in 1..100) { //loop 100
                result = 0
                for (j in 1..number) {
                    result = result + j
                }
                progressInterface.onUpdateProgress(i)
                Log.d(TAG, "Loop $i Result = $result")
                if (isLoadInBackgroundCanceled) {
                    Log.d(TAG, "Complex Background Computation was Canceled")
                    return 0
                }
            }
            return result
        }
    }
}