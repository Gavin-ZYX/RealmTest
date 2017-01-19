package com.example.gavin.realmtest;

import io.realm.RealmObject;

/**
 * Created by gavin on 16/12/4.
 */

public class User2 extends RealmObject {
    public String name;
    public int age;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}