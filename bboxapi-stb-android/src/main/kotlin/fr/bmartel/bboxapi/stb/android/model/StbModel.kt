package fr.bmartel.bboxapi.stb.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Vod(
        val _id: String?,
        val productId: String?,
        val externalId: String?,
        val title: String?,
        val series: String?,
        val year: Int?,
        val runtime: Int?,
        val contentType: String?,
        val outBundle: Int?,
        val bundles: String?,
        val provider: String?,
        val actors: String?,
        val directors: String?,
        val coverUrlPortrait: String?,
        val coverUrlLandscape: String?,
        val startDate: String?,
        val endDate: String?,
        val episodeNumber: Int?,
        val seasonNumber: Int?,
        val definition: Int?,
        val languages: String?,
        val captionLanguages: String?,
        val captionLanguagesImpairedHearing: String?,
        val captionLanguageInVideo: String?,
        val parentalGuidance: Int?,
        val audioCodingName: String?,
        val genres: String?,
        val publicRating: Float?,
        val pressRating: Float?,
        val summaryShort: String?,
        val summaryLong: String?,
        val countryOfOrigin: String?,
        val externalTrailerUrl: String?,
        val externalTrailerId: String?,
        val hasTrailer: Int?,
        val secondaryTitle: String?,
        val mostPopular: Int?,
        val highlight: Int?,
        val score: Float?
) : Parcelable

@Parcelize
data class EpgProgram(
        val eventId: String?,
        val externalId: String?,
        val epgChannelNumber: Int?,
        val positionId: Int?,
        val title: String?,
        val genre: List<String>?,
        val summary: String?,
        val character: String?,
        val startTime: String?,
        val endTime: String?,
        val duration: String?,
        val country: String?,
        val date: Int?,
        val publicRank: String?,
        val parentalGuidance: String?,
        val thumb: String?
) : Parcelable