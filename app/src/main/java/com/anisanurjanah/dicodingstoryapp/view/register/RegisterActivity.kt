package com.anisanurjanah.dicodingstoryapp.view.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.anisanurjanah.dicodingstoryapp.R
import com.anisanurjanah.dicodingstoryapp.data.Result
import com.anisanurjanah.dicodingstoryapp.data.pref.UserPreference
import com.anisanurjanah.dicodingstoryapp.databinding.ActivityRegisterBinding
import com.anisanurjanah.dicodingstoryapp.view.ViewModelFactory
import com.anisanurjanah.dicodingstoryapp.view.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SESSION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(false)
        setupAnimation()
        setupTitle()
        setupButton()
        setupAction()
        setupAccessibility()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener { setupRegister() }
    }

    private fun setupAccessibility() {
        binding.apply {
            dicodingImage.contentDescription = getString(R.string.dicoding_s_logo)
            storyLogo.contentDescription = getString(R.string.title_of_dicoding_logo)
            signupTitle.contentDescription = getString(R.string.title_of_signup)
            signupDescription.contentDescription = getString(R.string.description_of_signup)
            emailEditTextLayout.contentDescription = getString(R.string.email_input_field)
            passwordEditTextLayout.contentDescription = getString(R.string.password_input_field)
            signupButton.contentDescription = getString(R.string.sign_up_button)
            loginButton.contentDescription = getString(R.string.login_button)
        }
    }

    private fun setupAnimation() {
        ObjectAnimator.ofFloat(binding.dicodingImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val storyLogo = ObjectAnimator.ofFloat(binding.storyLogo, View.ALPHA, 1f).setDuration(100)
        val signupTitle = ObjectAnimator.ofFloat(binding.signupTitle, View.ALPHA, 1f).setDuration(100)
        val signupDescription = ObjectAnimator.ofFloat(binding.signupDescription, View.ALPHA, 1f).setDuration(100)
        val edName = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val edEmail = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val edPassword = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signupButton = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)
        val loginButton = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(storyLogo, signupTitle, signupDescription, edName, edEmail, edPassword, signupButton, loginButton)
            start()
        }
    }

    private fun setupTitle() {
        val linoleumBlue = ContextCompat.getColor(this, R.color.linoleum_blue)

        val spannable = SpannableString(getString(R.string.signup_title, getString(R.string.dicoding_story)))
        spannable.setSpan(
            ForegroundColorSpan(linoleumBlue),
            spannable.indexOf(getString(R.string.dicoding_story)),
            spannable.indexOf(getString(R.string.dicoding_story)) + getString(R.string.dicoding_story).length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.signupTitle.text = spannable
    }

    private fun setupButton() {
        val linoleumBlue = ContextCompat.getColor(this, R.color.linoleum_blue)

        val spannable = SpannableString(getString(R.string.signup_to_login, getString(R.string.login_now)))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }

        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(
            boldSpan,
            spannable.indexOf(getString(R.string.login_now)),
            spannable.indexOf(getString(R.string.login_now)) + getString(R.string.login_now).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            clickableSpan,
            spannable.indexOf(getString(R.string.login_now)),
            spannable.indexOf(getString(R.string.login_now)) + getString(R.string.login_now).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(linoleumBlue),
            spannable.indexOf(getString(R.string.login_now)),
            spannable.indexOf(getString(R.string.login_now)) + getString(R.string.login_now).length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.loginButton.text = spannable
        binding.loginButton.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupRegister() {
        val registerViewModel = obtainViewModel(this@RegisterActivity)

        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        when {
            name.isEmpty() -> {
                binding.nameEditText.error = getString(R.string.empty_name)
            }
            email.isEmpty() -> {
                binding.emailEditText.error = getString(R.string.empty_email)
            }
            password.isEmpty() -> {
                binding.passwordEditText.error = getString(R.string.empty_password)
            }
            else -> {
                registerViewModel.register(name, email, password).observe(this@RegisterActivity) {
                    if (it != null) {
                        when (it) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)

                                val response = it.data
                                AlertDialog.Builder(this).apply {
                                    setTitle(getString(R.string.register_success))
                                    val registerMessage = getString(R.string.register_message)
                                    val fullMessage = "${response.message} $registerMessage"
                                    setMessage(fullMessage)
                                    setPositiveButton(getString(R.string.continue_login)) { _, _ ->
                                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                    create()
                                    show()
                                }
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showToast(it.error)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun obtainViewModel(activity: AppCompatActivity): RegisterViewModel {
        val factory = ViewModelFactory.getInstance(
            activity.application,
            UserPreference.getInstance(dataStore)
        )
        return ViewModelProvider(activity, factory)[RegisterViewModel::class.java]
    }

    companion object {
        const val SESSION = "session"
    }
}