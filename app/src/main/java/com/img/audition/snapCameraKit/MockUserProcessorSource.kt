package com.img.audition.snapCameraKit

import com.snap.camerakit.Source
import com.snap.camerakit.UserProcessor
import com.snap.camerakit.common.Consumer
import java.io.Closeable
import java.util.*

internal class MockUserProcessorSource(
    private val userDisplayName: String? = null,
    private val userBirthDate: Date? = null
) : Source<UserProcessor> {

    override fun attach(processor: UserProcessor): Closeable {
        return processor.connectInput(object : UserProcessor.Input {
            override fun subscribeTo(onUserAvailable: Consumer<UserProcessor.Input.User>): Closeable {
                onUserAvailable.accept(
                    UserProcessor.Input.User(
                        displayName = userDisplayName,
                        birthDate = userBirthDate
                    )
                )
                return Closeable {
                    // no-op
                }
            }
        })
    }
}