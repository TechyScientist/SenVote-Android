package com.johnnyconsole.android.senvote

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.johnnyconsole.android.senvote.databinding.ActivityAddUserBinding
import com.johnnyconsole.android.senvote.session.UserSession
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class AddUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUserBinding

    private inner class AddUserTask: AsyncTask<String, Unit, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            binding.tvMessage.visibility = GONE
            binding.pbIndicator.visibility = VISIBLE
        }

        override fun doInBackground(vararg params: String): String {
            val conn = (URL("https://wildfly.johnnyconsole.com:8443/senvote-restful/api/user/add").openConnection()) as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.hostnameVerifier = HostnameVerifier { _, _ -> true }
            conn.doOutput = true
            with(conn.outputStream) {
                write("username=${params[0]}".toByteArray())
                write("&name=${params[1]}".toByteArray())
                write("&password=${params[2]}".toByteArray())
                write("&confirm=${params[3]}".toByteArray())
                write("&access=${params[4]}".toByteArray())
                write("&active=${params[5]}".toByteArray())
                write("&admin_username=${UserSession.username!!}".toByteArray())
                flush()
                close()
            }
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
            parseResponseString(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        with(binding) {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
            appBar.activityTitle.text = getString(R.string.activity_title, "Add a User")

            btDashboard.setOnClickListener { _ -> finish() }
            btAddUser.setOnClickListener { _ ->
                if (etUsername.text.isNullOrBlank() || etName.text.isNullOrBlank() ||
                    etPassword.text.isNullOrBlank() || etConfirm.text.isNullOrBlank()) {
                    return@setOnClickListener
                }
                    AddUserTask().execute(
                        etUsername.text.toString(),
                        etName.text.toString(),
                        etPassword.text.toString(),
                        etConfirm.text.toString(),
                        spAccess.selectedItemPosition.toString(),
                        (spStatus.selectedItemPosition == 0).toString()
                    )
            }
        }
    }

    private fun parseResponseString(response: String) {
        Log.d("AddUserResponse", response)
        val json = JSONObject(response)
        val status = json.getInt("status")

        with(binding) {
            if (status == 200) {
                tvMessage.background =
                    AppCompatResources.getDrawable(this@AddUserActivity, R.drawable.dr_success)
                tvMessage.text = HtmlCompat.fromHtml(
                    getString(R.string.success, json.getString("message")),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                tvMessage.visibility = VISIBLE
                etUsername.text.clear()
                etName.text.clear()
                etPassword.text.clear()
                etConfirm.text.clear()
                spAccess.setSelection(0)
                spStatus.setSelection(0)
                etUsername.requestFocus()
            } else {
                tvMessage.background =
                    AppCompatResources.getDrawable(this@AddUserActivity, R.drawable.dr_error)
                tvMessage.text = HtmlCompat.fromHtml(
                    getString(
                        R.string.error,
                        status,
                        json.getString("category"),
                        json.getString("message")
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvMessage.visibility = VISIBLE
            }
        }
    }
}