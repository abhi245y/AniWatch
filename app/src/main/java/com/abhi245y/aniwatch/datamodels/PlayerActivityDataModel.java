package com.abhi245y.aniwatch.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PlayerActivityDataModel implements Parcelable {

    String animeId, currentEpLink, currentEpNum, totalEp, fromActivity, epGogoLink;


    protected PlayerActivityDataModel(Parcel in) {
        animeId = in.readString();
        currentEpLink = in.readString();
        currentEpNum = in.readString();
        totalEp = in.readString();
        fromActivity = in.readString();
        epGogoLink = in.readString();
    }

    public static final Creator<PlayerActivityDataModel> CREATOR = new Creator<PlayerActivityDataModel>() {
        @Override
        public PlayerActivityDataModel createFromParcel(Parcel in) {
            return new PlayerActivityDataModel(in);
        }

        @Override
        public PlayerActivityDataModel[] newArray(int size) {
            return new PlayerActivityDataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(animeId);
        parcel.writeString(currentEpLink);
        parcel.writeString(currentEpNum);
        parcel.writeString(totalEp);
        parcel.writeString(fromActivity);
        parcel.writeString(epGogoLink);
    }
}
