package com.megaproject.urbanspace.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Patterns;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.megaproject.urbanspace.ui.CategoryDropdownAdapter;
import com.megaproject.urbanspace.Adapter.LostFoundAdapter;
import com.megaproject.urbanspace.Adapter.LostFoundResultAdapter;
import com.megaproject.urbanspace.Adapter.PhotoThumbnailAdapter;
import com.megaproject.urbanspace.Model.LostFoundItem;
import com.megaproject.urbanspace.R;

public class LostFoundFragment extends Fragment {

    private LostFoundViewModel viewModel;
    private TabLayout tabLayout;
    private ConstraintLayout contentLost;
    private ConstraintLayout contentFound;
    private TextView emptyFound;
    private LostFoundAdapter adapter;
    private boolean isUpdatingTabs = false;

    // Category dropdown views
    private View categoryDropdownContainer;
    private CardView categoryDropdownList;
    private TextView selectedCategoryText;
    private ImageView dropdownChevron;
    private ListView categoryListView;
    private CategoryDropdownAdapter categoryAdapter;

    private CardView photoUploadCard;
    private RecyclerView photosRecyclerView;
    private TextView photosLimitText;
    private PhotoThumbnailAdapter photoThumbnailAdapter;
    private java.util.List<Uri> selectedImageUris = new java.util.ArrayList<>();
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int MAX_PHOTOS = 5;

    private SwitchMaterial switchAllowContact;
    private View contactFieldsContainer;
    private TextInputLayout inputContactNameLayout;
    private TextInputLayout inputContactEmailLayout;
    private TextInputLayout inputContactPhoneLayout;
    private TextInputEditText inputContactName;
    private TextInputEditText inputContactEmail;
    private TextInputEditText inputContactPhone;
    private MaterialButton buttonReset;
    private MaterialButton buttonPost;

    // Browse and Results views
    private View browseCard;
    private View resultsCard;
    private TextInputEditText searchInput;
    private AutoCompleteTextView categoryFilter;
    private AutoCompleteTextView statusFilter;
    private RecyclerView resultsRecyclerView;
    private TextView resultsCount;
    private View emptyState;
    private LostFoundResultAdapter resultsAdapter;
    private List<LostFoundItem> allItems = new ArrayList<>();
    private List<LostFoundItem> filteredItems = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePickerInternal();
                    }
                }
        );

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) {
                        return;
                    }

                    int existingCount = selectedImageUris.size();
                    int availableSlots = MAX_PHOTOS - existingCount;

                    if (availableSlots <= 0) {
                        if (getContext() != null) {
                            Toast.makeText(
                                    getContext(),
                                    "Maximum " + MAX_PHOTOS + " images allowed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        return;
                    }

                    int toAdd = Math.min(availableSlots, uris.size());
                    int actuallyAdded = 0;

                    for (int i = 0; i < toAdd; i++) {
                        Uri uri = uris.get(i);
                        if (uri == null) {
                            continue;
                        }
                        selectedImageUris.add(uri);
                        actuallyAdded++;
                    }

                    if (photoThumbnailAdapter != null && actuallyAdded > 0) {
                        // Notify only the newly inserted range
                        photoThumbnailAdapter.notifyItemRangeInserted(existingCount, actuallyAdded);
                    }

                    updatePhotosCount();

                    if (uris.size() > availableSlots && getContext() != null) {
                        int remaining = availableSlots;
                        Toast.makeText(
                                getContext(),
                                "You can only add " + remaining + " more images. Maximum " + MAX_PHOTOS + " allowed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lost_found, container, false);

        viewModel = new ViewModelProvider(this).get(LostFoundViewModel.class);

        tabLayout = view.findViewById(R.id.tabLayout);
        contentLost = view.findViewById(R.id.contentLost);
        contentFound = view.findViewById(R.id.contentFound);
        emptyFound = view.findViewById(R.id.emptyFound);

        RecyclerView foundList = view.findViewById(R.id.foundList);
        foundList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LostFoundAdapter();
        foundList.setAdapter(adapter);

        // Category dropdown setup
        categoryDropdownContainer = view.findViewById(R.id.categoryDropdownContainer);
        categoryDropdownList = view.findViewById(R.id.categoryDropdownList);
        selectedCategoryText = view.findViewById(R.id.selectedCategoryText);
        dropdownChevron = view.findViewById(R.id.dropdownChevron);
        categoryListView = view.findViewById(R.id.categoryListView);

        photoUploadCard = view.findViewById(R.id.photoUploadCard);
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);
        photosLimitText = view.findViewById(R.id.photosLimitText);

        switchAllowContact = view.findViewById(R.id.switchAllowContact);
        contactFieldsContainer = view.findViewById(R.id.contactFieldsContainer);
        inputContactNameLayout = view.findViewById(R.id.inputContactNameLayout);
        inputContactEmailLayout = view.findViewById(R.id.inputContactEmailLayout);
        inputContactPhoneLayout = view.findViewById(R.id.inputContactPhoneLayout);
        inputContactName = view.findViewById(R.id.inputContactName);
        inputContactEmail = view.findViewById(R.id.inputContactEmail);
        inputContactPhone = view.findViewById(R.id.inputContactPhone);
        buttonReset = view.findViewById(R.id.buttonReset);
        buttonPost = view.findViewById(R.id.buttonPost);

        java.util.List<String> categories = java.util.Arrays.asList(
                "Electronics",
                "Accessories",
                "Documents",
                "Pet",
                "Clothing",
                "Other"
        );

        categoryAdapter = new CategoryDropdownAdapter(requireContext(), categories, 0);
        categoryListView.setAdapter(categoryAdapter);
        selectedCategoryText.setText(categories.get(0));
        expandListView(categoryListView);

        categoryDropdownContainer.setOnClickListener(v -> toggleCategoryDropdown());

        categoryListView.setOnItemClickListener((parent, itemView, position, id) -> {
            categoryAdapter.setSelectedPosition(position);
            String selected = categories.get(position);
            selectedCategoryText.setText(selected);
            categoryDropdownList.setVisibility(View.GONE);
            if (dropdownChevron != null) {
                dropdownChevron.animate().rotation(0f).setDuration(150).start();
            }
        });

        if (photosRecyclerView != null) {
            photosRecyclerView.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
            photoThumbnailAdapter = new PhotoThumbnailAdapter(
                    selectedImageUris,
                    this::updatePhotosCount
            );
            photosRecyclerView.setAdapter(photoThumbnailAdapter);
        }

        updatePhotosCount();

        if (photoUploadCard != null) {
            photoUploadCard.setOnClickListener(v -> handlePhotoUploadClick());
        }

        if (switchAllowContact != null && contactFieldsContainer != null) {
            switchAllowContact.setOnCheckedChangeListener((buttonView, isChecked) ->
                    handleAllowContactToggle(isChecked)
            );
        }

        if (buttonReset != null) {
            buttonReset.setOnClickListener(v -> {
                if (switchAllowContact != null) {
                    switchAllowContact.setChecked(false);
                }
                clearContactFields();
            });
        }

        if (buttonPost != null) {
            buttonPost.setOnClickListener(v -> {
                if (switchAllowContact != null && switchAllowContact.isChecked()) {
                    if (!validateContactFields()) {
                        return;
                    }
                }

                // TODO: hook up actual post logic when backend is ready
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Post submitted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Toggle: false = Lost Item, true = Found Item
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (isUpdatingTabs) return;

                    if (tab.getPosition() == 0) {
                        viewModel.setActiveTab(LostFoundViewModel.Tab.LOST);
                    } else {
                        viewModel.setActiveTab(LostFoundViewModel.Tab.FOUND);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) { }

                @Override
                public void onTabReselected(TabLayout.Tab tab) { }
            });
        }

        viewModel.getActiveTab().observe(getViewLifecycleOwner(), tab -> updateContent(tab));
        viewModel.getFoundItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submit(items);
            emptyFound.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // initialize default tab
        viewModel.setActiveTab(LostFoundViewModel.Tab.LOST);

        // Initialize browse and results cards
        initializeBrowseAndResults(view);

        return view;
    }

    private void updateContent(LostFoundViewModel.Tab tab) {
        if (getContext() == null) {
            return;
        }

        if (tabLayout != null) {
            isUpdatingTabs = true;
            TabLayout.Tab lostTab = tabLayout.getTabAt(0);
            TabLayout.Tab foundTab = tabLayout.getTabAt(1);

            if (tab == LostFoundViewModel.Tab.LOST) {
                if (lostTab != null) lostTab.select();
            } else {
                if (foundTab != null) foundTab.select();
            }
            isUpdatingTabs = false;
        }

        if (tab == LostFoundViewModel.Tab.LOST) {
            contentLost.setVisibility(View.VISIBLE);
            contentFound.setVisibility(View.GONE);
        } else {
            contentLost.setVisibility(View.GONE);
            contentFound.setVisibility(View.VISIBLE);
        }
    }

    private void handlePhotoUploadClick() {
        if (getContext() == null) {
            return;
        }

        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            openImagePickerInternal();
        } else if (permissionLauncher != null) {
            permissionLauncher.launch(permission);
        }
    }

    private void openImagePickerInternal() {
        if (imagePickerLauncher != null) {
            imagePickerLauncher.launch("image/*");
        }
    }

    private void updatePhotosCount() {
        if (photosLimitText == null) {
            return;
        }

        int count = selectedImageUris.size();
        if (count == 0) {
            photosLimitText.setText("Up to " + MAX_PHOTOS + " images");
        } else {
            photosLimitText.setText(count + "/" + MAX_PHOTOS + " images selected");
        }
    }

    private void handleAllowContactToggle(boolean enabled) {
        if (contactFieldsContainer == null) {
            return;
        }

        if (enabled) {
            if (contactFieldsContainer.getVisibility() == View.VISIBLE) {
                return;
            }
            contactFieldsContainer.setAlpha(0f);
            contactFieldsContainer.setTranslationY(16f);
            contactFieldsContainer.setVisibility(View.VISIBLE);
            contactFieldsContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
        } else {
            if (contactFieldsContainer.getVisibility() != View.VISIBLE) {
                clearContactFields();
                return;
            }
            contactFieldsContainer.animate()
                    .alpha(0f)
                    .translationY(16f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        contactFieldsContainer.setVisibility(View.GONE);
                        clearContactFields();
                    })
                    .start();
        }
    }

    private void clearContactFields() {
        if (inputContactName != null) {
            inputContactName.setText(null);
        }
        if (inputContactEmail != null) {
            inputContactEmail.setText(null);
        }
        if (inputContactPhone != null) {
            inputContactPhone.setText(null);
        }

        if (inputContactNameLayout != null) {
            inputContactNameLayout.setError(null);
        }
        if (inputContactEmailLayout != null) {
            inputContactEmailLayout.setError(null);
        }
        if (inputContactPhoneLayout != null) {
            inputContactPhoneLayout.setError(null);
        }
    }

    private boolean validateContactFields() {
        boolean isValid = true;

        String name = inputContactName != null && inputContactName.getText() != null
                ? inputContactName.getText().toString().trim()
                : "";
        String email = inputContactEmail != null && inputContactEmail.getText() != null
                ? inputContactEmail.getText().toString().trim()
                : "";
        String phone = inputContactPhone != null && inputContactPhone.getText() != null
                ? inputContactPhone.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            if (inputContactNameLayout != null) {
                inputContactNameLayout.setError("Name is required");
            }
            isValid = false;
        } else if (inputContactNameLayout != null) {
            inputContactNameLayout.setError(null);
        }

        if (email.isEmpty()) {
            if (inputContactEmailLayout != null) {
                inputContactEmailLayout.setError("Email is required");
            }
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (inputContactEmailLayout != null) {
                inputContactEmailLayout.setError("Enter a valid email");
            }
            isValid = false;
        } else if (inputContactEmailLayout != null) {
            inputContactEmailLayout.setError(null);
        }

        if (phone.isEmpty()) {
            if (inputContactPhoneLayout != null) {
                inputContactPhoneLayout.setError("Phone is required");
            }
            isValid = false;
        } else if (inputContactPhoneLayout != null) {
            inputContactPhoneLayout.setError(null);
        }

        return isValid;
    }

    private void toggleCategoryDropdown() {
        if (categoryDropdownList == null || dropdownChevron == null) {
            return;
        }

        if (categoryDropdownList.getVisibility() == View.VISIBLE) {
            categoryDropdownList.setVisibility(View.GONE);
            dropdownChevron.animate().rotation(0f).setDuration(150).start();
        } else {
            categoryDropdownList.setVisibility(View.VISIBLE);
            dropdownChevron.animate().rotation(180f).setDuration(150).start();
        }
    }

    private void expandListView(ListView listView) {
        if (listView == null || listView.getAdapter() == null) {
            return;
        }

        android.widget.ListAdapter adapter = listView.getAdapter();
        int totalHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(widthMeasureSpec, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * Math.max(adapter.getCount() - 1, 0));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void initializeBrowseAndResults(View view) {
        // Initialize browse card views
        browseCard = view.findViewById(R.id.browseCard);
        resultsCard = view.findViewById(R.id.resultsCard);
        
        if (browseCard != null) {
            searchInput = browseCard.findViewById(R.id.searchInput);
            categoryFilter = browseCard.findViewById(R.id.categoryFilter);
            statusFilter = browseCard.findViewById(R.id.statusFilter);
            
            // Setup search functionality
            if (searchInput != null) {
                searchInput.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterResults();
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });
            }
            
            // Setup category filter
            if (categoryFilter != null) {
                String[] categories = {"All Categories", "Electronics", "Accessories", "Documents", "Pet", "Clothing", "Other"};
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, categories);
                categoryFilter.setAdapter(categoryAdapter);
                categoryFilter.setOnItemClickListener((parent, view1, position, id) -> filterResults());
            }
            
            // Setup status filter
            if (statusFilter != null) {
                String[] statuses = {"All Statuses", "Open", "Found", "Closed"};
                ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, statuses);
                statusFilter.setAdapter(statusAdapter);
                statusFilter.setOnItemClickListener((parent, view1, position, id) -> filterResults());
            }
        }
        
        // Initialize results card views
        if (resultsCard != null) {
            resultsRecyclerView = resultsCard.findViewById(R.id.resultsRecyclerView);
            resultsCount = resultsCard.findViewById(R.id.resultsCount);
            emptyState = resultsCard.findViewById(R.id.emptyState);
            
            // Setup results RecyclerView
            if (resultsRecyclerView != null) {
                resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                resultsAdapter = new LostFoundResultAdapter(getContext(), new LostFoundResultAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(LostFoundItem item) {
                        // TODO: Open item details view
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Item clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDeleteClick(LostFoundItem item) {
                        // TODO: Handle item deletion
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Delete item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onStatusChangeClick(LostFoundItem item) {
                        // TODO: Show status change dialog
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Change status: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onContactClick(String contactType, String contactValue) {
                        // TODO: Handle contact actions (call, email, etc.)
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Contact " + contactType + ": " + contactValue, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                resultsRecyclerView.setAdapter(resultsAdapter);
            }
        }
        
        // Initialize with sample data
        initializeSampleData();
    }
    
    private void filterResults() {
        String searchQuery = searchInput != null && searchInput.getText() != null ? 
            searchInput.getText().toString().toLowerCase().trim() : "";
        String selectedCategory = categoryFilter != null && categoryFilter.getText() != null ? 
            categoryFilter.getText().toString() : "All Categories";
        String selectedStatus = statusFilter != null && statusFilter.getText() != null ? 
            statusFilter.getText().toString() : "All Statuses";
        
        filteredItems.clear();
        
        for (LostFoundItem item : allItems) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                item.getTitle().toLowerCase().contains(searchQuery) ||
                item.getDescription().toLowerCase().contains(searchQuery) ||
                (item.getLocation() != null && item.getLocation().toLowerCase().contains(searchQuery));
            
            boolean matchesCategory = selectedCategory.equals("All Categories") || 
                item.getCategory().equals(selectedCategory);
            
            boolean matchesStatus = selectedStatus.equals("All Statuses") || 
                item.getStatus().toLowerCase().equals(selectedStatus.toLowerCase());
            
            if (matchesSearch && matchesCategory && matchesStatus) {
                filteredItems.add(item);
            }
        }
        
        updateResultsDisplay();
    }
    
    private void updateResultsDisplay() {
        if (resultsAdapter != null) {
            resultsAdapter.updateItems(filteredItems);
        }
        
        if (resultsCount != null) {
            resultsCount.setText(String.valueOf(filteredItems.size()));
        }
        
        if (resultsRecyclerView != null && emptyState != null) {
            resultsRecyclerView.setVisibility(filteredItems.isEmpty() ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void initializeSampleData() {
        // Create sample data for testing
        allItems = new ArrayList<>();
        
        // Sample lost items
        allItems.add(new LostFoundItem(
            "1", "Silver ring with blue stone", "Diamond ring with blue stone found near railway station", 
            "Railway station", "Accessories", "Open", "12/10/2025, 2:17:19 PM", 
            true, "RudranshGawande", "rudranshgawande007@gmail.com", "1234567890",
            new ArrayList<>(), "lost"
        ));
        
        allItems.add(new LostFoundItem(
            "2", "Wireless earbuds", "Black wireless earbuds in charging case", 
            "Bus stop", "Electronics", "Found", "12/10/2025, 3:45:00 PM", 
            true, "John Doe", "john@example.com", "9876543210",
            new ArrayList<>(), "lost"
        ));
        
        allItems.add(new LostFoundItem(
            "3", "Leather wallet", "Brown leather wallet with ID cards", 
            "Shopping mall", "Accessories", "Closed", "12/09/2025, 10:30:00 AM", 
            false, "", "", "", new ArrayList<>(), "found"
        ));
        
        // Initialize filtered items with all items
        filteredItems = new ArrayList<>(allItems);
        updateResultsDisplay();
    }
}