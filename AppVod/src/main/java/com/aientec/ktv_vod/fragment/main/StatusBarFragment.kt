package com.aientec.ktv_vod.fragment.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aientec.ktv_vod.BuildConfig
import com.aientec.ktv_vod.R
import com.aientec.ktv_vod.databinding.*
import com.aientec.ktv_vod.viewmodel.*
import com.aientec.structure.Track
import com.aientec.structure.User
import com.squareup.picasso.Picasso
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class StatusBarFragment : Fragment() {
      private lateinit var binding: FragmentStatusBarBinding

      private val roomViewModel: RoomViewModel by activityViewModels()

      private val userViewModel: UserViewModel by activityViewModels()

      private val trackViewModel: TrackViewModel by activityViewModels()

      private val uiViewModel: UiViewModel by activityViewModels()

      private val systemViewModel: SystemViewModel by activityViewModels()

      private lateinit var languageListBinding: PopLanguageListBinding

      private lateinit var userListBinding: ViewUserListBinding

      private lateinit var serviceListBinding: PopServiceListBinding

      private lateinit var playListBinding: PopPlayingTrackListBinding

      private lateinit var menuBinding: PopSettingOldBinding

      private lateinit var buyCarBinding: PopBuyCarBinding

      private val playingItemAdapter: PlayingItemAdapter = PlayingItemAdapter()

      private val userItemAdapter: UserItemAdapter = UserItemAdapter()

      private var showPopup: SimpleTooltip? = null

      private lateinit var picasso: Picasso

      override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
      ): View {
            binding = FragmentStatusBarBinding.inflate(inflater, container, false)
            initViews()
            return binding.root
      }

      @SuppressLint("SetTextI18n")
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            picasso = Picasso.get()

            playListBinding.list.apply {
                  layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                  adapter = playingItemAdapter
            }

            playListBinding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                  playingItemAdapter.filterType = when (checkedId) {
                        R.id.rb_history -> 1
                        else -> 0
                  }
            }

            playListBinding.playlist.setOnClickListener {
                  uiViewModel.onControlActionEvent("ACTION:PLAY_LIST")
                  closePopupWindow()
            }

            serviceListBinding.summit.setOnClickListener {
                  serviceListBinding.cup.isChecked = false
                  serviceListBinding.ice.isChecked = false
                  serviceListBinding.paper.isChecked = false
                  serviceListBinding.clean.isChecked = false
                  serviceListBinding.check.isChecked = false
                  serviceListBinding.cupCount.text = "1"
                  Toast.makeText(requireContext(), "已呼叫服務鈴", Toast.LENGTH_LONG).show()
                  closePopupWindow()
            }

            serviceListBinding.cupAdd.setOnClickListener {
                  val count: Int = serviceListBinding.cupCount.text.toString().toInt()
                  if (count < 10) {
                        serviceListBinding.cupCount.text = (count + 1).toString()
                  }
            }

            serviceListBinding.cupDesc.setOnClickListener {
                  val count: Int = serviceListBinding.cupCount.text.toString().toInt()
                  if (count > 1) {
                        serviceListBinding.cupCount.text = (count - 1).toString()
                  }
            }





            binding.home.setOnClickListener {
                  uiViewModel.onControlActionEvent("ACTION:HOME")
            }

            binding.logo.setOnClickListener {
                  uiViewModel.onControlActionEvent("ACTION:HOME")
            }

            binding.search.setOnClickListener {
                  uiViewModel.onControlActionEvent("ACTION:SONG_SEARCHING")
            }

            binding.language.setOnClickListener(onClickListener)

            binding.user.setOnClickListener(onClickListener)

            binding.userLayout.setOnClickListener { binding.user.performClick() }

            binding.tracks.setOnClickListener(onClickListener)

            binding.service.setOnClickListener(onClickListener)

            binding.menu.setOnClickListener(onClickListener)

            binding.buyCar.setOnClickListener(onClickListener)

            binding.foodOrder.setOnClickListener {
                  uiViewModel.onControlActionEvent("ACTION:FOOD_ORDER")
            }

            systemViewModel.qrCodeData?.let {
                  it.observe(viewLifecycleOwner) { data ->
                        if (data != null) {
                              userListBinding.qrcode.setImageBitmap(BitmapFactory.decodeFile(data))
                              menuBinding.qrcode.setImageBitmap(BitmapFactory.decodeFile(data))
                        }
                  }
            }

            systemViewModel.configuration.observe(viewLifecycleOwner) {
                  if (it != null) {
                        binding.roomName.text = it.roomName
                  }
            }

            trackViewModel.searchingText.observe(viewLifecycleOwner) {

            }

            uiViewModel.quickSearch.observe(viewLifecycleOwner) {
                  Log.d("Trace", "Quick : $it")

            }

            trackViewModel.playingTracks.observe(viewLifecycleOwner) { list ->
                  playingItemAdapter.list = list
                  val trackCount: Int = list.count { it.state != Track.State.DONE }
                  if (trackCount == 0) {
                        binding.trackCount.visibility = View.INVISIBLE
                  } else {
                        binding.trackCount.visibility = View.VISIBLE
                        binding.trackCount.text = trackCount.toString()
                  }
            }



            userListBinding.list.apply {

                  val dec: DividerItemDecoration =
                        DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

                  dec.setDrawable(
                        ColorDrawable(
                              ContextCompat.getColor(
                                    requireContext(),
                                    R.color.colorGrayLight
                              )
                        )
                  )

                  addItemDecoration(dec)

                  val manager = GridLayoutManager(
                        requireContext(),
                        2,
                        GridLayoutManager.VERTICAL,
                        false
                  ).apply {
                        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                              override fun getSpanSize(position: Int): Int {
                                    return if (position == 0)
                                          2
                                    else
                                          1
                              }
                        }
                  }

                  layoutManager = manager
                  adapter = userItemAdapter
            }

            userViewModel.users.observe(viewLifecycleOwner) {
                  if (it.isNotEmpty()) {
                        val user: User = it[0]
                        Log.d("Trace", "User image : ${user.icon}")
                        val url: String = "${BuildConfig.FILE_ROOT}${user.icon}"

                        binding.user.text = user.name

                        picasso.load(Uri.parse(url)).error(R.drawable.ic_empty_user).fit()
                              .into(binding.userIcon)
                  }

                  userItemAdapter.list = it
            }

      }

      private fun initViews() {

            languageListBinding = PopLanguageListBinding.inflate(layoutInflater)

            userListBinding = ViewUserListBinding.inflate(layoutInflater)

            serviceListBinding = PopServiceListBinding.inflate(layoutInflater)

            playListBinding = PopPlayingTrackListBinding.inflate(layoutInflater)

            menuBinding = PopSettingOldBinding.inflate(layoutInflater)

            buyCarBinding = PopBuyCarBinding.inflate(layoutInflater)

            buyCarBinding.web.settings.apply {
                  javaScriptEnabled = true
                  setSupportZoom(false)
                  cacheMode = WebSettings.LOAD_NO_CACHE
            }
            buyCarBinding.web.setBackgroundColor(Color.TRANSPARENT)
      }

      private fun showPopupWindow(anchorView: Button, contentView: View, showCallback: () -> Unit) {
            closePopupWindow()

            if (contentView.parent != null)
                  (contentView.parent as ViewGroup).removeView(contentView)

            showPopup = SimpleTooltip.Builder(requireContext())
                  .onShowListener {
                        anchorView.setTextColor(resources.getColor(R.color.accentPurple, null))
                        anchorView.compoundDrawableTintList =
                              ColorStateList.valueOf(resources.getColor(R.color.accentPurple, null))
                        showCallback()
                  }
                  .onDismissListener {
                        anchorView.setTextColor(resources.getColor(R.color.colorWhite, null))
                        anchorView.compoundDrawableTintList =
                              ColorStateList.valueOf(resources.getColor(R.color.colorWhite, null))
                  }
                  .modal(true)
                  .focusable(true)
                  .anchorView(anchorView)
                  .gravity(Gravity.BOTTOM)
                  .contentView(contentView, 0)
                  .arrowColor(resources.getColor(R.color.accentPurple, null))
                  .dismissOnInsideTouch(false)
                  .build()

            showPopup!!.show()
      }

      private fun closePopupWindow() {
            if (showPopup != null && showPopup!!.isShowing) {
                  showPopup!!.dismiss()
                  showPopup = null
            }
      }

      private inner class ItemViewHolder(private val mBinding: ItemPlayingTrackBinding) :
            RecyclerView.ViewHolder(mBinding.root) {
            var track: Track? = null
                  set(value) {
                        field = value
                        if (field != null) {
                              mBinding.name.text = field!!.name
                              mBinding.playing.visibility =
                                    if (field!!.state == Track.State.PLAYING) View.VISIBLE else View.INVISIBLE
                              mBinding.insert.visibility =
                                    if (field!!.state == Track.State.QUEUE) View.VISIBLE else View.INVISIBLE
                              mBinding.delete.visibility =
                                    if (field!!.state == Track.State.QUEUE) View.VISIBLE else View.INVISIBLE
                        }
                  }

            init {
                  mBinding.insert.setOnClickListener {
                        if (track == null) return@setOnClickListener
                        trackViewModel.onPlaylistInsertTrack(track!!)
                  }
                  mBinding.delete.setOnClickListener {
                        if (track == null) return@setOnClickListener
                        trackViewModel.onPlaylistDeleteTrack(track!!)
                  }
            }
      }

      private inner class PlayingItemAdapter :
            RecyclerView.Adapter<PlayingItemAdapter.PlayingItemViewHolder>() {

            var list: List<Track>? = null
                  set(value) {
                        field = value
                        filter()
                  }

            var filterType: Int = 0
                  set(value) {
                        if (field != value) {
                              field = value
                              filter()
                        }
                  }

            private var mList: List<Track>? = null

            fun filter() {
                  mList = when (filterType) {
                        0 -> list?.filter { it.state != Track.State.DONE }
                        1 -> list?.filter { it.state == Track.State.DONE }
                        else -> null
                  }
                  notifyDataSetChanged()
            }

            override fun getItemViewType(position: Int): Int {
                  return when (mList!![position].state) {
                        Track.State.NONE -> 0
                        Track.State.QUEUE -> 1
                        Track.State.NEXT -> 0
                        Track.State.PLAYING -> 0
                        Track.State.DONE -> 2
                  }
            }

            override fun onCreateViewHolder(
                  parent: ViewGroup,
                  viewType: Int
            ): PlayingItemViewHolder {
                  return when (viewType) {
                        2 -> DoneItemViewHolder(
                              ItemPopPlayListQueueBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                        1 -> QueueItemViewHolder(
                              ItemPlayListQueueBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                        else -> LockedItemViewHolder(
                              ItemPlayListLockedBinding.inflate(
                                    layoutInflater,
                                    parent,
                                    false
                              )
                        )
                  }
            }

            override fun onBindViewHolder(
                  holder: PlayingItemViewHolder,
                  position: Int
            ) {
                  holder.track = mList?.get(position)
            }

            override fun getItemCount(): Int {
                  return mList?.size ?: 0
            }

            private abstract inner class PlayingItemViewHolder(view: View) :
                  RecyclerView.ViewHolder(view) {
                  abstract var track: Track?
            }

            private inner class LockedItemViewHolder(private val mBinding: ItemPlayListLockedBinding) :
                  PlayingItemViewHolder(mBinding.root) {
                  override var track: Track? = null
                        set(value) {
                              field = value
                              if (field == null) return

                              mBinding.name.text = field!!.name
                              mBinding.state.text = when (field!!.state) {
                                    Track.State.NONE -> ""
                                    Track.State.QUEUE -> ""
                                    Track.State.NEXT -> "下一首"
                                    Track.State.PLAYING -> "播放中"
                                    Track.State.DONE -> ""
                              }
                        }
            }

            private inner class QueueItemViewHolder(private val mBinding: ItemPlayListQueueBinding) :
                  PlayingItemViewHolder(mBinding.root) {
                  override var track: Track? = null
                        set(value) {
                              field = value
                              if (field == null) return
                              mBinding.name.text = field!!.name
                        }

                  init {
                        mBinding.insert.setOnClickListener {
                              trackViewModel.onPlayListMoveTrackToTop(
                                    track ?: return@setOnClickListener
                              )
                        }
                        mBinding.delete.setOnClickListener {
                              trackViewModel.onPlaylistDeleteTrack(
                                    track ?: return@setOnClickListener
                              )
                        }
                  }
            }

            private inner class DoneItemViewHolder(private val mBinding: ItemPopPlayListQueueBinding) :
                  PlayingItemViewHolder(mBinding.root) {
                  override var track: Track? = null
                        set(value) {
                              field = value
                              if (field == null) return
                              mBinding.name.text = field!!.name
                        }

                  init {
                        mBinding.insert.setOnClickListener {
                              trackViewModel.onPlaylistInsertTrack(
                                    track ?: return@setOnClickListener
                              )
                        }
                        mBinding.order.setOnClickListener {
                              trackViewModel.onPlayListOrderTrack(
                                    track ?: return@setOnClickListener
                              )
                        }
                  }
            }
      }

      private inner class UserAdminViewHolder(private val mBinding: ItemUserListAdminBinding) :
            UserViewHolder(mBinding.root) {
            override var user: User? = null
                  set(value) {
                        field = value
                        if (field == null) return
                        mBinding.name.text = field!!.name

                        val url: String = "${BuildConfig.FILE_ROOT}${field!!.icon}"

                        picasso.load(Uri.parse(url)).error(R.drawable.ic_empty_user).fit()
                              .into(binding.userIcon)
                  }
      }

      private inner class UserNormalViewHolder(private val mBinding: ItemUserListNormalBinding) :
            UserViewHolder(mBinding.root) {
            override var user: User? = null
                  set(value) {
                        field = value
                        if (field == null) return
                        mBinding.name.text = field!!.name

                        val url: String = "${BuildConfig.FILE_ROOT}${field!!.icon}"

                        picasso.load(Uri.parse(url)).error(R.drawable.ic_empty_user).fit()
                              .into(binding.userIcon)
                  }
      }

      private abstract class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            abstract var user: User?
      }

      private inner class UserItemAdapter : RecyclerView.Adapter<UserViewHolder>() {
            var list: List<User>? = null
                  set(value) {
                        field = value
                        notifyDataSetChanged()
                  }

            override fun getItemViewType(position: Int): Int {
                  return if (position == 0) 1 else 2
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                  return if (viewType == 2)
                        UserNormalViewHolder(ItemUserListNormalBinding.inflate(layoutInflater))
                  else
                        UserAdminViewHolder(ItemUserListAdminBinding.inflate(layoutInflater))
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
                  holder.user = list!![position]
            }

            override fun getItemCount(): Int {
                  return list?.size ?: 0
            }
      }


      private val onClickListener: View.OnClickListener = View.OnClickListener {
            when (it) {
                  binding.language -> showPopupWindow(it as Button, languageListBinding.root) {}
                  binding.user -> showPopupWindow(it as Button, userListBinding.root) {}
                  binding.tracks -> showPopupWindow(it as Button, playListBinding.root) {
                        playingItemAdapter.filter()
                  }
                  binding.service -> showPopupWindow(it as Button, serviceListBinding.root) {}
                  binding.menu -> showPopupWindow(it as Button, menuBinding.root) {}
                  binding.buyCar -> showPopupWindow(it as Button, buyCarBinding.root) {
                        val roomId = systemViewModel.roomId

                        val uuid = systemViewModel.uuid

                        buyCarBinding.web.loadUrl("${BuildConfig.WEB_ROOT}index.php/VodFood/CartList?boxId=$roomId&devId=$uuid")
                  }
            }
      }

//    private val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
//        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
//            when (buttonView) {
//                binding.language -> {
//                    if (isChecked)
//                        showPopupWindow(buttonView as CheckBox, languageListBinding.root) {}
//                }
//                binding.user -> {
//                    if (isChecked)
//                        showPopupWindow(buttonView as CheckBox, userListBinding.root) {}
//                }
//                binding.tracks -> {
//                    if (isChecked)
//                        showPopupWindow(buttonView as CheckBox, playListBinding.root) {
//                            playingItemAdapter.filter()
//                        }
//                }
//                binding.service -> {
//                    if (isChecked)
//                        showPopupWindow(buttonView as CheckBox, serviceListBinding.root) {}
//                }
//                binding.menu -> {
//                    if (isChecked)
//                        showPopupWindow(buttonView as CheckBox, menuBinding.root) {}
//                }
//            }
//        }

}