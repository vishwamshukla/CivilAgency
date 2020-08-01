package com.example.civilagency;

public class Users {
    public String name;
    public String status;
    public String image;
    public String thumb_image;
    public String usertype;
    Users(){ }

    public Users(String name, String status, String image, String thumb_image, String usertype) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image=thumb_image;
        this.usertype = usertype;
    }

    public String getName() { return name; }


    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public void setThumbImage(String thumbImage) {
        this.thumb_image = thumb_image;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}
