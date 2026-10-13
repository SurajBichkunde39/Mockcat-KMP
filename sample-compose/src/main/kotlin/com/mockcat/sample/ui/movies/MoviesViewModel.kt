package com.mockcat.sample.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockcat.sample.data.FilmDto
import com.mockcat.sample.data.KtorMovieRepository
import com.mockcat.sample.data.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoviesUiState(
    val isDetail: Boolean = false,
    val list: List<FilmDto> = emptyList(),
    val selectedFilm: FilmDto? = null,
    val detail: FilmDto? = null,
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val errorMessage: String? = null,
) {
    val isListEmpty: Boolean get() = list.isEmpty() && !isLoading
}

class MoviesViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMovieList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository
                .fetchList()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            list = list,
                            isLoading = false,
                            isDetail = false,
                            selectedFilm = null,
                            detail = null,
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: e.toString()) }
                }
        }
    }

    fun openDetail(film: FilmDto) {
        _uiState.update {
            it.copy(
                isDetail = true,
                selectedFilm = film,
                detail = null,
                isLoadingDetail = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            repository
                .fetchDetail(film)
                .onSuccess { d -> _uiState.update { s -> s.copy(detail = d, isLoadingDetail = false) } }
                .onFailure { e -> _uiState.update { s -> s.copy(isLoadingDetail = false, errorMessage = e.message) } }
        }
    }

    fun onBack() {
        _uiState.update {
            it.copy(
                isDetail = false,
                selectedFilm = null,
                detail = null,
                isLoadingDetail = false,
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        (repository as? KtorMovieRepository)?.close()
    }
}
