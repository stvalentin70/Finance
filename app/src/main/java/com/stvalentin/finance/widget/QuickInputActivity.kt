package com.stvalentin.finance.widget

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.stvalentin.finance.R

class QuickInputActivity : AppCompatActivity() {

    private lateinit var btnIncome: Button
    private lateinit var btnExpense: Button
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnSave: Button

    private var selectedType: String = "expense" // по умолчанию расход

    // Жестко заданные категории (пока так)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_quick_input)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        initViews()
        setupListeners()
        
        // Устанавливаем начальное состояние (расход выбран)
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
            selectedType = "income"
            updateButtonStates()
            updateCategorySpinner()
        }

        btnExpense.setOnClickListener {
            selectedType = "expense"
            updateButtonStates()
            updateCategorySpinner()
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            val selectedCategory = spinnerCategory.selectedItem.toString()
            
            if (amountText.isNotEmpty()) {
                val typeText = if (selectedType == "income") "Доход" else "Расход"
                Toast.makeText(
                    this, 
                    "$typeText: $selectedCategory - $amountText ₽", 
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonStates() {
        btnIncome.isSelected = selectedType == "income"
        btnExpense.isSelected = selectedType == "expense"
    }

    private fun updateCategorySpinner() {
        val categories = if (selectedType == "income") {
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
}