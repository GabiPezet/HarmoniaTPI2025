package com.android.harmoniatpi.domain.usecases

import javax.inject.Inject

class SetOnPlaybackCompletedCallbackUseCase @Inject constructor(
) {
    operator fun invoke(callback: () -> Unit) {}
}
