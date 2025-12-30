package edu.travels.travelapp.model.dto;

public class ImageUploadDTO {
    private String description;
    private Double gpsLat;
    private Double gpsLng;
    private String locationName;

    public ImageUploadDTO() {}

    public ImageUploadDTO(String description, Double gpsLat, Double gpsLng, String locationName) {
        this.description = description;
        this.gpsLat = gpsLat;
        this.gpsLng = gpsLng;
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(Double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public Double getGpsLng() {
        return gpsLng;
    }

    public void setGpsLng(Double gpsLng) {
        this.gpsLng = gpsLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}