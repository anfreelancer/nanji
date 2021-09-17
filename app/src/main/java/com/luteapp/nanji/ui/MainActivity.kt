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

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artyommironov.anyadapter.AnyAdapter
import com.luteapp.nanji.BillingClientSetup
import com.luteapp.nanji.R
import com.luteapp.nanji.ShopActivity
import com.luteapp.nanji.databinding.ItemBinding
import com.luteapp.nanji.model.Language
import com.luteapp.nanji.model.Prefs
import com.luteapp.nanji.model.year
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
  private val prefs by lazy { Prefs(PreferenceManager.getDefaultSharedPreferences(this)) }
  private val diffUtilItemCallback = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return oldItem is Item && newItem is Item && oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = false
  }
  private val myAdapter = AnyAdapter(diffUtilItemCallback)
    .map { ActionHolder(getItemBinding(it)) }
    .map { SwitchHolder(getItemBinding(it)) }
    .map { EditHolder(getItemBinding(it), ::showDialog) }
    .map { SelectorHolder(getItemBinding(it), ::showDialog) }
  private var currentDialog: Dialog? = null
  private var shouldHideTime: Boolean
    get() = prefs.hideTime
    set(value) {
      prefs.hideTime = value
      render()
    }
  private var tapActionIndex: Int
    get() = TapAction.values().indexOf(prefs.tapAction)
    set(value) {
      prefs.tapAction = TapAction.values()[value]
    }
  private var languageIndex: Int
    get() = Language.values().indexOf(prefs.language)
    set(value) {
      prefs.language = Language.values()[value]
      render()
    }
  private val timeZones: List<String> = listOf("") + TimeZone.getAvailableIDs()
  private var timeZoneIndex: Int
    get() = timeZones.indexOf(prefs.timeZone)
    set(value) {
      prefs.timeZone = timeZones[value]
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(
      RecyclerView(this).apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        adapter = myAdapter
      }
    )
    render()
  }

  override fun onPause() {
    super.onPause()
    val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, WidgetProvider::class.java))
    sendBroadcast(
      Intent(this, WidgetProvider::class.java)
        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    )
    currentDialog?.dismiss()
  }

  private fun render() {
    val languageStrings = Language.values().map { getString(it.title) }
    val tapActionStrings = TapAction.values().map { getString(it.title) }
    val timeZoneStrings = timeZones.map { it.ifEmpty { getString(R.string.languageSystem) } }
    val messageReplaceDigits = String.format(TEXT_REPLACE_DIGITS_EXAMPLE, getString(R.string.prefsExamples))
    val canShowWords = when (shouldHideTime) {
      true -> prefs.language in setOf(Language.zhCN, Language.zhTW, Language.ja, Language.ko)
      false -> prefs.language != Language.system
    }

    myAdapter.submitList(
      listOfNotNull(
        ActionItem(R.string.appearance, ::goAppearance),
        SelectorItem(R.string.language, languageStrings, ::languageIndex),
        SelectorItem(R.string.prefsTapAction, tapActionStrings, ::tapActionIndex),
        SwitchItem(R.string.prefsHideTime, ::shouldHideTime),
        SwitchItem(R.string.prefsShowWords, prefs::showWords).takeIf { canShowWords },
        SwitchItem(R.string.prefsTwentyFour, prefs::twentyFour).takeIf { !shouldHideTime },
        SwitchItem(R.string.prefsShowBattery, prefs::showBattery),
        SwitchItem(R.string.japaneseEra, prefs::japaneseEra).takeIf { prefs.language == Language.ja },
        SelectorItem(R.string.prefsTimeZone, timeZoneStrings, ::timeZoneIndex),
        EditItem(R.string.prefsReplaceChars, messageReplaceDigits, prefs::customSymbols),
        ActionItem(R.string.premium, ::startShopActivity),
        ActionItem(R.string.about, ::showAboutAlert)

      )
    )
  }

  private fun getItemBinding(parent: ViewGroup): ItemBinding {
    return ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
  }

  private fun showDialog(dialog: Dialog) {
    if (BillingClientSetup.isUpgraded(applicationContext)) {
      currentDialog?.dismiss()
      currentDialog = dialog
      currentDialog?.show()
    }

  }

  private fun goAppearance() {
    if (BillingClientSetup.isUpgraded(applicationContext)) {
      startActivity(Intent(this, AppearanceActivity::class.java))
    }
  }

  private fun goEmail(appName: String, appVersion: String) {
    val deviceInfo = "${Build.BRAND} ${Build.MODEL} (${Build.VERSION.SDK_INT})"
    Intent(Intent.ACTION_SEND)
      .setType("plain/text")
      .putExtra(Intent.EXTRA_EMAIL, arrayOf("artyommironov@gmail.com"))
      .putExtra(Intent.EXTRA_SUBJECT, "$appName $appVersion $deviceInfo")
      .takeIf { it.resolveActivity(packageManager) != null }
      ?.let(::startActivity)
  }

  private fun startShopActivity() {
    startActivity(Intent(this@MainActivity, ShopActivity::class.java))
  }

  private fun showAboutAlert() {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val appName = getString(R.string.appName)
    val appVersion = packageInfo.versionName
    val year = Calendar.getInstance().year
    AlertDialog.Builder(this)
      .setTitle("$appName $appVersion")
      .setMessage(String.format(LICENSE, year))
      .setPositiveButton(android.R.string.ok, null)
      .setNegativeButton(R.string.prefsFeedback) { _, _ -> goEmail(appName, appVersion) }
      .create()
      .let(::showDialog)
  }
}

private const val TEXT_REPLACE_DIGITS_EXAMPLE = "ABCDEF = A->B C->D E->F\n%s\n零〇~♥\n一壱二弐三参十拾"

private const val LICENSE = """Copyright %d Artyom Mironov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
