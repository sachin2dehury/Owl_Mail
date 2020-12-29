package github.sachin2dehury.nitrmail.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import github.sachin2dehury.nitrmail.R
import github.sachin2dehury.nitrmail.databinding.FragmentMailItemBinding
import github.sachin2dehury.nitrmail.others.Status
import github.sachin2dehury.nitrmail.ui.ActivityExt
import github.sachin2dehury.nitrmail.ui.viewmodels.MailItemViewModel

@AndroidEntryPoint
class MailItemFragment : Fragment(R.layout.fragment_mail_item) {

    lateinit var viewModel: MailItemViewModel

    private var _binding: FragmentMailItemBinding? = null
    private val binding: FragmentMailItemBinding get() = _binding!!

    private val args: MailItemFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MailItemViewModel.id = args.id

        (activity as ActivityExt).apply {
            toggleDrawer(false)
            toggleActionBar(true)
        }

        _binding = FragmentMailItemBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(MailItemViewModel::class.java)

        viewModel.syncParsedMails()
        subscribeToObservers()
    }

    @SuppressLint("SimpleDateFormat", "SetJavaScriptEnabled")
    private fun subscribeToObservers() {
        viewModel.parsedMail.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()
                result.data?.let { mail ->
                    binding.apply {
//                        textViewDate.text =
//                            SimpleDateFormat(Constants.DATE_FORMAT_YEAR).format(mail.date)
//                        textViewMailSubject.text = mail.subject
//                        textViewSender.text = mail.from.email
                        webView.apply {
//                            settings.javaScriptEnabled = true
//                            settings.loadsImagesAutomatically = true
//                            val body =
//                                if (mail.bodyText.length > mail.bodyHtml.length) mail.bodyText else mail.bodyHtml
//                            loadDataWithBaseURL(null, mail.response, "text/html", "utf-8", null)
                            loadData(mail.response, "text/html", "ascii")
                        }
                        Log.w("Test", mail.response)
                    }
                }
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message ->
                                (activity as ActivityExt).showSnackbar(message)
                                Log.w("Test", message)
                            }
                        }
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    Status.LOADING -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}