package com.humayapp.scout.core.domain

//sealed interface VMComponent
//
//class UiState<State>(initialState: State) : VMComponent {
//    private val _state = MutableStateFlow(initialState)
//    val flow: StateFlow<State> = _state.asStateFlow()
//    val value: State
//        get() = _state.value
//
//    fun asFlow() = _state.asStateFlow()
//    fun update(updater: (State) -> State) = _state.update(updater)
//}
//
//class UiEvent<Event> : VMComponent {
//    private val _events = Channel<Event>(Channel.BUFFERED)
//    val events: Flow<Event> = _events.receiveAsFlow()
//
//    suspend fun sendEvent(event: Event) = _events.send(event)
//}
//
//class UiError<E>(initial: E? = null) : VMComponent {
//    private val _errors = MutableStateFlow<E?>(initial)
//    val flow = _errors.asStateFlow()
//
//    fun setError(error: E?) = _errors.update { error }
//    fun clearError() = _errors.update { null }
//}
//
//interface UiAction<Action> : VMComponent {
//    fun onAction(action: Action)
//}
