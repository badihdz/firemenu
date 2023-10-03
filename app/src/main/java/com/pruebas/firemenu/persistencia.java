package com.pruebas.firemenu;

import com.google.firebase.database.FirebaseDatabase;

public class persistencia extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
