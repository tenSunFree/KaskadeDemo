package com.home.kaskadedemo.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.home.kaskadedemo.R
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_auth.*

@Suppress("IMPLICIT_CAST_TO_ANY")
internal class AuthFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(AuthViewModel::class.java)
    }

    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disposable = viewModel.state.subscribe { render(it) }
        viewModel.process(buttonLogin.clicks().map {
            AuthAction.Login(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }

    private fun render(state: AuthState) = when (state) {
        AuthState.Initial -> {}
        AuthState.Loading -> {
            buttonLogin.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            textError.visibility = View.GONE
        }
        AuthState.Error -> {
            buttonLogin.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            textError.visibility = View.VISIBLE
        }
        AuthState.Success -> {
            buttonLogin.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            textError.visibility = View.GONE
            Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
