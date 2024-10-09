package com.tt.dramatime.http.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取用户钱包余额
 * </pre>
 */
class WalletBalanceApi : IRequestApi {

    override fun getApi(): String {
        return "user/wallet/balance"
    }

    class Bean() :Parcelable{
        /**
         * balance : 0
         * integral : 0
         * total : 0
         */
        @SerializedName("balance")
        var balance = 0

        @SerializedName("integral")
        var integral = 0

        @SerializedName("total")
        var total = 0

        constructor(parcel: Parcel) : this() {
            balance = parcel.readInt()
            integral = parcel.readInt()
            total = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(balance)
            parcel.writeInt(integral)
            parcel.writeInt(total)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Bean> {
            override fun createFromParcel(parcel: Parcel): Bean {
                return Bean(parcel)
            }

            override fun newArray(size: Int): Array<Bean?> {
                return arrayOfNulls(size)
            }
        }
    }
}