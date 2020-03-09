package edu.boun.edgecloudsim.applications.rsu_placement.model;

import edu.boun.edgecloudsim.utils.Location;

import java.util.Objects;

public class Coordinate extends Location {

    private double lat;
    private double lng;

    public Coordinate(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.lat, lat) == 0 &&
                Double.compare(that.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }


    @Override
    public String toString() {
        return "Coordinate{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
