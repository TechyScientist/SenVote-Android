package com.johnnyconsole.android.senvote

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.johnnyconsole.android.senvote.databinding.ActivityDashboardBinding
import com.johnnyconsole.android.senvote.session.UserSession
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.core.text.HtmlCompat

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        with(binding) {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(root, { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            })
            appBar.activityTitle.text = getString(R.string.activity_title, "Dashboard")

            tvHeader.text = getString(R.string.dashboard_header, UserSession.name!!)

            if(UserSession.access != 1) llAdmin.visibility = INVISIBLE
            if(!UserSession.active) {
                tvInactiveWarning.text = HtmlCompat.fromHtml(getString(R.string.inactive_warning,
                    if(UserSession.access != 1) "." else " or perform any administrative functions."), HtmlCompat.FROM_HTML_MODE_LEGACY)
                btActiveDivisions.visibility = INVISIBLE
                tvInactiveWarning.visibility = VISIBLE
                llAdmin.visibility = INVISIBLE
            }

            btSignOut.setOnClickListener { _ ->
                UserSession.destroy()
                finish()
            }

            btAddUser.setOnClickListener {_ ->
                startActivity(Intent(this@DashboardActivity, AddUserActivity::class.java))
            }
        }
    }
}