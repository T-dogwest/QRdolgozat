package com.example.qrdolgozat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaAdatok_Activity extends AppCompatActivity {

    private LinearLayout linearlayout;
    private ListView data;
    private EditText Id;
    private EditText Name;
    private EditText Grade;
    private Button ButtonModify;
    private Button ButtonCancel;
    private String url;
    private List<Student> students = new ArrayList<>();

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_adatok);
        init();

        ButtonModify.setOnClickListener(v -> {
            int id = Integer.parseInt(Id.getText().toString());
            String name = Name.getText().toString();
            String grade = Grade.getText().toString();
            Student editedPerson = new Student(id, name, grade);
            new RequestTask(url + "/" + id, "PUT", editedPerson).execute();
        });

        ButtonCancel.setOnClickListener(v -> {
            linearlayout.setVisibility(LinearLayout.GONE);
            resetForm();
        });

        linearlayout.setVisibility(LinearLayout.GONE);

        new RequestTask(url, "GET").execute();
    }

    private void init() {
        linearlayout = findViewById(R.id.linearlayout);
        data = findViewById(R.id.data);
        Id = findViewById(R.id.Id);
        Name = findViewById(R.id.Name);
        Grade = findViewById(R.id.Grade);
        ButtonModify = findViewById(R.id.ButtonModify);
        ButtonCancel = findViewById(R.id.ButtonCancel);

        sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        url = sharedPreferences.getString("url", null);
        Toast.makeText(this, "URL: " + url, Toast.LENGTH_SHORT).show();

        data.setAdapter(new StudentAdapter());
    }

    private void resetForm() {
        Id.setText("");
        Name.setText("");
        Grade.setText("");
    }

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        Object requestData;

        // Konstruktor, ha van requestData
        public RequestTask(String requestUrl, String requestType, Object requestData) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestData = requestData;
        }

        // Konstruktor, ha nincs requestData
        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestData = null; // null értéket adunk requestData-nak
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(getApplicationContext(), "Hiba történt a kérés során",
                        Toast.LENGTH_SHORT).show();
                Log.d("Error", response.getResponseCode() + "");
            }
            switch (requestType) {
                case "GET":
                   Student[] studentsArray = converter.fromJson(response.getResponseMessage(),
                            Student[].class);
                    students.clear();
                    for (Student student : studentsArray) {
                        students.add(student);
                    }
                 /*  Toast.makeText(getApplicationContext(), "GET: " + students.size(),
                            Toast.LENGTH_SHORT).show();*/
                    ((StudentAdapter) data.getAdapter()).notifyDataSetChanged();
                    break;

                case "PUT":
                    Student putStudent = converter.fromJson(response.getResponseMessage(),
                            Student.class);
                    for (int i = 0; i < students.size(); i++) {
                        if (students.get(i).getId() == putStudent.getId()) {
                            students.set(i, putStudent);
                            break;
                        }
                    }
                    resetForm();
                    break;
            }
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestType) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);
                        break;

                    case "PUT":
                        response = RequestHandler.put(requestUrl, new Gson().toJson(requestData));
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            return response;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private class StudentAdapter extends ArrayAdapter<Student> {
        public StudentAdapter() {
            super(ListaAdatok_Activity.this, R.layout.person_list_adapter, students);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.person_list_adapter, null, false);

            Student actualStudent = students.get(position);

            TextView aModify = view.findViewById(R.id.aModify);
            TextView aName = view.findViewById(R.id.aName);
            TextView aGrade = view.findViewById(R.id.aGrade);

            aName.setText(actualStudent.getName());
            aGrade.setText(String.valueOf(actualStudent.getGrade()));

            if (TextUtils.isDigitsOnly(actualStudent.getGrade()) && Integer.parseInt(actualStudent.getGrade()) == 0) {
                aGrade.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                aGrade.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            aModify.setOnClickListener(v -> {
                linearlayout.setVisibility(LinearLayout.VISIBLE);
                Id.setText(String.valueOf(actualStudent.getId()));
                Name.setText(actualStudent.getName());
                Grade.setText(String.valueOf(actualStudent.getGrade()));
            });

            return view;
        }
    }
}
