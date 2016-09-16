package i_will_pass.to_final_of.devchallenge_x.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * describes an object parsed from initial RSS response \
 */
public class InfoEntity implements Parcelable {

    private String title;
    private String link;
    private String pubDate;
    private String mediaContentUrl;
    private long fileSize;
    private String type;
    private String summary;

    public InfoEntity(String title, String link, String pubDate, String mediaContentUrl, long fileSize, String type, String summary) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.mediaContentUrl = mediaContentUrl;
        this.fileSize = fileSize;
        this.type = type;
        this.summary = summary;
    }

    protected InfoEntity(Parcel in) {
        title = in.readString();
        link = in.readString();
        pubDate = in.readString();
        mediaContentUrl = in.readString();
        fileSize = in.readLong();
        type = in.readString();
        summary = in.readString();
    }

    public static final Creator<InfoEntity> CREATOR = new Creator<InfoEntity>() {
        @Override
        public InfoEntity createFromParcel(Parcel in) {
            return new InfoEntity(in);
        }

        @Override
        public InfoEntity[] newArray(int size) {
            return new InfoEntity[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getMediaContentUrl() {
        return mediaContentUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        return "InfoEntity{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", mediaContentUrl='" + mediaContentUrl + '\'' +
                ", fileSize=" + fileSize +
                ", type='" + type + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(pubDate);
        parcel.writeString(mediaContentUrl);
        parcel.writeLong(fileSize);
        parcel.writeString(type);
        parcel.writeString(summary);
    }
}