package github.sachin2dehury.owlmail.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import github.sachin2dehury.owlmail.R
import github.sachin2dehury.owlmail.api.calls.MailViewClient
import github.sachin2dehury.owlmail.databinding.FragmentWebViewBinding
import github.sachin2dehury.owlmail.others.Constants
import github.sachin2dehury.owlmail.others.debugLog
import github.sachin2dehury.owlmail.ui.viewmodels.WebPageViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WebPageFragment : Fragment(R.layout.fragment_web_view) {

    private var _binding: FragmentWebViewBinding? = null
    private val binding: FragmentWebViewBinding get() = _binding!!

    private val viewModel: WebPageViewModel by viewModels()

    private val args: WebPageFragmentArgs by navArgs()

    @Inject
    lateinit var mailViewClient: MailViewClient


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentWebViewBinding.bind(view)

        setContent()

        binding.swipeRefreshLayout.setOnRefreshListener {
            setContent()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setContent() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.webView.apply {
            webViewClient = mailViewClient
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.setSupportZoom(true)
            setInitialScale(100)
            debugLog(args.url + Constants.AUTH_FROM_TOKEN + viewModel.token.substringAfter('='))
            loadUrl(args.url + Constants.AUTH_FROM_TOKEN + viewModel.token.substringAfter('='))
            zoomOut()
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}