package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.Guidance

/**
 * Guidance를 사람이 읽고 들을 문장으로 바꾼다. 구현은 strings.xml을 쓰므로 data 계층에 있고,
 * ViewModel은 Context 없이 이 인터페이스로 문장을 얻는다.
 */
interface PhraseRepository {
    fun phrase(guidance: Guidance): String
}
