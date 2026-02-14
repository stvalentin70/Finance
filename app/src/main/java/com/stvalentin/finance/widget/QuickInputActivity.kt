package com.stvalentin.finance.widget

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
    private lateinit var etAmount: EditText
    private lateinit var btnSave: Button

    private var selectedType: TransactionType = TransactionType.EXPENSE

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_quick_input)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        // Инициализируем базу данных
        database = AppDatabase.getDatabase(this)

        initViews()
        setupListeners()
        
        updateButtonStates()
        updateCategorySpinner()
    }

    private fun initViews() {
        btnIncome = findViewById(R.id.btn_income)
        btnExpense = findViewById(R.id.btn_expense)
        spinnerCategory = findViewById(R.id.spinner_category)
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

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            val selectedCategory = spinnerCategory.selectedItem.toString()
            
            if (amountText.isNotEmpty()) {
                try {
                    val amount = amountText.replace(',', '.').toDouble()
                    if (amount > 0) {
                        saveTransaction(amount, selectedCategory)
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

    private fun saveTransaction(amount: Double, category: String) {
        lifecycleScope.launch {
            val transaction = Transaction(
                type = selectedType,
                category = category,
                amount = amount,
                description = "Быстрый ввод",
                date = System.currentTimeMillis()
            )
            
            database.transactionDao().insert(transaction)
            
            val typeText = if (selectedType == TransactionType.INCOME) "Доход" else "Расход"
            val dateFormat = SimpleDateFormat("HH:mm", Locale("ru"))
            val timeStr = dateFormat.format(Date())
            
            Toast.makeText(
                this@QuickInputActivity,
                "✅ $typeText: $category $amount ₽ ($timeStr)",
                Toast.LENGTH_LONG
            ).show()
            
            finish()
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
            "Перевод", "Вклад", "Другое"
        )
        
        private val expenseCategories = arrayOf(
            "Продукты", "Транспорт", "Жилье", "Кредиты",
            "Ипотека", "Развлечения", "Здоровье", "Одежда",
            "Образование", "Рестораны", "Связь", "Перевод",
            "Интернет покупки", "Хозтовары", "Мебель",
            "Электротовары", "Услуги", "Другое"
        )
    }
}