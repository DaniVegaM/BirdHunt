package pl.jojczak.birdhunt.base

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import pl.jojczak.birdhunt.assetsloader.Asset
import pl.jojczak.birdhunt.assetsloader.AssetsLoader
import pl.jojczak.birdhunt.utils.Preferences
import pl.jojczak.birdhunt.utils.Preferences.PREF_GAME_SCALE

abstract class BaseStage(
    protected val viewportMinWidth: Float = WORLD_WIDTH,
    protected val viewportMinHeight: Float = WORLD_HEIGHT
) : Stage(
    ExtendViewport(viewportMinWidth, viewportMinHeight)
) {
    protected val skin = AssetsLoader.get<Skin>(Asset.UI_SKIN)
    protected val i18N = AssetsLoader.get<I18NBundle>(Asset.I18N)

    protected var gameScale = Preferences.get(PREF_GAME_SCALE)

    private val gameScaleListener = Preferences.PreferenceListener<Float> {
        gameScale = it
        onResize(Gdx.graphics.width, Gdx.graphics.height)
    }

    private val subClassName = this::class.simpleName

    private fun fadeInAction(callback: () -> Unit) = SequenceAction(
        ColorAction().apply {
            this.color = root.color
            this.endColor = root.color.cpy().apply { a = 1f }
            this.duration = FADE_DURATION
        },
        RunnableAction().apply {
            setRunnable {
                callback()
            }
        }
    )

    private fun fadeOutAction(callback: () -> Unit) = SequenceAction(
        ColorAction().apply {
            this.color = root.color
            this.endColor = root.color.cpy().apply { a = 0f }
            this.duration = FADE_DURATION
        },
        RunnableAction().apply {
            setRunnable {
                callback()
            }
        }
    )

    init {
        root.color.a = 0f
        Preferences.addListener(PREF_GAME_SCALE, gameScaleListener)
    }

    // TBH, I have no idea why a NullPointerException occurs here. The issue appears randomly
    // in com.badlogic.gdx.scenes.scene2d.ui.Table.computeSize (Table.java:806), or I just
    // haven't noticed a pattern yet. Either way, debugging this will take a lot of time and
    // this workaround works - the app doesn’t crash, and there are no changes in gameplay.
    // Since this seems to be the most common crash according to the stats, I’m leaving
    // it like this for now.
    override fun act(delta: Float) {
        try {
            super.act(delta)
        } catch (e: NullPointerException) {
            Gdx.app.error("$TAG/$subClassName", "NullPointerException in act()", e)
            safelyEndBatch()
        } catch (e: ArrayIndexOutOfBoundsException) {
            Gdx.app.error("$TAG/$subClassName", "ArrayIndexOutOfBoundsException in act()", e)
            safelyEndBatch()
        } catch (e: IndexOutOfBoundsException) {
            Gdx.app.error("$TAG/$subClassName", "IndexOutOfBoundsException in act()", e)
            safelyEndBatch()
        } catch (e: IllegalStateException) {
            Gdx.app.error("$TAG/$subClassName", "IllegalStateException in act()", e)
            safelyEndBatch()
        } catch (e: Exception) {
            Gdx.app.error("$TAG/$subClassName", "Unexpected exception in act()", e)
            safelyEndBatch()
        }
    }

    override fun draw() {
        viewport.apply()

        try {
            super.draw()
        } catch (e: NullPointerException) {
            Gdx.app.error("$TAG/$subClassName", "NullPointerException in draw()", e)
            safelyEndBatch()
        } catch (e: ArrayIndexOutOfBoundsException) {
            Gdx.app.error("$TAG/$subClassName", "ArrayIndexOutOfBoundsException in draw()", e)
            safelyEndBatch()
        } catch (e: IllegalStateException) {
            Gdx.app.error("$TAG/$subClassName", "IllegalStateException in draw()", e)
            safelyEndBatch()
        } catch (e: IndexOutOfBoundsException) {
            Gdx.app.error("$TAG/$subClassName", "IndexOutOfBoundsException in draw()", e)
            safelyEndBatch()
        } catch (e: Exception) {
            Gdx.app.error("$TAG/$subClassName", "Unexpected exception in act()", e)
            safelyEndBatch()
        }
    }

    private fun safelyEndBatch() {
        try {
            if (super.getBatch().isDrawing) {
                super.getBatch().end()
            }
        } catch (e: Exception) {
            Gdx.app.error("$TAG/$subClassName", "Exception during getBatch().end()", e)
        }
    }

    fun fadeIn(callback: () -> Unit = {}) {
        addAction(fadeInAction(callback))
    }

    fun fadeOut(callback: () -> Unit = {}) {
        addAction(fadeOutAction(callback))
    }

    open fun onFirstFrame() = Unit

    open fun onResize(scrWidth: Int, scrHeight: Int) {
        val ratioScale = getViewportScaleByRatio(scrWidth, scrHeight)

        viewport = ExtendViewport(
            viewportMinWidth * ratioScale * gameScale,
            viewportMinHeight * ratioScale * gameScale
        )
        viewport.update(scrWidth, scrHeight, true)

        for (actor in actors) {
            if (actor is BaseActor) {
                actor.onResize(scrWidth, scrHeight)
            }
        }
    }

    protected fun getViewportScaleByRatio(scrWidth: Int, scrHeight: Int) = maxOf(
        1f,
        if (scrWidth < scrHeight) {
            (scrWidth.toFloat() / scrHeight) * 1.6f
        } else {
            (scrHeight.toFloat() / scrWidth) * 1.6f
        }
    )

    override fun dispose() {
        Preferences.removeListener(PREF_GAME_SCALE, gameScaleListener)
        for (actor in actors) {
            if (actor is DisposableActor) actor.dispose()
        }
        super.dispose()
    }

    override fun actorRemoved(actor: Actor) {
        if (actor is DisposableActor) actor.dispose()
        super.actorRemoved(actor)
    }

    companion object {
        private const val TAG = "BaseStage"
        const val WORLD_WIDTH = 200f
        const val WORLD_HEIGHT = 200f

        private const val FADE_DURATION = 0.20f
    }
}
