package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import mx.edu.unpa.calificacionesunpa.R

class FragmentCalendarioEscolar : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)

        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        val fileId = "1qAMJ70t0CcKNHu6XoFDyfWbeIDQ4wQR2"
        val pdfUrl = "https://drive.google.com/file/d/1rILxFFcWXNrkyZ8Kz0pRVobb0a6Z05zi/view?usp=sharing"
        webView.loadUrl(pdfUrl)

        return view
    }
}
