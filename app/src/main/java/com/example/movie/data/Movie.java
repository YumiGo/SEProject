package com.example.movie.data;

import android.graphics.Bitmap;


// 영화 정보 저장용
public class Movie {
    private int id;
    private String name;
    private String content;
    private String director;
    private String imageName;
    private Bitmap image;

    public Movie() {
        this(-1, "", "", "", "", null);
    }

    public Movie(int id, String name, String content, String director, String imageName) {
        this(id, name, content, director, imageName, null);
    }

    public Movie(int id, String name, String content, String director, String imageName, Bitmap image) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.director = director;
        this.imageName = imageName;
        this.image = image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getDirector() {
        return director;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getImageName() {
        return imageName;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", director='" + director + '\'' +
                ", imageName='" + imageName + '\'' +
                ", image='" + (imageName == null ? "null" : "존재") + '\'' +
                '}';
    }
}
