package com.stvalentin.finance.widget

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stvalentin.finance.R
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuickInputActivity : AppCompatActivity() {

    private lateinit var btnIncome: Button
    private lateinit var btnExpense: Button
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var layoutDate: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var etAmount: EditText
    private lateinit var btnSave: Button

    private var selectedType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: Long = System.currentTimeMillis()
    
    private lateinit var database: AppDatabase
    
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_quick_input)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        database = AppDatabase.getDatabase(this)

        initViews()
        setupListeners()
        
        updateButtonStates()
        updateCategorySpinner()
        updateDateDisplay()
    }

    private fun initViews() {
        btnIncome = findViewById(R.id.btn_income)
        btnExpense = findViewById(R.id.btn_expense)
        spinnerCategory = findViewById(R.id.spinner_category)
        etDescription = findViewById(R.id.et_description)
        layoutDate = findViewById(R.id.layout_date)
        tvDate = findViewById(R.id.tv_date)
        etAmount = findViewById(R.id.et_amount)
        btnSave = findViewById(R.id.btn_save)
    }

    private fun setupListeners() {
        btnIncome.setOnClickListener {
            selectedType = TransactionType.INCOME
            updateButtonStates()
            updateCategorySpinner()
        }

        btnExpense.setOnClickListener {
            selectedType = TransactionType.EXPENSE
            updateButtonStates()
            updateCategorySpinner()
        }

        layoutDate.setOnClickListener {
            showDateTimePicker()
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            val selectedCategory = spinnerCategory.selectedItem.toString()
            val description = etDescription.text.toString()
            
            if (amountText.isNotEmpty()) {
                try {
                    val amount = amountText.replace(',', '.').toDouble()
                    if (amount > 0) {
                        saveTransaction(amount, selectedCategory, description)
                    } else {
                        Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDateTimePicker() {
        calendar.timeInMillis = selectedDate
        
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        selectedDate = calendar.timeInMillis
                        updateDateDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        tvDate.text = dateTimeFormat.format(Date(selectedDate))
    }

    private fun saveTransaction(amount: Double, category: String, description: String) {
        lifecycleScope.launch {
            val transaction = Transaction(
                type = selectedType,
                category = category,
                amount = amount,
                description = description,
                date = selectedDate
            )
            
            database.transactionDao().insert(transaction)
            
            // ОБНОВЛЯЕМ ВИДЖЕТ
            forceUpdateWidget()
            
            val typeText = if (selectedType == TransactionType.INCOME) "Доход" else "Расход"
            val dateStr = dateTimeFormat.format(Date(selectedDate))
            
            Toast.makeText(
                this@QuickInputActivity,
                "✅ $typeText: $category $amount ₽ ($dateStr)",
                Toast.LENGTH_LONG
            ).show()
            
            finish()
        }
    }
    
    private fun forceUpdateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, FinanceWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            // Создаем экземпляр виджета и вызываем forceUpdate
            FinanceWidget().forceUpdate(this, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateButtonStates() {
        btnIncome.isSelected = selectedType == TransactionType.INCOME
        btnExpense.isSelected = selectedType == TransactionType.EXPENSE
    }

    private fun updateCategorySpinner() {
        val categories = if (selectedType == TransactionType.INCOME) {
            incomeCategories
        } else {
            expenseCategories
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    companion object {
        private val incomeCategories = arrayOf(
            "Зарплата", "Фриланс", "Инвестиции", 
            "Подарок", "Возврат долга", "Связь", 
            "Перевод", "Вклад", "Дети",
            "Другое"
        )
        
        private val expenseCategories = arrayOf(
            "Продукты", "Транспорт", "Жилье", "Кредиты",
            "Ипотека", "Развлечения", "Здоровье", "Одежда",
            "Образование", "Рестораны", "Связь", "Перевод",
            "Интернет", "Хозтовары", "Мебель", "Электро",
            "Услуги", "Дети",
            "Другое"
        )
    }
}