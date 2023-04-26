@file:JvmName("Closeables")

package com.img.audition.snapCameraKit

import java.io.Closeable

internal fun Closeable.addTo(closeables: MutableList<Closeable>) = apply { closeables.add(this) }
