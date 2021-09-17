/*
 * Copyright 2021 Artyom Mironov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luteapp.nanji.model

import java.util.Calendar
import java.util.Locale

class TimeKo(
  private val useWords: Boolean,
  private val useTwentyFourHours: Boolean
) : Time {
  override fun getPercentText(value: Int): String = value.toWords() + '％'

  override fun getDateText(cal: Calendar): String {
    val year = cal.year.toWords()
    val month = cal.monthNum.toWords()
    val day = cal.day.toWords()
    val weekday = cal.weekday(Locale.KOREAN)
    return "${year}년${month}월${day}일${weekday}"
  }

  override fun getTimeText(cal: Calendar): String {
    val hourOfDay = cal.hourOfDay
    val ampm = when {
      useTwentyFourHours -> ""
      hourOfDay in 0..5 -> "새벽"
      hourOfDay in 6..11 -> "오전"
      hourOfDay in 12..17 -> "오후"
      hourOfDay in 18..20 -> "저녁"
      else -> "밤"
    }
    val h = if (useTwentyFourHours) cal.hourOfDay else cal.hour12
    val hour = when {
      !useWords -> "$h"
      else -> listOf(
        "영", "한", "두", "세", "네", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "열한",
        "열두", "열세", "열네", "열다섯", "열여섯", "열일곱", "열여덟", "열아홉", "스물", "스물한", "스물두", "스물세"
      ).getOrNull(h).orEmpty()
    }
    val minute = cal.minute.toWords()
    return "${ampm}${hour}시${minute}분"
  }

  private val Int.word: String get() = when (this) {
    0 -> "영"
    1 -> "일"
    2 -> "이"
    3 -> "삼"
    4 -> "사"
    5 -> "오"
    6 -> "육"
    7 -> "칠"
    8 -> "팔"
    9 -> "구"
    10 -> "십"
    100 -> "백"
    1000 -> "천"
    else -> ""
  }

  private fun Int.toWords(): String = if (useWords) toWordsCJK { it.word } else toString()
}
