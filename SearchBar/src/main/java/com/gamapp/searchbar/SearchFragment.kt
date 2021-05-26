package com.gamapp.searchbar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.cielyang.android.clearableedittext.ClearableEditText
import com.gamapp.searchbar.databinding.SearchFragmentLayoutBinding

class SearchFragment : Fragment() {
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var forwardBtn: FrameLayout
    private lateinit var speechBtn: FrameLayout
    private lateinit var searchEditText: ClearableEditText
    private val SPEECH_REQUEST_CODE: Int = 32434
    private lateinit var searchBar: CardView
    private var _searchFragmentLayoutBinding: SearchFragmentLayoutBinding? = null
    private val searchFragmentLayoutBinding get() = _searchFragmentLayoutBinding!!
    private var searchedText: String? = null
    private var uiData = UiData().apply {
        uiColor.primaryColor = Color.rgb(242, 242, 240)
        uiColor.secondaryColor = Color.rgb(100, 100, 100)
        uiColor.thirdaryColor = Color.rgb(248, 248, 248)
    }
    private var viewLiveData = MutableLiveData(true)
    private fun updateViews() {
        updateViewsColors()
    }

    fun changeViewsColor(uiColor: UiData.UiColor) {
        this.uiData.uiColor = uiColor
        viewLiveData.postValue(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _searchFragmentLayoutBinding =
            SearchFragmentLayoutBinding.inflate(inflater, container, false)
        return searchFragmentLayoutBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _searchFragmentLayoutBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewsId()
        setUpViews()
        updateViewsColors()
        setViewsAction()
        setLiveDataObserve()
        onEditTextChange()
    }

    private fun setLiveDataObserve() {
        viewLiveData.observe(viewLifecycleOwner){
            updateViews()
        }
    }

    private fun updateViewsColors() {
        uiData.uiColor.primaryColor.let {
            searchFragmentLayoutBinding.apply {
                root.background = ColorDrawable(it)
            }
        }
        uiData.uiColor.thirdaryColor.let {
            searchFragmentLayoutBinding.apply {
                searchCard.setCardBackgroundColor(it)
                historyTitleBack.background = ColorDrawable(it)
            }
        }
        uiData.uiColor.secondaryColor.let {
            searchFragmentLayoutBinding.apply {
                historyTitle.setTextColor(it)
                searchBtnImage.setColorFilter(it)
                speechBtnImage.setColorFilter(it)
                searchEditText.setTextColor(it)
                searchEditText.setHintTextColor(it)
                val field = searchEditText::class.java.getDeclaredField("mClearIconDrawable")
                field.isAccessible = true
                val clearBtnDrawable = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.clear_ic,
                    null
                )
                clearBtnDrawable?.let { drawable ->
                    drawable.setColorFilter(it, PorterDuff.Mode.MULTIPLY)
                }
                field.set(searchEditText, clearBtnDrawable)
            }
        }
    }

    private fun setUpViews() {
        searchFragmentLayoutBinding.historyRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            historyAdapter = HistoryAdapter(context, uiData) {
                if (it) {
                    searchFragmentLayoutBinding.historyCard.visibility = View.VISIBLE
                } else {
                    searchFragmentLayoutBinding.historyCard.visibility = View.GONE
                }
            }
            adapter = historyAdapter
        }

    }

    interface OnSearchListener {
        fun onSearch(searchedText: String)
    }

    private var searchParam: ((String) -> Unit)? = null
    private var searchListener: OnSearchListener? = null

    fun setOnSearchListener(listener: OnSearchListener) {
        searchListener = listener
    }

    fun setOnSearchListener(function: (String) -> Unit) {
        searchParam = function
    }


    private fun addToHistory(element: String) {
        HistoryManager.getInstance(requireContext()).addHistoryItem(HistoryModel(element))
    }

    private fun setViewsAction() {
        speechBtn.setOnClickListener {
            startSpeechRecognition()
        }
        forwardBtn.setOnClickListener {
            searchedText?.let {
                onSearch()
            }
        }
        historyAdapter.setOnHistorySelected { model: HistoryModel ->
            searchEditText.setText(model.value)
            forwardBtn.callOnClick()
        }
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearch()
                return@setOnEditorActionListener true;
            }
            return@setOnEditorActionListener false;
        }
    }

    private fun onSearch() {
        searchedText?.let {
            val out = HistoryManager.getInstance(requireContext()).readyItem(it)
            if (out.length > 1) {
                addToHistory(out)
                callSearchListeners(out)
                closeSoftInput()
            }
        }

    }

    private fun closeSoftInput() {
        requireActivity().apply {
            currentFocus?.let {
                (getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager).apply {
                    hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
        }
    }

    private fun callSearchListeners(searchedText: String) {
        searchListener?.onSearch(searchedText)
        searchParam?.let {
            it(searchedText)
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        // This starts the activity and populates the intent with the speech text.

        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    private fun setViewsId() {
        searchBar = searchFragmentLayoutBinding.searchCard
        speechBtn = searchFragmentLayoutBinding.speechBtn
        forwardBtn = searchFragmentLayoutBinding.searchBtn
        searchEditText = searchFragmentLayoutBinding.searchEditText
    }

    private fun onEditTextChange() {

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    historyAdapter.search(it.toString())
                    searchedText = it.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }


    private fun onSpeechResult(speech: String) {
        searchEditText.setText(speech)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? = data?.let {
                it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }
            }
            spokenText?.let {
                onSpeechResult(it)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}