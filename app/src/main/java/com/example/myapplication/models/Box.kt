package com.example.myapplication.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Thông tin từng Box
 */
data class Box(

    /** ID box (local / server) */
    val id: Int = 0,

    /** Số thứ tự box */
    val numberBox: Int = 0,

    /** Số lượng sản phẩm trong box */
    var count: Long = 0L

) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        numberBox = parcel.readInt(),
        count = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(numberBox)
        parcel.writeLong(count)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Box> {
        override fun createFromParcel(parcel: Parcel): Box {
            return Box(parcel)
        }

        override fun newArray(size: Int): Array<Box?> {
            return arrayOfNulls(size)
        }
    }
}