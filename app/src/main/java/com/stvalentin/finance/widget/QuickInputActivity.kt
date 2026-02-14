package com.stvalentin.finance.widget

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stvalentin.finance.R

class QuickInputActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Делаем окно прозрачным и без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_quick_input)

        // Настраиваем параметры окна - чтобы было как диалог
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        // Инициализируем view
        etAmount = findViewById(R.id.et_amount)
        btnSave = findViewById(R.id.btn_save)

        // Обработчик кнопки
        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            
            if (amountText.isNotEmpty()) {
                // Показываем сообщение (позже заменим на сохранение)
                Toast.makeText(this, "Введено: $amountText ₽", Toast.LENGTH_SHORT).show()
                finish() // Закрываем диалог
            } else {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            }
        }
    }
}