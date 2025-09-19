package com.johnnyconsole.android.senvote

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
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
            val conn =
                (URL("https://wildfly.johnnyconsole.com:8443/senvote-restful/api/user/all-except-${UserSession.username}")
                    .openConnection()) as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.hostnameVerifier = HostnameVerifier { _, _ -> true }

            conn.connect()
            val response = StringBuffer()

            if (conn.responseCode == HTTP_OK) {
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

            for (i in 0 until userArray.length()) {
                usernames.add(userArray.getString(i))
            }

            binding.spUserList.adapter = UserListAdapter(usernames)
        }
    }

    private inner class DeleteUserTask(): AsyncTask<String, Unit, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            binding.pbIndicator.visibility = VISIBLE
        }

        override fun doInBackground(vararg params: String?): String? {
            val conn =
                (URL("https://wildfly.johnnyconsole.com:8443/senvote-restful/api/user/delete")
                    .openConnection()) as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.hostnameVerifier = HostnameVerifier { _, _ -> true }
            conn.doOutput = true
            with(conn.outputStream) {
                write("username=${params[0]}".toByteArray())
                write("&admin_username=${UserSession.username}".toByteArray())
                flush()
                close()
            }
            conn.connect()
            val response = StringBuffer()

            if (conn.responseCode == HTTP_OK) {
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
            btDeleteUser.setOnClickListener { _ ->
                val dialog = AlertDialog.Builder(this@DeleteUserActivity)
                    .setTitle(R.string.confirm_delete_title)
                    .setMessage(
                        HtmlCompat.fromHtml(
                            getString(R.string.delete_warning),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    .setPositiveButton(R.string.yes) { it, _, ->
                        it.dismiss()
                        val item = spUserList.selectedItem.toString()
                        DeleteUserTask().execute(
                            item.subSequence(
                                item.indexOf("(") + 1,
                                item.indexOf(")")
                            )
                                .toString()
                        )
                    }.setNegativeButton(R.string.no) { it, _ ->
                        it.dismiss()
                    }.create()

                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.error))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.success))
            }
                GetUsersTask().execute()
        }
    }

    private fun parseResponseString(response: String) {
        Log.d("DeleteUserResponse", response)
        val json = JSONObject(response)
        val status = json.getInt("status")

        with(binding) {
            if (status == 200) {
                tvMessage.background =
                    AppCompatResources.getDrawable(this@DeleteUserActivity, R.drawable.dr_success)
                tvMessage.text = HtmlCompat.fromHtml(
                    getString(R.string.success, json.getString("message")),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                tvMessage.visibility = VISIBLE
                GetUsersTask().execute()
            } else {
                tvMessage.background =
                    AppCompatResources.getDrawable(this@DeleteUserActivity, R.drawable.dr_error)
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