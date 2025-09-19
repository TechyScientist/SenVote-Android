package com.johnnyconsole.android.senvote

import android.os.AsyncTask
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.johnnyconsole.android.senvote.databinding.ActivityDeleteUserBinding
import android.view.View.VISIBLE
import android.view.View.INVISIBLE
import android.view.View.GONE
import android.widget.ArrayAdapter
import com.johnnyconsole.android.senvote.session.UserSession
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class DeleteUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteUserBinding

    private inner class UserListAdapter(val list: ArrayList<String>) : ArrayAdapter<String>(this@DeleteUserActivity, android.R.layout.simple_spinner_dropdown_item) {

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): String? {
            return list[position]
        }
    }

    private inner class GetUsersTask() : AsyncTask<Unit, Unit, String>() {
        override fun onPreExecute() {
            binding.tvMessage.visibility = GONE
            binding.pbIndicator.visibility = VISIBLE
        }

        override fun doInBackground(vararg p0: Unit?): String {
            val conn = (URL("https://wildfly.johnnyconsole.com:8443/senvote-restful/api/user/all-except-${UserSession.username}")
                .openConnection()) as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.hostnameVerifier = HostnameVerifier { _, _ -> true }

            conn.connect()
            val response = StringBuffer()

            if(conn.responseCode == HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                for (line in reader.readLines()) {
                    response.append(line)
                }
            }
            return response.toString()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            binding.pbIndicator.visibility = INVISIBLE
            val userArray = JSONObject(result).getJSONArray("users")
            val usernames = ArrayList<String>()

            for(i in 0 until userArray.length()) {
                usernames.add(userArray.getString(i))
            }

            binding.spUserList.adapter = UserListAdapter(usernames)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        with(binding) {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }

            appBar.activityTitle.text = getString(R.string.activity_title, "Delete a User")
            tvDeleteWarning.text = HtmlCompat.fromHtml(
                getString(R.string.delete_warning),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            btDashboard.setOnClickListener { _ -> finish() }

            GetUsersTask().execute()
        }
    }
}