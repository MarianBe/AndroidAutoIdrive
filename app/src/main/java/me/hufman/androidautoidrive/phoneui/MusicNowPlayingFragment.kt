package me.hufman.androidautoidrive.phoneui

import android.arch.lifecycle.ViewModelProviders
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.music_nowplaying.*
import me.hufman.androidautoidrive.R
import me.hufman.androidautoidrive.Utils
import me.hufman.androidautoidrive.getThemeColor
import me.hufman.androidautoidrive.music.MusicController

class MusicNowPlayingFragment: Fragment() {
	companion object {
		const val ARTIST_ID = "150.png"
		const val ALBUM_ID = "148.png"
		const val SONG_ID = "152.png"
		const val PLACEHOLDER_ID = "147.png"
	}

	lateinit var musicController: MusicController
	lateinit var placeholderCoverArt: Bitmap

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.music_nowplaying, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val viewModel = ViewModelProviders.of(requireActivity()).get(MusicActivityModel::class.java)
		val musicController = viewModel.musicController ?: return
		this.musicController = musicController

		imgArtist.setImageBitmap(viewModel.icons[ARTIST_ID])
		imgAlbum.setImageBitmap(viewModel.icons[ALBUM_ID])
		imgSong.setImageBitmap(viewModel.icons[SONG_ID])
		imgArtist.colorFilter = Utils.getIconMask(context!!.getThemeColor(android.R.attr.textColorSecondary))
		imgAlbum.colorFilter = Utils.getIconMask(context!!.getThemeColor(android.R.attr.textColorSecondary))
		imgSong.colorFilter = Utils.getIconMask(context!!.getThemeColor(android.R.attr.textColorSecondary))
		placeholderCoverArt = viewModel.icons[PLACEHOLDER_ID]!!

		btnPrevious.setOnClickListener { musicController.skipToPrevious() }
		btnPlay.setOnClickListener {
			if (musicController.getPlaybackPosition().playbackPaused) {
				musicController.play()
			} else {
				musicController.pause()
			}
		}
		btnNext.setOnClickListener { musicController.skipToNext() }
		musicController.listener = Runnable { redraw() }

		seekProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
			override fun onProgressChanged(seekbar: SeekBar?, value: Int, fromUser: Boolean) {
				if (fromUser) {
					musicController.controller?.transportControls?.seekTo(value * 1000L)
				}
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {
			}
			override fun onStopTrackingTouch(p0: SeekBar?) {
			}

		})
	}

	fun redraw() {
		if (!isVisible) return
		val metadata = musicController.getMetadata()
		if (metadata?.coverArt != null) {
			imgCoverArt.setImageBitmap(metadata.coverArt)
		} else {
			imgCoverArt.setImageBitmap(placeholderCoverArt)
		}
		txtArtist.text = metadata?.artist ?: (if (musicController.controller == null) getString(R.string.nowplaying_notconnected) else "")
		txtAlbum.text = metadata?.album
		txtSong.text = metadata?.title ?: getString(R.string.nowplaying_unknown)

		if (musicController.getPlaybackPosition().playbackPaused) {
			btnPlay.setImageResource(android.R.drawable.ic_media_play)
		} else {
			btnPlay.setImageResource(android.R.drawable.ic_media_pause)
		}
		val position = musicController.getPlaybackPosition()
		seekProgress.progress = (position.getPosition() / 1000).toInt()
		seekProgress.max = (position.maximumPosition / 1000).toInt()
	}
}