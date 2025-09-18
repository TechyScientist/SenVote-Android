package com.johnnyconsole.android.senvote.session

class UserSession() {
    companion object {
        var username: String? = null
        var name: String? = null
        var access = -1
        var active = false

        fun construct(username: String, name: String, access: Int, active: Boolean) {
            this.username = username
            this.name = name
            this.access = access
            this.active = active
        }

        fun destroy() {
            username = null
            name = null
            access = -1
            active = false
        }
    }
}
