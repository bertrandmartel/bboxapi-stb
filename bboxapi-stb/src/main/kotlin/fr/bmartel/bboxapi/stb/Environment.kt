package fr.bmartel.bboxapi.stb

interface Environment

fun createEnvironment(): Environment = try {
    Class.forName(AndroidEnvironmentClass).newInstance() as Environment
} catch (exception: ClassNotFoundException) {
    DefaultEnvironment()
}

class DefaultEnvironment : Environment

const val AndroidEnvironmentClass = "fr.bmartel.bboxapi.stb.android.AndroidEnvironment"