package com.example.myapplication.helper

import android.os.Parcel
import android.os.Parcelable

data class ChiyodaInfo(
    val wono: String = "",
    val completedCount: Long = 0L,
    val entryDate: String? = null,
    val packingType: Int = 0,
    val wonoComplete: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        wono = parcel.readString() ?: "",
        completedCount = parcel.readLong(),
        entryDate = parcel.readString(),
        packingType = parcel.readInt(),
        wonoComplete = parcel.readByte().toInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(wono)
        parcel.writeLong(completedCount)
        parcel.writeString(entryDate)
        parcel.writeInt(packingType)
        parcel.writeByte(if (wonoComplete) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ChiyodaInfo> {
        override fun createFromParcel(parcel: Parcel): ChiyodaInfo {
            return ChiyodaInfo(parcel)
        }

        override fun newArray(size: Int): Array<ChiyodaInfo?> {
            return arrayOfNulls(size)
        }
    }
}