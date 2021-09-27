package com.ssr_projects.findmyphone.Adapter;


import android.os.Parcel;
import android.os.Parcelable;

public class NumberDataHolder implements Parcelable {

    String userName;
    String userNumber;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public NumberDataHolder(String userName, String userNumber) {
        this.userName = userName;
        this.userNumber = userNumber;
    }

    protected NumberDataHolder(Parcel in) {
        userName = in.readString();
        userNumber = in.readString();
    }

    public static final Creator<NumberDataHolder> CREATOR = new Creator<NumberDataHolder>() {
        @Override
        public NumberDataHolder createFromParcel(Parcel in) {
            return new NumberDataHolder(in);
        }

        @Override
        public NumberDataHolder[] newArray(int size) {
            return new NumberDataHolder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeString(userNumber);
    }
}
