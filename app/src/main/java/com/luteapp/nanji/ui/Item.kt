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

package com.luteapp.nanji.ui

import androidx.annotation.StringRes
import kotlin.reflect.KMutableProperty0

sealed class Item(@StringRes val title: Int)

class ActionItem(
  title: Int,
  val onClick: () -> Unit
) : Item(title)

class SwitchItem(
  title: Int,
  val property: KMutableProperty0<Boolean>
) : Item(title)

class EditItem(
  title: Int,
  val message: String,
  val property: KMutableProperty0<String>
) : Item(title)

class SelectorItem(
  title: Int,
  val items: List<String>,
  val indexProperty: KMutableProperty0<Int>
) : Item(title)

