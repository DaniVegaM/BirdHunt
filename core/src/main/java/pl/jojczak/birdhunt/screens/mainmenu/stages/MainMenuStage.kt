package pl.jojczak.birdhunt.screens.mainmenu.stages

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import pl.jojczak.birdhunt.assetsloader.Asset
import pl.jojczak.birdhunt.assetsloader.AssetsLoader
import pl.jojczak.birdhunt.base.ScreenWithUIStage
import pl.jojczak.birdhunt.main.MainAction
import pl.jojczak.birdhunt.utils.ButtonListener
import pl.jojczak.birdhunt.utils.DisabledButtonListener
import pl.jojczak.birdhunt.utils.Preferences
import pl.jojczak.birdhunt.utils.Preferences.PREF_HIGH_SCORE
import pl.jojczak.birdhunt.utils.Preferences.PREF_PGS_AUTH
import pl.jojczak.birdhunt.os.helpers.playServicesHelperInstance

class MainMenuStage : ScreenWithUIStage.ScreenStage() {
    private var orientationVertical: Boolean? = null

    private val pgsPreferenceListener = Preferences.PreferenceListener(::onPgsAuthChanged)

    private val startGameButton = TextButton(i18N.get("bt_start_game"), skin).also { sgB ->
        sgB.addListener(ButtonListener { _, _ ->
            Gdx.app.log(TAG, "Start button clicked")
            if (Preferences.get(Preferences.PREF_FIRST_GAME)) {
                fadeOut { mainActionReceiver(MainAction.NavigateToControls(firstGame = true)) }
                Preferences.put(Preferences.PREF_FIRST_GAME, false)
                Preferences.flush()
            } else {
                fadeOut { mainActionReceiver(MainAction.NavigateToGameplay) }
            }
        })
    }

    private val aboutButton = TextButton(i18N.get("bt_about"), skin).also { aB ->
        aB.addListener(ButtonListener { _, _ ->
            Gdx.app.log(TAG, "About button clicked")
            fadeOut { mainActionReceiver(MainAction.NavigateToAbout) }
        })
    }

    private val penMouseSAdButton = ImageButton(skin, "penmouse_s").also { pm ->
        pm.addListener(ButtonListener { _, _ ->
            Gdx.app.log(TAG, "PenMouse button clicked")
            Gdx.net.openURI(i18N.get("main_menu_penmouse_s_url"))
        })
    }

    private val settingsButton = ImageButton(skin, "settings").also { sB ->
        sB.addListener(ButtonListener { _, _ ->
            Gdx.app.log(TAG, "Settings button clicked")
            fadeOut { mainActionReceiver(MainAction.NavigateToSettings) }
        })
    }

    private val leaderboardButton = ImageButton(skin, "gp_leaderboard").also { lB ->
        lB.addListener(DisabledButtonListener { _ ->
            Gdx.app.log(TAG, "Leaderboard button clicked")
            playServicesHelperInstance.showLeaderboard()
        })
        lB.isVisible = false // Ocultar botón
    }

    private val achievementsButton = ImageButton(skin, "gp_achievements").also { lB ->
        lB.addListener(DisabledButtonListener { _ ->
            Gdx.app.log(TAG, "Achievements button clicked")
            playServicesHelperInstance.showAchievements()
        })
        lB.isVisible = false // Ocultar botón
    }

    private val highScoreLabel = Label(
        i18N.format("main_menu_high_score", Preferences.get(PREF_HIGH_SCORE)),
        skin,
        Asset.FONT_MEDIUM_BORDERED,
        Color.WHITE
    )

    private val titleLabel = Label(
        "Bird Hunt by Daniel Vega", 
        skin,
        Asset.FONT_MEDIUM_BORDERED, 
        Color.WHITE
    ).also { tL ->
        tL.setAlignment(Align.center)
    }

    private var currentTable: Table? = null

    init {
        Gdx.app.log(TAG, "init MainMenuStage")
        Preferences.addListener(PREF_PGS_AUTH, pgsPreferenceListener)
        onPgsAuthChanged(Preferences.get(PREF_PGS_AUTH))
        
        // Initialize the table immediately
        currentTable = if (viewport.worldHeight > viewport.worldWidth) {
            orientationVertical = true
            getVerticalTable()
        } else {
            orientationVertical = false
            getHorizontalTable()
        }
        addActor(currentTable)
    }

    override fun onResize(scrWidth: Int, scrHeight: Int) {
        super.onResize(scrWidth, scrHeight)

        if (viewport.worldHeight > viewport.worldWidth && orientationVertical != true) {
            Gdx.app.log(TAG, "Orientation changed to vertical")
            orientationVertical = true
            currentTable?.remove()
            currentTable = getVerticalTable()
            addActor(currentTable)
        } else if (viewport.worldWidth > viewport.worldHeight && orientationVertical != false) {
            Gdx.app.log(TAG, "Orientation changed to horizontal")
            orientationVertical = false
            currentTable?.remove()
            currentTable = getHorizontalTable()
            addActor(currentTable)
        }
    }

    private fun getHorizontalTable() = Table().also { cT ->
        cT.setFillParent(true)
        cT.center().top()

        cT.add(titleLabel).expandX().align(Align.top)

        cT.add().expandX()

        cT.add(Table().also { bT ->
            bT.add(startGameButton).padBottom(ROW_PAD).row()
            bT.add(aboutButton).padBottom(ROW_PAD).row()
            bT.add(settingsButton).padBottom(ROW_PAD).row()
            bT.add(highScoreLabel).row()
            bT.add(penMouseSAdButton).padTop(ROW_PAD * 1.5f).row()
        }).expand().center().padRight(50f)
    }

    private fun getVerticalTable() = Table().also { cT ->
        cT.setFillParent(true)
        cT.center().top()

        cT.add(titleLabel).expandX().align(Align.top)

        cT.row()

        cT.add(Table().also { bT ->
            bT.add(startGameButton).padBottom(ROW_PAD).row()
            bT.add(aboutButton).padBottom(ROW_PAD).row()
            bT.add(settingsButton).padBottom(ROW_PAD).row()
            bT.add(highScoreLabel).row()
            bT.add(penMouseSAdButton).padTop(ROW_PAD * 1.5f).row()
        }).expand().top().padTop(50f)
    }

    private fun onPgsAuthChanged(pgsAuthEnabled: Boolean) {
        leaderboardButton.isDisabled = !pgsAuthEnabled
        achievementsButton.isDisabled = !pgsAuthEnabled
    }

    override fun keyDown(keyCode: Int) = if (keyCode == Input.Keys.BACK) {
        Gdx.app.exit()
        true
    } else super.keyDown(keyCode)

    override fun dispose() {
        Preferences.removeListener(PREF_PGS_AUTH, pgsPreferenceListener)
        super.dispose()
    }

    companion object {
        private const val TAG = "MainMenuStage"
        private const val ROW_PAD = 25f
        private const val FONT_SCALE = 1f
    }
}
