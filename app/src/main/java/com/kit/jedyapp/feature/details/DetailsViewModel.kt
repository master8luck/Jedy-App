package com.kit.jedyapp.feature.details

import androidx.lifecycle.ViewModel
import com.kit.jedyapp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel()
