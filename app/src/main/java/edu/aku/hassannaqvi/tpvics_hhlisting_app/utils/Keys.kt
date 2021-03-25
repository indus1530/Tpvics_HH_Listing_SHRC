package edu.aku.hassannaqvi.tpvics_hhlisting_app.utils

object Keys {

    init {
        System.loadLibrary("native-lib")
    }

    external fun apiKey(): String
}