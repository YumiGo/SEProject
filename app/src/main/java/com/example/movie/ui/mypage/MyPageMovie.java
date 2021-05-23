package com.example.movie.ui.mypage;

public class MyPageMovie {
    String name;
    String date;
    String time;
    String theater;
    String person_num;
    String reservation_num;

    public MyPageMovie(String name, String date, String time, String theater, String person_num, String reservation_num) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.theater = theater;
        this.person_num = person_num;
        this.reservation_num = reservation_num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }

    public String getPerson_num() {
        return person_num;
    }

    public void setPerson_num(String person_num) {
        this.person_num = person_num;
    }

    public String getReservation_num() {
        return reservation_num;
    }

    public void setReservation_num(String reservation_num) {
        this.reservation_num = reservation_num;
    }
}
