package com.kit.jedyapp.domain.model

/** The service rejected the request or returned unusable movie data. */
class MovieDataException(message: String) : Exception(message)
