package com.mpcorporation.snapeffect.presentation.language

import androidx.annotation.DrawableRes
import com.mpcorporation.snapeffect.R

/**
 * Cờ hiển thị đầu dòng ngôn ngữ (cùng bộ cờ + cách map với AdmobBase). Map ở presentation để
 * [com.mpcorporation.snapeffect.domain.model.AppLanguage] không phụ thuộc resource Android.
 *
 * Trả về null chỉ khi thêm ngôn ngữ mới mà quên map cờ -> dòng đó hiện badge mã ngôn ngữ.
 */
@DrawableRes
internal fun flagFor(code: String): Int? = when (code) {
    "en" -> R.drawable.ic_flag_uk
    "hi" -> R.drawable.ic_flag_india
    "fr" -> R.drawable.ic_flag_france
    "es" -> R.drawable.ic_flag_spain
    "pt" -> R.drawable.ic_flag_portugal
    "ar" -> R.drawable.ic_flag_arab
    "bg" -> R.drawable.ic_flag_bulgary
    "cs" -> R.drawable.ic_flag_czech
    "da" -> R.drawable.ic_flag_denmark
    "de" -> R.drawable.ic_flag_germany
    "el" -> R.drawable.ic_flag_greek
    "fa" -> R.drawable.ic_flag_iran
    "fi" -> R.drawable.ic_flag_finland
    "fil" -> R.drawable.ic_flag_filipino
    "gu" -> R.drawable.ic_flag_india
    "hu" -> R.drawable.ic_flag_hungary
    "in" -> R.drawable.ic_flag_indonesia
    "it" -> R.drawable.ic_flag_italy
    "iw" -> R.drawable.ic_flag_israel
    "ja" -> R.drawable.ic_flag_japan
    "kn" -> R.drawable.ic_flag_india
    "ko" -> R.drawable.ic_flag_south_korea
    "ml" -> R.drawable.ic_flag_india
    "mr" -> R.drawable.ic_flag_india
    "ms" -> R.drawable.ic_flag_malaysia
    "nl" -> R.drawable.ic_flag_netherlands
    "pa" -> R.drawable.ic_flag_pakistan
    "pl" -> R.drawable.ic_flag_poland
    "ro" -> R.drawable.ic_flag_romania
    "ru" -> R.drawable.ic_flag_russia
    "sr" -> R.drawable.ic_flag_serbia
    "sv" -> R.drawable.ic_flag_sweden
    "ta" -> R.drawable.ic_flag_india
    "te" -> R.drawable.ic_flag_india
    "th" -> R.drawable.ic_flag_thailand
    "tr" -> R.drawable.ic_flag_turkey
    "uk" -> R.drawable.ic_flag_ukraine
    "ur" -> R.drawable.ic_flag_pakistan
    "zh" -> R.drawable.ic_flag_china
    "zh_TW" -> R.drawable.ic_flag_taiwan
    "vi" -> R.drawable.ic_flag_vietnam
    "pt_BR" -> R.drawable.ic_flag_brazil
    "es_MX" -> R.drawable.ic_flag_mexico
    "ur_PK" -> R.drawable.ic_flag_pakistan
    "ar_AE" -> R.drawable.ic_flag_uae
    "ar_SA" -> R.drawable.ic_flag_arab
    else -> null
}
