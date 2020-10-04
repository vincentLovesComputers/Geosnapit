package com.vincent.govermentcomplaintapp;


import androidx.appcompat.app.AppCompatActivity;

import com.vincent.govermentcomplaintapp.models.User;
import com.vincent.govermentcomplaintapp.models.Users;


public class UserClient extends AppCompatActivity {

        private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
            this.user = user;
        }
}
