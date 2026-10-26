package com.firstroad.mate.data.datasource

import javax.inject.Qualifier

/** Pleos SDK를 쓸 수 없을 때 대신 쓰는 Android 플랫폼 기본 구현 (음성 합성·인식). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlatformSource
