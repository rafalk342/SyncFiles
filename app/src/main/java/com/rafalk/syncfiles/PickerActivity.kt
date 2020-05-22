package com.rafalk.syncfiles

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File


class PickerActivity : AppCompatActivity(),
    FilesListFragment.OnListFragmentInteractionListener,
    FilesListFragment.OnDriveListFragmentInteractionListener,
    CoroutineScope by MainScope() {

    lateinit var currentDirectory: File
    lateinit var currentDriveDirectory: DriveFilesAdapter.DriveItem
    private lateinit var googleDriveService: Drive

    companion object {
        const val REQUEST_SIGN_IN = 1
        private const val REQUEST_WRITE_STORAGE_REQUEST_CODE = 2
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        Timber.d("onActivityResult=$requestCode")
        when (requestCode) {
            REQUEST_SIGN_IN -> {
                if (resultCode == RESULT_OK && result != null) {
                    Timber.d("Signin successful")
                } else {
                    Timber.d("Signin request failed")
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        requestAppPermissions()
        requestSignInToGoogleAccount()

        setContentView(R.layout.activity_picker)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, currentDirectory.absolutePath, Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, getDrivePath(currentDriveDirectory), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

    }

    private fun getDrivePath(directory: DriveFilesAdapter.DriveItem): String {
        if (directory.parent == null) {
            return "/"
        }
        return getDrivePath(directory.parent)  + directory.file.name + '/'
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onListFragmentInteraction(item: SystemFilesAdapter.FileItem?) {
        Timber.d("Clicked ${item?.file?.absolutePath}")
        currentDirectory = item?.file!!
    }

    private fun requestAppPermissions() {
        if (hasReadPermissions() && hasWritePermissions()) {
            return
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_WRITE_STORAGE_REQUEST_CODE
        ) // your request code
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSignInToGoogleAccount() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
        Timber.d("Client received")
        startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)
    }

    override fun onDriveListFragmentInteraction(item: DriveFilesAdapter.DriveItem?) {
        Timber.d("Clicked ${item?.content}")
        if (item != null) {
            currentDriveDirectory = item
        }
    }
}