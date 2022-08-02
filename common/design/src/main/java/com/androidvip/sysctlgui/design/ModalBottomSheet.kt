package com.androidvip.sysctlgui.design

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.androidvip.sysctlgui.design.databinding.ModalBottomSheetBinding

open class ModalBottomSheet : BaseBottomSheetFragment<ModalBottomSheetBinding>() {

    private var listener: EventListener? = null

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ModalBottomSheetBinding = ModalBottomSheetBinding.inflate(inflater)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {
            listener = parent as? EventListener
        }

        if (listener == null) {
            listener = context as? EventListener
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sheetTitle.text = arguments?.getString(ARG_TITLE).orEmpty()
        binding.sheetDescription.text = arguments?.getCharSequence(ARG_MESSAGE) ?: ""

        arguments?.getString(ARG_POSITIVE_BUTTON_TEXT)?.let {
            binding.positiveButton.apply {
                text = it
                visibility = View.VISIBLE
                setOnClickListener {
                    listener?.onContinuePressed()
                    dismiss()
                }
            }
        }

        arguments?.getString(ARG_NEGATIVE_BUTTON_TEXT)?.let {
            binding.negativeButton.apply {
                text = it
                visibility = View.VISIBLE
                setOnClickListener {
                    listener?.onCancelPressed()
                    dismiss()
                }
            }
        }
    }

    interface EventListener {
        fun onContinuePressed()
        fun onCancelPressed()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_POSITIVE_BUTTON_TEXT = "positiveButtonText"
        private const val ARG_NEGATIVE_BUTTON_TEXT = "negativeButtonText"

        fun newInstance(
            title: String,
            message: CharSequence,
            positiveButtonText: String? = null,
            negativeButtonText: String? = null
        ): ModalBottomSheet {
            return ModalBottomSheet().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_MESSAGE to message,
                    ARG_POSITIVE_BUTTON_TEXT to positiveButtonText,
                    ARG_NEGATIVE_BUTTON_TEXT to negativeButtonText
                )
            }
        }
    }
}
