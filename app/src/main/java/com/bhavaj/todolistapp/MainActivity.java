package com.bhavaj.todolistapp;
import androidx.appcompat.widget.Toolbar;
import android.content.DialogInterface;  // For DialogInterface
import android.widget.Toast;  // For Toast messages


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bhavaj.todolistapp.TaskContract;
import com.bhavaj.todolistapp.TaskDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ToDo List");

        getSupportActionBar().setTitle("ToDo List");
        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.list_todo);

        updateUI();
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_task) {  // Logic for "Add Task" button click
            // Create an EditText for the user to input the task title
            final EditText taskEditText = new EditText(this);

            // Create an AlertDialog to show the input box
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Add a new task")  // Title of the dialog
                    .setMessage("Enter task details")  // Message in the dialog
                    .setView(taskEditText)  // Set the EditText as the dialog view
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Get the inputted task text
                            String task = taskEditText.getText().toString();

                            // Only add task if it's not empty
                            if (!task.isEmpty()) {
                                // Access database and insert the new task
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);

                                // Insert the task into the database
                                db.insert(TaskContract.TaskEntry.TABLE, null, values);
                                db.close();

                                // Update the UI after adding the task
                                updateUI();
                            } else {
                                // Show a message if the task is empty
                                Toast.makeText(MainActivity.this, "Task cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)  // Cancel button for the dialog
                    .create()
                    .show();

            return true;  // Indicate the item was handled
        }

        return super.onOptionsItemSelected(item);  // Handle other menu items if any
    }



    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }
}
