package pl.jojczak.birdhunt.screens.gameplay.stages.ui.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import pl.jojczak.birdhunt.assetsloader.Asset
import pl.jojczak.birdhunt.base.BaseTable
import pl.jojczak.birdhunt.os.helpers.PlayServicesHelper
import pl.jojczak.birdhunt.os.helpers.playServicesHelperInstance
import pl.jojczak.birdhunt.screens.gameplay.GameplayLogic
import pl.jojczak.birdhunt.screens.gameplay.GameplayState
import pl.jojczak.birdhunt.utils.ButtonListener

class GameOverWindow(
    private val i18N: I18NBundle,
    skin: Skin,
    private val gameplayLogic: GameplayLogic
) : BaseTable(), GameplayLogic.FromActions {
    
    private val titleLabel = Label(
        "Bird Hunt by Daniel Vega", 
        skin,
        Asset.FONT_MEDIUM_BORDERED, 
        Color.WHITE
    ).also { tL ->
        tL.setAlignment(Align.center)
    }

    private val gameOverLabel = Label("", skin, Asset.FONT_MEDIUM_BORDERED, Color.WHITE).apply {
        setAlignment(Align.center)
    }

    private val restartButton = TextButton(i18N.get("game_over_bt_restart"), skin).apply {
        addListener(ButtonListener { _, _ ->
            fadeOut {
                gameplayLogic.onAction(GameplayLogic.ToActions.RestartGame)
            }
        })
    }

    private val shareButton = ImageTextButton(i18N.get("bt_share"), skin, "share").apply {
        addListener(ButtonListener { _, _ ->
            Gdx.app.log(TAG, "Share button clicked")
            // Simple share without screenshot for now
            Gdx.net.openURI("https://birdhunt.app") // Share app URL
        })
    }

    private val exitButton = TextButton(i18N.get("exit_bt"), skin).apply {
        addListener(ButtonListener { _, _ ->
            gameplayLogic.onAction(GameplayLogic.ToActions.ExitGame)
            isDisabled = true
        })
    }

    private val window = Window(i18N.get("game_over"), skin).apply {
        isMovable = false
        isResizable = false

        add(restartButton).padTop(PAD).row()
        add(shareButton).padTop(PAD).row()
        add(exitButton).padTop(PAD)

        align(Align.top)
    }

    init {
        setFillParent(true)
        onOrientationChange(isOrientationVertical)
    }

    override fun onOrientationChange(vertical: Boolean) {
        clearChildren()

        if (vertical) {
            add(titleLabel).adjustGameTitle().row()
            add(gameOverLabel).padTop(PAD).row()
            add(window).padTop(PAD).expandY().align(Align.top)
        } else {
            add(titleLabel).adjustGameTitle().expand().uniformX()
            add(window)
            add(gameOverLabel).expandX().uniformX()
        }
    }

    private fun Cell<Label>.adjustGameTitle(): Cell<Label> {
        width(TITLE_WIDTH)
        height(TITLE_HEIGHT)
        return this
    }

    override fun gameplayStateUpdate(state: GameplayState) {
        if (state is GameplayState.GameOver) {
            playServicesHelperInstance.getUserName {
                val userName = it ?: DEF_PLAYER_NAME

                gameOverLabel.setText(i18N.format("game_over_score", userName, state.points))

                // Leaderboard update for points
                playServicesHelperInstance.submitScore(state.points)
            }
        }
    }

    companion object {
        private const val TAG = "GameOverWindow"
        private const val PAD = 20f
        private const val DEF_PLAYER_NAME = "Player"
        private const val TITLE_WIDTH = 300f
        private const val TITLE_HEIGHT = 60f
    }
}
