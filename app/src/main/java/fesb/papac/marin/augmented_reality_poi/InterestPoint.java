package fesb.papac.marin.augmented_reality_poi;

/**
 * Created by Marin on 19.4.2017..
 */

public class InterestPoint {
    public double Latitude;
    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }


    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double Longitude;

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }

    public double Altitude;

    public String getPlace() {
        return Place;
    }

    public void setPlace(String place) {
        Place = place;
    }

    public String Place;

    InterestPoint(double Latitude, double Longitude, double Altitude, String Place)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Altitude = Altitude;
        this.Place = Place;
    }
}

