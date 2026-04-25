package com.tomaflow.app.ui.tasks;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TasksActivity — To-Do List Screen (stub).
 *
 * Corresponds to the Tasks tab in the bottom navigation.
 * Will display a list of tasks fetched from the Room database,
 * and allow the user to add / toggle / delete tasks.
 *
 * Prototype reference: TaskScreen.tsx
 *
 * TODO: Inflate activity_tasks.xml, wire RecyclerView, connect TaskRepository.
 */
public class TasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_tasks);
    }
}
