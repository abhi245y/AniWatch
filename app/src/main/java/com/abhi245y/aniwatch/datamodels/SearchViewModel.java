package com.abhi245y.aniwatch.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SearchViewModel implements Parcelable {

    String anime_name;
    String gogo_id;
    String poster_link;
    String total_ep;

    protected SearchViewModel(Parcel in) {
        anime_name = in.readString();
        gogo_id = in.readString();
        poster_link = in.readString();
        total_ep = in.readString();
    }

    public static final Creator<SearchViewModel> CREATOR = new Creator<SearchViewModel>() {
        @Override
        public SearchViewModel createFromParcel(Parcel in) {
            return new SearchViewModel(in);
        }

        @Override
        public SearchViewModel[] newArray(int size) {
            return new SearchViewModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(anime_name);
        parcel.writeString(gogo_id);
        parcel.writeString(poster_link);
        parcel.writeString(total_ep);
    }
}
