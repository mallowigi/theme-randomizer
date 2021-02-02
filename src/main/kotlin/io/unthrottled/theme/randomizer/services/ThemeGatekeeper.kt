package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import javax.swing.UIManager

class ThemeGatekeeper : Disposable {
  companion object {
    val instance: ThemeGatekeeper
      get() = ServiceManager.getService(ThemeGatekeeper::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private var preferredCharactersIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.selectedThemes)

  private fun extractAllowedCharactersFromState(characterConfig: String): Set<String> =
    characterConfig.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .toSet()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        preferredCharactersIds = extractAllowedCharactersFromState(newPluginState.selectedThemes)
      }
    )
  }

  fun isPreferred(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    preferredCharactersIds.contains(
      getId(lookAndFeelInfo)
    )

  fun getId(lookAndFeelInfo: UIManager.LookAndFeelInfo): String =
    when (lookAndFeelInfo) {
      is UIThemeBasedLookAndFeelInfo -> lookAndFeelInfo.theme.id
      else -> lookAndFeelInfo.name
    }

  override fun dispose() {
    connection.dispose()
  }
}