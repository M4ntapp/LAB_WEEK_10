package com.example.lab_week_10

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast // <-- IMPORT BARU
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject // <-- IMPORT BARU
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.Date // <-- IMPORT BARU

class MainActivity : AppCompatActivity() {
    private val db by lazy { prepareDatabase() }
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }
    private var lastUpdatedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()
        prepareViewModel()
    }


    override fun onStart() {
        super.onStart()
        lastUpdatedDate?.let {
            Toast.makeText(this, "Last updated: $it", Toast.LENGTH_LONG).show()
        }
    }


    override fun onPause() {
        super.onPause()
        val currentTotalValue = viewModel.total.value ?: 0
        val currentDateString = Date().toString()
        val updatedTotalObject = TotalObject(value = currentTotalValue, date = currentDateString)
        db.totalDao().update(Total(ID, updatedTotalObject))
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel(){
        viewModel.total.observe(this) {
            updateText(it)
        }
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).allowMainThreadQueries().build()
    }


    private fun initializeValueFromDatabase() {
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isEmpty()) {

            val initialObject = TotalObject(value = 0, date = Date().toString())
            db.totalDao().insert(Total(id = 1, total = initialObject))
            viewModel.setTotal(0)
            lastUpdatedDate = initialObject.date
        } else {

            val existingTotalObject = totalList.first().total
            viewModel.setTotal(existingTotalObject.value)
            lastUpdatedDate = existingTotalObject.date
        }
    }

    companion object {
        const val ID: Long = 1
    }
}