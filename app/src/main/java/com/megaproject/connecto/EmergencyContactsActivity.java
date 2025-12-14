package com.megaproject.connecto;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.megaproject.connecto.Adapter.EmergencyContactAdapter;
import com.megaproject.connecto.Model.EmergencyContact;
import com.megaproject.connecto.R;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity implements EmergencyContactAdapter.OnContactToggleListener {

    private ImageButton backButton, addContactButton;
    private TextView manageContactsBtn;
    private SwitchMaterial globalSwitch;
    private RecyclerView contactsRecyclerView;
    private FloatingActionButton fabAddContact;
    
    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        initializeViews();
        setupRecyclerView();
        loadContacts();
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        addContactButton = findViewById(R.id.addContactButton);
        manageContactsBtn = findViewById(R.id.manageContactsBtn);
        globalSwitch = findViewById(R.id.globalSwitch);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        fabAddContact = findViewById(R.id.fabAddContact);
    }

    private void setupRecyclerView() {
        contactList = new ArrayList<>();
        adapter = new EmergencyContactAdapter(this, contactList, this);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(adapter);
    }

    private void loadContacts() {
        contactList.clear();
        String json = getSharedPreferences("EmergencyPrefs", MODE_PRIVATE).getString("saved_contacts", null);
        if (json != null) {
            try {
                org.json.JSONArray array = new org.json.JSONArray(json);
                for (int i=0; i<array.length(); i++) {
                     org.json.JSONObject obj = array.getJSONObject(i);
                     contactList.add(new EmergencyContact(
                         obj.getString("id"),
                         obj.getString("name"),
                         obj.getString("phone"),
                         obj.optString("image", ""),
                         obj.getBoolean("active"),
                         obj.optBoolean("system", false)
                     ));
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            // Mock data matching HTML
            contactList.add(new EmergencyContact("1", "Mom", "+1 (555) 012-3456", "url_mom", true, false));
            contactList.add(new EmergencyContact("2", "John Doe", "+1 (555) 987-6543", "url_john", true, false));
            contactList.add(new EmergencyContact("3", "Sarah Smith", "+1 (555) 111-2222", "url_sarah", false, false));
            contactList.add(new EmergencyContact("911", "Emergency Services", "911", "", true, true)); // System contact
            saveContacts();
        }
        
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());
        
        addContactButton.setOnClickListener(v -> openAddContactDialog());
        fabAddContact.setOnClickListener(v -> openAddContactDialog());
        
        manageContactsBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Tap a contact to edit", Toast.LENGTH_SHORT).show();
        });
        
        globalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Enabled" : "Disabled";
            Toast.makeText(this, "Global SOS Alerts " + status, Toast.LENGTH_SHORT).show();
            // Optional: Persist global switch if needed
        });
    }

    private void saveContacts() {
        try {
            org.json.JSONArray array = new org.json.JSONArray();
            for (EmergencyContact c : contactList) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", c.getId());
                obj.put("name", c.getName());
                obj.put("phone", c.getPhoneNumber());
                obj.put("image", c.getImageUrl());
                obj.put("active", c.isActive());
                obj.put("system", c.isSystemContact());
                array.put(obj);
            }
            getSharedPreferences("EmergencyPrefs", MODE_PRIVATE)
                .edit().putString("saved_contacts", array.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void openAddContactDialog() {
        showContactDialog(null);
    }

    private void showContactDialog(EmergencyContact contact) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        builder.setView(view);

        com.google.android.material.textfield.TextInputEditText etName = view.findViewById(R.id.etName);
        com.google.android.material.textfield.TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        
        if (contact != null) {
            etName.setText(contact.getName());
            etPhone.setText(contact.getPhoneNumber());
            builder.setTitle("Edit Contact");
            builder.setNeutralButton("Delete", (d, w) -> {
                contactList.remove(contact);
                saveContacts();
                adapter.notifyDataSetChanged();
            });
        } else {
            builder.setTitle("Add Contact");
        }

        builder.setPositiveButton("Save", (d, w) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if(!name.isEmpty() && !phone.isEmpty()){
                if (contact != null) {
                    contact.setName(name);
                    contact.setPhoneNumber(phone);
                } else {
                    EmergencyContact newContact = new EmergencyContact(
                        String.valueOf(System.currentTimeMillis()), 
                        name, phone, "", true, false
                    );
                    // Insert before the last item (assuming last is system 911)
                    // Or just add to end if list is simple
                    int insertIndex = contactList.size() > 0 ? contactList.size() - 1 : 0;
                    if (insertIndex < 0) insertIndex = 0;
                    // Check if last is system
                    if (contactList.size() > 0 && contactList.get(contactList.size()-1).isSystemContact()) {
                         contactList.add(insertIndex, newContact);
                    } else {
                         contactList.add(newContact);
                    }
                }
                saveContacts();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onToggle(EmergencyContact contact, boolean isChecked) {
        String status = isChecked ? "Active" : "Inactive";
        // Toast.makeText(this, contact.getName() + " SOS is now " + status, Toast.LENGTH_SHORT).show();
        saveContacts();
    }
    
    @Override
    public void onContactClick(EmergencyContact contact) {
        if (!contact.isSystemContact()) {
             showContactDialog(contact);
        } else {
             Toast.makeText(this, "Cannot edit system contact", Toast.LENGTH_SHORT).show();
        }
    }
}


