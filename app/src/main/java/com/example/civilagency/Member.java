package com.example.civilagency;

public class Member {
    private Integer status_int;

    public Member(int status_int) {
        this.status_int = status_int;
    }

    public Member() {

    }

    public int getStatus_int() {
        return status_int;
    }

    public void setStatus_int(int status_int) {
        this.status_int = status_int;
    }
}
