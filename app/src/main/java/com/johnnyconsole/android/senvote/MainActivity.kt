package com.johnnyconsole.android.senvote

import android.os.AsyncTask
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.johnnyconsole.android.senvote.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import android.view.View.VISIBLE
import android.view.View.INVISIBLE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private inner class SignInTask: AsyncTask<String, Unit, Unit>() {
        private var response = StringBuffer()

        override fun onPreExecute() {
            super.onPreExecute()
            binding.pbIndicator.visibility = VISIBLE
        }

        override fun doInBackground(vararg params: String): Unit {
            val conn = (URL("https://wildfly.johnnyconsole.com:8443/senvote-restful/api/user/signin").openConnection()) as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.hostnameVerifier = HostnameVerifier { _, _ -> true }
            conn.doOutput = true
            with(conn.outputStream) {
                write("username=${params[0]}&password=${params[1]}".toByteArray())
                flush()
                close()
            }
            conn.connect()

            if(conn.responseCode == HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                for (line in reader.readLines()) {
                    response.append(line)
                }
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            binding.pbIndicator.visibility = INVISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        with(binding) {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }

            btSignIn.setOnClickListener {_ ->
                if(etUsername.text.isNullOrBlank() || etPassword.text.isNullOrBlank())
                    return@setOnClickListener
                SignInTask().execute(etUsername.text.toString().lowercase(), etPassword.text.toString())
            }
        }
    }
}