package com.home.kaskadedemo.auth

import androidx.lifecycle.ViewModel
import dev.gumil.kaskade.Action
import dev.gumil.kaskade.Kaskade
import dev.gumil.kaskade.State
import dev.gumil.kaskade.rx.rx
import dev.gumil.kaskade.rx.stateObservable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class AuthViewModel(private val delay: Long = 3) : ViewModel() {

    private val disposables = CompositeDisposable() // 可以快速解除所有添加的一次性類

    private val observer
        get() = object : DisposableObserver<AuthState>() {
            override fun onComplete() {}

            /**
             * Do sideffects here like logging. Sending state already handled by Kaskade
             */
            override fun onNext(state: AuthState) {}

            override fun onError(e: Throwable) {
                process(Observable.just(AuthAction.OnError))
            }
        }.also { disposables.add(it) } // 將它添加到容器中, 在退出的時候, 調用CompositeDisposable.clear() 即可快速解除

    private val kaskade = Kaskade.create<AuthAction, AuthState>(AuthState.Initial) {
        rx {
            on<AuthAction.Login>({ observer }) {
                delay(delay, TimeUnit.SECONDS)
                    .map { actionState ->
                        val isEmailCorrect = actionState.action.email == "kaskade@gmail.com"
                        val isPasswordCorrect = actionState.action.password == "12345678"
                        if (isEmailCorrect && isPasswordCorrect) {
                            AuthState.Success
                        } else {
                            AuthState.Error
                        }
                    }
                    .ofType(AuthState::class.java)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWith(AuthState.Loading)
            }
        }
        on<AuthAction.OnError> {
            AuthState.Error
        }
    }

    val state: Observable<AuthState> = kaskade.stateObservable()

    fun process(actions: Observable<AuthAction>) {
        actions.subscribe { kaskade.process(it) }.also { disposables.add(it) }
    }

    /**
     * 只有當Activity的finish()方法被調用時, ViewModel.onCleared()方法會被調用, 對象才會被銷毀
     */
    override fun onCleared() {
        super.onCleared()
        kaskade.unsubscribe()
        disposables.clear() // 快速解除
    }
}

sealed class AuthState : State {
    object Initial : AuthState()
    object Loading : AuthState()
    object Error : AuthState()
    object Success : AuthState()
}

sealed class AuthAction : Action {
    data class Login(val email: String, val password: String) : AuthAction()
    object OnError : AuthAction()
}