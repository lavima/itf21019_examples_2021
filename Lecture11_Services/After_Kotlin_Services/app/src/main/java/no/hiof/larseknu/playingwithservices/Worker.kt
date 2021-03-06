package no.hiof.larseknu.playingwithservices

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Bundle
import android.os.Environment
import android.os.HandlerThread
import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Worker(private val context: Context) {
    private var gpsHandlerThread: HandlerThread? = null
    private var locationListener: LocationListener? = null
    private var locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun monitorGpsInBackground() {
        gpsHandlerThread = HandlerThread("GPSThread")
        gpsHandlerThread?.start()

        if (locationListener == null)
            this.locationListener = NoOpLocationListener()

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener!!,
            gpsHandlerThread?.looper
        )
    }

    fun monitorGpsInBackground(locationListener: LocationListener) {
        this.locationListener = locationListener
        monitorGpsInBackground()
    }

    @SuppressLint("MissingPermission")
    fun stopGpsMonitoring() {
        if (locationListener != null)
            locationManager.removeUpdates(locationListener!!)

        if (gpsHandlerThread != null)
            gpsHandlerThread?.quit()
    }

    /**
     * Uses the LocationManager to get the last known location
     * Since we use the LocationManager, we need permission to access the users location
     */
    @SuppressLint("MissingPermission")
    fun retrieveLocation() : Location {
        var lastLocation: Location? = null

        if (useGpsToGetLocation) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        if (lastLocation == null) {
            lastLocation = createLocationManually()
        }

        addDelay()
        return lastLocation!!
    }

    /**
     * Gets one address from a specific location
     * If no address is found, HI?? location is returned
     */
    fun reverseGeocode(location: Location) : String {
        val geocoder = Geocoder(context)
        var addressList : MutableList<Address>? = null

        try {
            addressList = geocoder.getFromLocation(location.latitude, location.longitude, 5)
        }
        catch (e : IOException) {
            Log.e("Worker.reverseGeocode", "IOException Error")
        }
        addDelay()

        return if (addressList.isNullOrEmpty()) {
            "B.R.A. veien 4, 1757 Halden"
        } else {
            addressList[0].getAddressLine(0)
        }
    }

    /**
     * Gets JSON from a specific URL
     * @param urlString The URL that points to a JSON-object
     * @return The JSON object retreived from the URL
     */
    fun getJSONObjectFromURL(urlString: String): JSONObject {

        try {
            val url = URL(urlString)

            val urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            val br = BufferedReader(InputStreamReader(url.openStream()))

            val sb = StringBuilder()

            for (line in br.readLine())
                sb.append(line)

            br.close()
            urlConnection.disconnect()

            val jsonString = sb.toString()

            addDelay()

            return JSONObject(jsonString)
        } catch (jsone: JSONException) {
            Log.e("Worker.getJSON", jsone.message)
        } catch (ioe: IOException) {
            Log.e("Worker.getJSON", ioe.message)
        }

        addDelay()
        return JSONObject()

    }

    /**
     * Appends the information given by the parameters to a file in the downloads directory
     *
     * @param location The location to save to the file
     * @param address The address to save to the file
     * @param movietitle The movietitle to save to the file
     * @param fileName The filename we're going to use to save to
     */
    fun saveToFile(location: Location, address: String, movietitle: String, fileName: String) {
        try {
            val targetDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!targetDir.exists())
                targetDir.mkdirs()

            val outFile = File(targetDir, fileName)
            val fileWriter = FileWriter(outFile, true)
            val writer = BufferedWriter(fileWriter)

            val outLine = String.format(
                Locale.getDefault(), "%s - %f/%f\n",
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(location.time),
                location.latitude,
                location.longitude
            )
            writer.write(outLine)
            writer.write(address + "\n")
            writer.write(movietitle + "\n\n")

            writer.flush()
            writer.close()
            fileWriter.close()
        } catch (ex: Exception) {
            Log.e("Worker.saveToFile", ex.message)
        }

        addDelay()
    }

    private fun addDelay() {
        Thread.sleep(2000)
    }

    /**
     * Creates a location manually for HI?? to use for testing
     *
     * @return The location of HI??
     */
    private fun createLocationManually(): Location? {
        val lastLocation = Location("Hiof")
        val now = Date()
        lastLocation.time = now.time
        lastLocation.latitude = 59.128229
        lastLocation.longitude = 11.352860

        return lastLocation
    }

    companion object {
        var useGpsToGetLocation = true
    }


    internal inner class NoOpLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {}

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

        override fun onProviderEnabled(s: String) {}

        override fun onProviderDisabled(s: String) {}
    }
}
