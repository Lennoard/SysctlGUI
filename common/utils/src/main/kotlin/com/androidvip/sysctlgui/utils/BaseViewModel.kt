package com.androidvip.sysctlgui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class BaseViewModel<Event, State, Effect> : ViewModel() {
    abstract fun createInitialState(): State

    private val initialState: State by lazy { createInitialState() }

    val currentState: State get() = uiState.value

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect
        .receiveAsFlow()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    abstract fun onEvent(event: Event)

    protected fun setState(block: State.() -> State) {
        _uiState.value = currentState.block()
    }

    protected fun setEffect(block: () -> Effect) {
        viewModelScope.launch {
            _effect.send(block())
        }
    }
}