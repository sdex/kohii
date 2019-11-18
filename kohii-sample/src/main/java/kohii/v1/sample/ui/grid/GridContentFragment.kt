/*
 * Copyright (c) 2019 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kohii.v1.sample.ui.grid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import kohii.core.Master
import kohii.core.Master.MemoryMode
import kohii.core.Rebinder
import kohii.v1.sample.BuildConfig
import kohii.v1.sample.R
import kohii.v1.sample.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_debug_child.container
import kotlin.LazyThreadSafetyMode.NONE

class GridContentFragment : BaseFragment() {

  private var callback: Callback? = null

  private val master by lazy(NONE) { Master[this] }
  private val adapter by lazy(NONE) {
    ItemsAdapter(
        master,
        shouldBindVideo = { !selectionTracker.isSelected(it) },
        onVideoClick = { callback?.onSelected(it) }
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    callback = parentFragment as? Callback
  }

  override fun onDetach() {
    super.onDetach()
    callback = null
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_debug_child, container, false)
  }

  private lateinit var selectionTracker: SelectionTracker<Rebinder>
  private lateinit var videoKeyProvider: VideoTagKeyProvider
  private lateinit var videoItemDetailsLookup: VideoItemDetailsLookup

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    master.register(this, MemoryMode.BALANCED)
        .attach(container)

    val spanCount = resources.getInteger(R.integer.grid_span)
    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position % 6 == 3 || position % 6 == spanCount) 2 else 1
      }
    }

    (container.layoutManager as? GridLayoutManager)?.spanSizeLookup = spanSizeLookup
    container.adapter = adapter

    videoKeyProvider = VideoTagKeyProvider(container)
    videoItemDetailsLookup = VideoItemDetailsLookup(container)

    selectionTracker = SelectionTracker.Builder<Rebinder>(
        "${BuildConfig.APPLICATION_ID}::sample::debug",
        container,
        videoKeyProvider,
        videoItemDetailsLookup,
        StorageStrategy.createParcelableStorage(Rebinder::class.java)
    )
        .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
        .build()

    selectionTracker.onRestoreInstanceState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    selectionTracker.onSaveInstanceState(outState)
  }

  internal fun select(rebinder: Rebinder) {
    selectionTracker.select(rebinder)
  }

  internal fun deselect(rebinder: Rebinder) {
    selectionTracker.deselect(rebinder)
  }

  interface Callback {

    fun onSelected(rebinder: Rebinder)
  }
}
