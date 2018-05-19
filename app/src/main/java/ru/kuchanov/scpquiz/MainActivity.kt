package ru.kuchanov.scpquiz

import android.content.pm.PackageInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.kuchanov.scpquiz.di.SCOPE_APP
import toothpick.Toothpick
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var packageInfo: PackageInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        Toothpick.inject(this, Toothpick.openScope(SCOPE_APP))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener { Snackbar.make(root, packageInfo.packageName, Snackbar.LENGTH_LONG).show() }
    }
}
