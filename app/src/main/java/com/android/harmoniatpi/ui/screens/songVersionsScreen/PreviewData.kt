package com.android.harmoniatpi.ui.screens.songVersionsScreen

import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User

/**
 * Función para crear una lista de versiones derivadas simuladas.
 */
fun createMockDerivedVersions(): List<DerivedVersion> {
    val derivedVersions = listOf(
        DerivedVersion(
            "v1",
            User(
                "u1",
                "Luna Beats",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30"
            ),
            "projectA",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationMillis = 0L

        ),
        DerivedVersion(
            "v2",
            User(
                "u2",
                "Echo Rivera",
                "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91"
            ),
            "projectB",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v3",
            User(
                "u3",
                "Kai Harmonix",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e"
            ),
            "projectC",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v4",
            User(
                "u4",
                "Selene Nova",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1"
            ),
            "projectD",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v5",
            User(
                "u5",
                "Aria Flow",
                "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61",
            ),
            "projectE",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v6",
            User(
                "u6",
                "Noah Frequenza",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330"
            ),
            "projectF",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v7",
            User(
                "u7",
                "Zion Wave",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d"
            ),
            "projectG",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v8",
            User(
                "u8",
                "Vera Pulse",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e"
            ),
            "projectH",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v9",
            User(
                "u9",
                "Milo Resonance",
                "https://images.unsplash.com/photo-1521119989659-a83eee488004"
            ),
            "projectI",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v10",
            User(
                "u10",
                "Nia Groove",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde"
            ),
            "projectJ",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v11",
            User(
                "u11",
                "Riley Sound",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde"
            ),
            "projectK",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            durationMillis = 0L
        ),
        DerivedVersion(
            "v12",
            User(
                "u12",
                "Ivy Echo",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2"
            ),
            "projectL",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
            durationMillis = 0L
        )
    )
    return derivedVersions
}

/**
 * Función para crear una canción original simulada.
 */
fun createMockSong(): Song {
    val song = Song(
        id = "original-01",
        title = "El paso del tiempo",
        creator = User(id = "creator-01", name = "Luna Beats", avatarUrl = null),
        imageUrl = "url_de_imagen",
        durationMillis = 0L,
        versionType = VersionType.ORIGINAL,
        audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        projectId = "proj-01",
    )
    return song
}


