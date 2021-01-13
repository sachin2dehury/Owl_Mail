package github.sachin2dehury.nitrmail.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import dagger.hilt.android.AndroidEntryPoint
import github.sachin2dehury.nitrmail.R
import github.sachin2dehury.nitrmail.databinding.FragmentMailItemBinding
import github.sachin2dehury.nitrmail.others.Constants
import github.sachin2dehury.nitrmail.others.Status
import github.sachin2dehury.nitrmail.ui.ActivityExt
import github.sachin2dehury.nitrmail.ui.viewmodels.MailItemViewModel
import java.text.SimpleDateFormat

@AndroidEntryPoint
class MailItemFragment : Fragment(R.layout.fragment_mail_item) {

    private var _binding: FragmentMailItemBinding? = null
    private val binding: FragmentMailItemBinding get() = _binding!!

    private val viewModel: MailItemViewModel by viewModels()

    private val args: MailItemFragmentArgs by navArgs()

    lateinit var colors: IntArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMailItemBinding.bind(view)

        (activity as ActivityExt).apply {
            toggleDrawer(false)
            toggleActionBar(true)
        }

        colors = resources.getIntArray(R.array.colors)

        subscribeToObservers()

        viewModel.setId(args.id)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncParsedMails()
        }

        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = viewModel.getMailViewClient()
            isVerticalScrollBarEnabled = false
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            setInitialScale(160)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                val darkMode = when (AppCompatDelegate.getDefaultNightMode()) {
                    AppCompatDelegate.MODE_NIGHT_NO -> WebSettingsCompat.FORCE_DARK_OFF
                    else -> WebSettingsCompat.FORCE_DARK_ON
                }
                WebSettingsCompat.setForceDark(this.settings, darkMode)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun subscribeToObservers() {
        viewModel.parsedMail.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()
                result.data?.let { mail ->
                    val sender =
                        if (mail.flag.contains('s')) mail.addresses.first() else mail.addresses.last()
                    val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_FULL)
                    val name =
                        if (sender.name.isNotEmpty()) sender.name else sender.email.substringBefore(
                            '@'
                        )
                    binding.apply {
                        imageViewSender.setColorFilter(colors.random())
                        textViewDate.text =
                            dateFormat.format(mail.time)
                        textViewSender.text = name.first().toString()
                        textViewMailSubject.text = mail.subject
                        textViewSenderName.text =
                            if (sender.name.isNotEmpty()) sender.name else sender.email.substringBefore(
                                '@'
                            )
                        textViewSenderEmail.text = sender.email
                        if (mail.flag.contains('a')) {
                            imageViewAttachment.isVisible = true
                        }
                        if (mail.body.isEmpty()) {
                            (activity as ActivityExt).showSnackbar("This mail has no content")
                        }
                        webView.loadDataWithBaseURL(
                            Constants.BASE_URL,
                            mail.parsedBody,
                            "text/html",
                            "utf-8",
                            null
                        )
                    }
                }
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.webView.isVisible = true
                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message ->
                                (activity as ActivityExt).showSnackbar(message)
                            }
                        }
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.webView.isVisible = true
                    }
                    Status.LOADING -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                        binding.webView.isVisible = false
                    }
                }
            }
        })
        viewModel.id.observe(viewLifecycleOwner, {
            viewModel.syncParsedMails()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}