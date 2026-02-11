package com.stvalentin.finance.data

import androidx.room.TypeConverter

class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): Int {
        return type.ordinal
    }
    
    @TypeConverter
    fun toTransactionType(ordinal: Int): TransactionType {
        return TransactionType.values()[ordinal]
    }
}