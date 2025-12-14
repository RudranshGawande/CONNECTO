package com.megaproject.connecto.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.megaproject.connecto.ui.CategoryDropdownAdapter;
import com.megaproject.connecto.Adapter.LostFoundAdapter;
import com.megaproject.connecto.Adapter.LostFoundResultAdapter;
import com.megaproject.connecto.Adapter.PhotoThumbnailAdapter;
import com.megaproject.connecto.Model.LostFoundItem;
import com.megaproject.connecto.R;

public class LostFoundFragment extends Fragment {

    private LostFoundViewModel viewModel;
    private TabLayout tabLayout;
    private ConstraintLayout contentLost;
    private ConstraintLayout contentFound;
    private TextView emptyFound;
    private LostFoundAdapter adapter;
    private boolean isUpdatingTabs = false;
    private boolean isUploadingForFound = false;

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
    
    // Found Photos
    private PhotoThumbnailAdapter photoThumbnailAdapterFound;
    private java.util.List<Uri> selectedImageUrisFound = new java.util.ArrayList<>();
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int MAX_PHOTOS = 5;

    private SwitchMaterial switchAllowContact;
    private View contactFieldsContainer;
    private TextInputLayout inputTitleLayout;
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
    private TextView categoryFilterText;
    private TextView statusFilterText;
    private View btnFilterCategory;
    private View btnFilterStatus;
    private RecyclerView resultsRecyclerView;
    private TextView resultsCount;
    private View emptyState;
    private LostFoundResultAdapter resultsAdapter;

    // Found Browse/Results Views
    private View browseCardFound;
    private View resultsCardFound;
    private TextInputEditText searchInputFound;
    private TextView categoryFilterTextFound;
    private TextView statusFilterTextFound;
    private View btnFilterCategoryFound;
    private View btnFilterStatusFound;

    private RecyclerView resultsRecyclerViewFound;
    private TextView resultsCountFound;
    private View emptyStateFound;
    private LostFoundResultAdapter resultsAdapterFound;

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

                    List<Uri> targetList = isUploadingForFound ? selectedImageUrisFound : selectedImageUris;
                    PhotoThumbnailAdapter targetAdapter = isUploadingForFound ? photoThumbnailAdapterFound : photoThumbnailAdapter;

                    int existingCount = targetList.size();
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
                        targetList.add(uri);
                        actuallyAdded++;
                    }

                    if (targetAdapter != null && actuallyAdded > 0) {
                        targetAdapter.notifyItemRangeInserted(existingCount, actuallyAdded);
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
        inputTitleLayout = view.findViewById(R.id.inputTitleLayout);
        inputContactNameLayout = view.findViewById(R.id.inputContactNameLayout);
        inputContactEmailLayout = view.findViewById(R.id.inputContactEmailLayout);
        inputContactPhoneLayout = view.findViewById(R.id.inputContactPhoneLayout);
        inputContactName = view.findViewById(R.id.inputContactName);
        inputContactEmail = view.findViewById(R.id.inputContactEmail);
        inputContactPhone = view.findViewById(R.id.inputContactPhone);
        buttonReset = view.findViewById(R.id.buttonReset);
        buttonPost = view.findViewById(R.id.buttonPost);

        // Found UI Initialization
        View categoryDropdownContainerFound = view.findViewById(R.id.categoryDropdownContainerFound);
        CardView categoryDropdownListFound = view.findViewById(R.id.categoryDropdownListFound);
        TextView selectedCategoryTextFound = view.findViewById(R.id.selectedCategoryTextFound);
        ImageView dropdownChevronFound = view.findViewById(R.id.dropdownChevronFound);
        ListView categoryListViewFound = view.findViewById(R.id.categoryListViewFound);

        CardView photoUploadCardFound = view.findViewById(R.id.photoUploadCardFound);
        RecyclerView photosRecyclerViewFound = view.findViewById(R.id.photosRecyclerViewFound);
        TextView photosLimitTextFound = view.findViewById(R.id.photosLimitTextFound);

        SwitchMaterial switchAllowContactFound = view.findViewById(R.id.switchAllowContactFound);
        View contactFieldsContainerFound = view.findViewById(R.id.contactFieldsContainerFound);
        
        TextInputLayout inputContactNameLayoutFound = view.findViewById(R.id.inputContactNameLayoutFound);
        TextInputLayout inputContactEmailLayoutFound = view.findViewById(R.id.inputContactEmailLayoutFound);
        TextInputLayout inputContactPhoneLayoutFound = view.findViewById(R.id.inputContactPhoneLayoutFound);
        
        TextInputEditText inputContactNameFound = view.findViewById(R.id.inputContactNameFound);
        TextInputEditText inputContactEmailFound = view.findViewById(R.id.inputContactEmailFound);
        TextInputEditText inputContactPhoneFound = view.findViewById(R.id.inputContactPhoneFound);
        
        MaterialButton buttonResetFound = view.findViewById(R.id.buttonResetFound);
        MaterialButton buttonPostFound = view.findViewById(R.id.buttonPostFound);

        java.util.List<String> categories = java.util.Arrays.asList(
                "Electronics",
                "Accessories",
                "Documents",
                "Pet",
                "Clothing",
                "Other"
        );

        // Lost Category Dropdown
        // We now use ListPopupWindow via showUnifiedDropdown instead of the expandable CardView
        categoryDropdownContainer.setOnClickListener(v -> showUnifiedDropdown(categoryDropdownContainer, categories, selectedCategoryText, null));

        // Found Category Dropdown
        if (categoryDropdownContainerFound != null) {
            categoryDropdownContainerFound.setOnClickListener(v -> 
                showUnifiedDropdown(categoryDropdownContainerFound, categories, selectedCategoryTextFound, null));
        }
        
        // Hide legacy dropdown lists if they were visible by default (they are GONE in xml, so safe)
        if (categoryDropdownList != null) categoryDropdownList.setVisibility(View.GONE);
        if (categoryDropdownListFound != null) categoryDropdownListFound.setVisibility(View.GONE);

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
        
        // Found Photos Setup
        if (photosRecyclerViewFound != null) {
            photosRecyclerViewFound.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
            photoThumbnailAdapterFound = new PhotoThumbnailAdapter(
                    selectedImageUrisFound,
                    this::updatePhotosCount
            );
            photosRecyclerViewFound.setAdapter(photoThumbnailAdapterFound);
        }

        updatePhotosCount();

        if (photoUploadCard != null) {
            photoUploadCard.setOnClickListener(v -> handlePhotoUploadClick(false));
        }
        if (photoUploadCardFound != null) {
            photoUploadCardFound.setOnClickListener(v -> handlePhotoUploadClick(true));
        }

        if (switchAllowContact != null && contactFieldsContainer != null) {
            switchAllowContact.setOnCheckedChangeListener((buttonView, isChecked) ->
                    handleAllowContactToggle(isChecked, contactFieldsContainer, inputContactName, inputContactEmail, inputContactPhone, inputContactNameLayout, inputContactEmailLayout, inputContactPhoneLayout)
            );
        }
        
        if (switchAllowContactFound != null && contactFieldsContainerFound != null) {
            switchAllowContactFound.setOnCheckedChangeListener((buttonView, isChecked) ->
                    handleAllowContactToggle(isChecked, contactFieldsContainerFound, inputContactNameFound, inputContactEmailFound, inputContactPhoneFound, inputContactNameLayoutFound, inputContactEmailLayoutFound, inputContactPhoneLayoutFound)
            );
        }

        if (buttonReset != null) {
            buttonReset.setOnClickListener(v -> {
                if (switchAllowContact != null) {
                    switchAllowContact.setChecked(false);
                }
                clearContactFields(inputContactName, inputContactEmail, inputContactPhone, inputContactNameLayout, inputContactEmailLayout, inputContactPhoneLayout);
            });
        }
        
        if (buttonResetFound != null) {
            buttonResetFound.setOnClickListener(v -> {
                if (switchAllowContactFound != null) switchAllowContactFound.setChecked(false);
                clearContactFields(inputContactNameFound, inputContactEmailFound, inputContactPhoneFound, inputContactNameLayoutFound, inputContactEmailLayoutFound, inputContactPhoneLayoutFound);
                
                // Clear Found Form Fields
                TextInputEditText tFound = view.findViewById(R.id.inputTitleFound);
                TextInputEditText dFound = view.findViewById(R.id.inputDescriptionFound);
                TextInputEditText lFound = view.findViewById(R.id.inputLastLocationFound);
                if(tFound != null) tFound.setText(null);
                if(dFound != null) dFound.setText(null);
                if(lFound != null) lFound.setText(null);
                
                selectedImageUrisFound.clear();
                if (photoThumbnailAdapterFound != null) photoThumbnailAdapterFound.notifyDataSetChanged();
                updatePhotosCount();
            });
        }

        if (buttonPost != null) {
            buttonPost.setOnClickListener(v -> {
                if (switchAllowContact != null && switchAllowContact.isChecked()) {
                    if (!validateContactFields(inputContactName, inputContactEmail, inputContactPhone, inputContactNameLayout, inputContactEmailLayout, inputContactPhoneLayout)) {
                        return;
                    }
                }

                handleCreateLostItem();
            });
        }
        
        if (buttonPostFound != null) {
            buttonPostFound.setOnClickListener(v -> {
                if (switchAllowContactFound != null && switchAllowContactFound.isChecked()) {
                    if (!validateContactFields(inputContactNameFound, inputContactEmailFound, inputContactPhoneFound, inputContactNameLayoutFound, inputContactEmailLayoutFound, inputContactPhoneLayoutFound)) {
                        return;
                    }
                }
                
                handleCreateFoundItem(view, selectedCategoryTextFound, switchAllowContactFound, inputContactNameFound, inputContactEmailFound, inputContactPhoneFound);
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

        // initialize default tab
        viewModel.setActiveTab(LostFoundViewModel.Tab.LOST);

        // Initialize browse and results cards
        initializeBrowseAndResults(view);
        initializeFoundBrowseAndResults(view);

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

    private void handlePhotoUploadClick(boolean isFound) {
        if (getContext() == null) {
            return;
        }
        
        this.isUploadingForFound = isFound;

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
        TextView targetText = isUploadingForFound ? 
            (getView() != null ? getView().findViewById(R.id.photosLimitTextFound) : null) : photosLimitText;
            
        if (targetText == null) return;

        int count = isUploadingForFound ? selectedImageUrisFound.size() : selectedImageUris.size();
        if (count == 0) {
            targetText.setText("Up to " + MAX_PHOTOS + " images");
        } else {
            targetText.setText(count + "/" + MAX_PHOTOS + " images selected");
        }
    }

    private void handleAllowContactToggle(boolean enabled, View container, TextInputEditText name, TextInputEditText email, TextInputEditText phone, TextInputLayout lName, TextInputLayout lEmail, TextInputLayout lPhone) {
        if (container == null) {
            return;
        }

        if (enabled) {
            if (container.getVisibility() == View.VISIBLE) {
                return;
            }
            container.setAlpha(0f);
            container.setTranslationY(16f);
            container.setVisibility(View.VISIBLE);
            container.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
        } else {
            if (container.getVisibility() != View.VISIBLE) {
                clearContactFields(name, email, phone, lName, lEmail, lPhone);
                return;
            }
            container.animate()
                    .alpha(0f)
                    .translationY(16f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        container.setVisibility(View.GONE);
                        clearContactFields(name, email, phone, lName, lEmail, lPhone);
                    })
                    .start();
        }
    }

    private void clearContactFields(TextInputEditText name, TextInputEditText email, TextInputEditText phone, TextInputLayout lName, TextInputLayout lEmail, TextInputLayout lPhone) {
        if (name != null) name.setText(null);
        if (email != null) email.setText(null);
        if (phone != null) phone.setText(null);

        if (lName != null) lName.setError(null);
        if (lEmail != null) lEmail.setError(null);
        if (lPhone != null) lPhone.setError(null);
    }

    private boolean validateContactFields(TextInputEditText inputName, TextInputEditText inputEmail, TextInputEditText inputPhone, TextInputLayout layoutName, TextInputLayout layoutEmail, TextInputLayout layoutPhone) {
        boolean isValid = true;

        String name = inputName != null && inputName.getText() != null
                ? inputName.getText().toString().trim()
                : "";
        String email = inputEmail != null && inputEmail.getText() != null
                ? inputEmail.getText().toString().trim()
                : "";
        String phone = inputPhone != null && inputPhone.getText() != null
                ? inputPhone.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            if (layoutName != null) {
                layoutName.setError("Name is required");
            }
            isValid = false;
        } else if (layoutName != null) {
            layoutName.setError(null);
        }

        if (email.isEmpty()) {
            if (layoutEmail != null) {
                layoutEmail.setError("Email is required");
            }
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (layoutEmail != null) {
                layoutEmail.setError("Enter a valid email");
            }
            isValid = false;
        } else if (layoutEmail != null) {
            layoutEmail.setError(null);
        }

        if (phone.isEmpty()) {
            if (layoutPhone != null) {
                layoutPhone.setError("Phone is required");
            }
            isValid = false;
        } else if (layoutPhone != null) {
            layoutPhone.setError(null);
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
                categoryFilterText = browseCard.findViewById(R.id.categoryFilterText);
                statusFilterText = browseCard.findViewById(R.id.statusFilterText);
                btnFilterCategory = browseCard.findViewById(R.id.btnFilterCategory);
                btnFilterStatus = browseCard.findViewById(R.id.btnFilterStatus);
                
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
                if (btnFilterCategory != null) {
                    btnFilterCategory.setOnClickListener(v -> showCategoryPopup(v, categoryFilterText));
                }
                
                // Setup status filter
                if (btnFilterStatus != null) {
                    btnFilterStatus.setOnClickListener(v -> showStatusPopup(v, statusFilterText));
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

        // Start with empty list; items will be added when user posts or when backend is wired
        allItems = new ArrayList<>();
        filteredItems = new ArrayList<>();
        updateResultsDisplay();
    }

    private void filterResults() {
        if (viewModel == null || viewModel.getActiveTab().getValue() == null) return;
        boolean isFoundTab = viewModel.getActiveTab().getValue() == LostFoundViewModel.Tab.FOUND;

        TextInputEditText searchField = isFoundTab ? searchInputFound : searchInput;
        TextView categoryField = isFoundTab ? categoryFilterTextFound : categoryFilterText;
        TextView statusField = isFoundTab ? statusFilterTextFound : statusFilterText;

        String searchQuery = searchField != null && searchField.getText() != null ? 
            searchField.getText().toString().toLowerCase().trim() : "";
        String selectedCategory = categoryField != null && categoryField.getText() != null ? 
            categoryField.getText().toString() : "All Categories";
        String selectedStatus = statusField != null && statusField.getText() != null ? 
            statusField.getText().toString() : "All Statuses";
        
        filteredItems.clear();
        
        for (LostFoundItem item : allItems) {
            if (item == null) continue;

            String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
            String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
            String location = item.getLocation() != null ? item.getLocation().toLowerCase() : "";

            boolean matchesSearch = searchQuery.isEmpty() || 
                title.contains(searchQuery) ||
                description.contains(searchQuery) ||
                location.contains(searchQuery);
            
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
        
        // Update Found Results
        if (resultsAdapterFound != null) {
            resultsAdapterFound.updateItems(filteredItems);
        }
        
        if (resultsCountFound != null) {
            resultsCountFound.setText(String.valueOf(filteredItems.size()));
        }
        
        if (resultsRecyclerViewFound != null && emptyStateFound != null) {
            resultsRecyclerViewFound.setVisibility(filteredItems.isEmpty() ? View.GONE : View.VISIBLE);
            emptyStateFound.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void initializeFoundBrowseAndResults(View view) {
        browseCardFound = view.findViewById(R.id.browseCardFound);
        resultsCardFound = view.findViewById(R.id.resultsCardFound);
        
        if (browseCardFound != null) {
            searchInputFound = browseCardFound.findViewById(R.id.searchInput);
            categoryFilterTextFound = browseCardFound.findViewById(R.id.categoryFilterText);
            statusFilterTextFound = browseCardFound.findViewById(R.id.statusFilterText);
            btnFilterCategoryFound = browseCardFound.findViewById(R.id.btnFilterCategory);
            btnFilterStatusFound = browseCardFound.findViewById(R.id.btnFilterStatus);
            
            if (searchInputFound != null) {
                searchInputFound.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterResults(); // Reuse same filter logic for now
                    }
                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });
            }
            
            if (btnFilterCategoryFound != null) {
                btnFilterCategoryFound.setOnClickListener(v -> showCategoryPopup(v, categoryFilterTextFound));
            }
            if (btnFilterStatusFound != null) {
                btnFilterStatusFound.setOnClickListener(v -> showStatusPopup(v, statusFilterTextFound));
            }
        }
        
        if (resultsCardFound != null) {
            resultsRecyclerViewFound = resultsCardFound.findViewById(R.id.resultsRecyclerView);
            resultsCountFound = resultsCardFound.findViewById(R.id.resultsCount);
            emptyStateFound = resultsCardFound.findViewById(R.id.emptyState);
            
            if (resultsRecyclerViewFound != null) {
                resultsRecyclerViewFound.setLayoutManager(new LinearLayoutManager(getContext()));
                resultsAdapterFound = new LostFoundResultAdapter(getContext(), new LostFoundResultAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(LostFoundItem item) {
                         if (getContext() != null) Toast.makeText(getContext(), "Item clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onDeleteClick(LostFoundItem item) {
                         if (getContext() != null) Toast.makeText(getContext(), "Delete: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onStatusChangeClick(LostFoundItem item) {
                         if (getContext() != null) Toast.makeText(getContext(), "Status: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onContactClick(String type, String val) {
                         if (getContext() != null) Toast.makeText(getContext(), "Contact " + type + ": " + val, Toast.LENGTH_SHORT).show();
                    }
                });
                resultsRecyclerViewFound.setAdapter(resultsAdapterFound);
            }
        }
    }
    
    private void handleCreateLostItem() {
        View root = getView();
        if (root == null || getContext() == null) {
            return;
        }

        TextInputEditText inputTitle = root.findViewById(R.id.inputTitle);
        TextInputEditText inputDescription = root.findViewById(R.id.inputDescription);
        TextInputEditText inputLastLocation = root.findViewById(R.id.inputLastLocation);

        String title = inputTitle != null && inputTitle.getText() != null
                ? inputTitle.getText().toString().trim() : "";
        String description = inputDescription != null && inputDescription.getText() != null
                ? inputDescription.getText().toString().trim() : "";
        String location = inputLastLocation != null && inputLastLocation.getText() != null
                ? inputLastLocation.getText().toString().trim() : "";

        if (title.isEmpty()) {
            if (inputTitleLayout != null) {
                inputTitleLayout.setError("Title is required");
            }
            return;
        } else if (inputTitleLayout != null) {
            inputTitleLayout.setError(null);
        }

        String category = selectedCategoryText != null
                ? selectedCategoryText.getText().toString()
                : "Other";

        boolean allowContact = switchAllowContact != null && switchAllowContact.isChecked();
        String contactName = allowContact && inputContactName != null && inputContactName.getText() != null
                ? inputContactName.getText().toString().trim() : "";
        String contactEmail = allowContact && inputContactEmail != null && inputContactEmail.getText() != null
                ? inputContactEmail.getText().toString().trim() : "";
        String contactPhone = allowContact && inputContactPhone != null && inputContactPhone.getText() != null
                ? inputContactPhone.getText().toString().trim() : "";

        List<String> imageUrisStrings = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            if (uri != null) {
                imageUrisStrings.add(uri.toString());
            }
        }

        String id = String.valueOf(System.currentTimeMillis());
        String dateTime = new SimpleDateFormat("MM/dd/yyyy, h:mm:ss a", Locale.getDefault())
                .format(new Date());

        LostFoundItem item = new LostFoundItem(
                id,
                title,
                description,
                location,
                category,
                "open",
                dateTime,
                allowContact,
                contactName,
                contactEmail,
                contactPhone,
                imageUrisStrings,
                "lost"
        );

        allItems.add(0, item);
        filterResults();

        // Clear form and photos after posting
        if (inputTitle != null) inputTitle.setText(null);
        if (inputDescription != null) inputDescription.setText(null);
        if (inputLastLocation != null) inputLastLocation.setText(null);
        if (switchAllowContact != null) switchAllowContact.setChecked(false);
        clearContactFields(inputContactName, inputContactEmail, inputContactPhone, inputContactNameLayout, inputContactEmailLayout, inputContactPhoneLayout);

        selectedImageUris.clear();
        if (photoThumbnailAdapter != null) {
            photoThumbnailAdapter.notifyDataSetChanged();
        }
        updatePhotosCount();

        Toast.makeText(getContext(), "Item posted", Toast.LENGTH_SHORT).show();
    }

    private void handleCreateFoundItem(View root, TextView selectedCategoryTextFound, SwitchMaterial switchAllowContactFound, TextInputEditText contactNameV, TextInputEditText contactEmailV, TextInputEditText contactPhoneV) {
        if (root == null || getContext() == null) {
            return;
        }

        TextInputEditText inputTitle = root.findViewById(R.id.inputTitleFound);
        TextInputEditText inputDescription = root.findViewById(R.id.inputDescriptionFound);
        TextInputEditText inputLastLocation = root.findViewById(R.id.inputLastLocationFound);
        TextInputLayout inputTitleLayout = root.findViewById(R.id.inputTitleLayoutFound);

        String title = inputTitle != null && inputTitle.getText() != null
                ? inputTitle.getText().toString().trim() : "";
        String description = inputDescription != null && inputDescription.getText() != null
                ? inputDescription.getText().toString().trim() : "";
        String location = inputLastLocation != null && inputLastLocation.getText() != null
                ? inputLastLocation.getText().toString().trim() : "";

        if (title.isEmpty()) {
            if (inputTitleLayout != null) {
                inputTitleLayout.setError("Title is required");
            }
            return;
        } else if (inputTitleLayout != null) {
            inputTitleLayout.setError(null);
        }

        String category = selectedCategoryTextFound != null
                ? selectedCategoryTextFound.getText().toString()
                : "Other";

        boolean allowContact = switchAllowContactFound != null && switchAllowContactFound.isChecked();
        String contactName = allowContact && contactNameV != null && contactNameV.getText() != null
                ? contactNameV.getText().toString().trim() : "";
        String contactEmail = allowContact && contactEmailV != null && contactEmailV.getText() != null
                ? contactEmailV.getText().toString().trim() : "";
        String contactPhone = allowContact && contactPhoneV != null && contactPhoneV.getText() != null
                ? contactPhoneV.getText().toString().trim() : "";

        List<String> imageUrisStrings = new ArrayList<>();
        for (Uri uri : selectedImageUrisFound) {
            if (uri != null) {
                imageUrisStrings.add(uri.toString());
            }
        }

        String id = String.valueOf(System.currentTimeMillis());
        String dateTime = new SimpleDateFormat("MM/dd/yyyy, h:mm:ss a", Locale.getDefault())
                .format(new Date());

        LostFoundItem item = new LostFoundItem(
                id,
                title,
                description,
                location,
                category,
                "open",
                dateTime,
                allowContact,
                contactName,
                contactEmail,
                contactPhone,
                imageUrisStrings,
                "found"
        );

        allItems.add(0, item);
        filterResults();

        // Clear form and photos after posting
        if (inputTitle != null) inputTitle.setText(null);
        if (inputDescription != null) inputDescription.setText(null);
        if (inputLastLocation != null) inputLastLocation.setText(null);
        if (switchAllowContactFound != null) switchAllowContactFound.setChecked(false);
        
        // Find layouts to clear logic reuse
        TextInputLayout nL = root.findViewById(R.id.inputContactNameLayoutFound);
        TextInputLayout eL = root.findViewById(R.id.inputContactEmailLayoutFound);
        TextInputLayout pL = root.findViewById(R.id.inputContactPhoneLayoutFound);
        
        clearContactFields(contactNameV, contactEmailV, contactPhoneV, nL, eL, pL);

        selectedImageUrisFound.clear();
        if (photoThumbnailAdapterFound != null) {
            photoThumbnailAdapterFound.notifyDataSetChanged();
        }
        updatePhotosCount();

        Toast.makeText(getContext(), "Found Item posted", Toast.LENGTH_SHORT).show();
    }

    private void showUnifiedDropdown(View anchor, java.util.List<String> items, TextView targetView, Runnable onSelectionChanged) {
        if (getContext() == null || targetView == null) return;

        // Reorder list: Selection at top
        java.util.List<String> orderedItems = new java.util.ArrayList<>(items);
        String currentSelection = targetView.getText() != null ? targetView.getText().toString() : "";
        
        if (!currentSelection.isEmpty() && orderedItems.contains(currentSelection)) {
            orderedItems.remove(currentSelection);
            orderedItems.add(0, currentSelection);
        }

        com.megaproject.connecto.ui.UnifiedDropdownAdapter adapter = 
            new com.megaproject.connecto.ui.UnifiedDropdownAdapter(getContext(), orderedItems);
            
        adapter.setSelectedItem(currentSelection);

        android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(getContext());
        popup.setAnchorView(anchor);
        popup.setAdapter(adapter);
        popup.setWidth(anchor.getWidth() > 0 ? anchor.getWidth() : 500);
        popup.setHeight(android.widget.ListPopupWindow.WRAP_CONTENT);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded));
        popup.setVerticalOffset(16);

        popup.setOnItemClickListener((parent, view, position, id) -> {
            String selected = orderedItems.get(position);
            targetView.setText(selected);
            popup.dismiss();
            
            // Animate chevron if applicable (hacky instance check or passed param would be better, but this suffices for known IDs)
            if (anchor.getId() == R.id.categoryDropdownContainer && dropdownChevron != null) {
                dropdownChevron.animate().rotation(0f).setDuration(150).start();
            } else if (anchor.getId() == R.id.categoryDropdownContainerFound) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevronFound = root.findViewById(R.id.dropdownChevronFound);
                     if(chevronFound != null) chevronFound.animate().rotation(0f).setDuration(150).start();
                 }
            } else if (anchor.getId() == R.id.btnFilterCategory) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.categoryChevron);
                     if(chevron != null) chevron.animate().rotation(0f).setDuration(150).start();
                 }
            } else if (anchor.getId() == R.id.btnFilterStatus) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.statusChevron);
                     if(chevron != null) chevron.animate().rotation(0f).setDuration(150).start();
                 }
            }
             
            if (onSelectionChanged != null) {
                onSelectionChanged.run();
            }
        });

        popup.setOnDismissListener(() -> {
            // Reset chevrons on dismiss
             if (anchor.getId() == R.id.categoryDropdownContainer && dropdownChevron != null) {
                dropdownChevron.animate().rotation(0f).setDuration(150).start();
            } else if (anchor.getId() == R.id.categoryDropdownContainerFound) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevronFound = root.findViewById(R.id.dropdownChevronFound);
                     if(chevronFound != null) chevronFound.animate().rotation(0f).setDuration(150).start();
                 }
            } else if (anchor.getId() == R.id.btnFilterCategory) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.categoryChevron);
                     if(chevron != null) chevron.animate().rotation(0f).setDuration(150).start();
                 }
            } else if (anchor.getId() == R.id.btnFilterStatus) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.statusChevron);
                     if(chevron != null) chevron.animate().rotation(0f).setDuration(150).start();
                 }
            }
        });

        // Rotate chevron when showing
         if (anchor.getId() == R.id.categoryDropdownContainer && dropdownChevron != null) {
            dropdownChevron.animate().rotation(180f).setDuration(150).start();
        } else if (anchor.getId() == R.id.categoryDropdownContainerFound) {
             View root = getView();
             if(root != null) {
                 ImageView chevronFound = root.findViewById(R.id.dropdownChevronFound);
                 if(chevronFound != null) chevronFound.animate().rotation(180f).setDuration(150).start();
             }
        } else if (anchor.getId() == R.id.btnFilterCategory) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.categoryChevron);
                     if(chevron != null) chevron.animate().rotation(180f).setDuration(150).start();
                 }
        } else if (anchor.getId() == R.id.btnFilterStatus) {
                 View root = getView();
                 if(root != null) {
                     ImageView chevron = root.findViewById(R.id.statusChevron);
                     if(chevron != null) chevron.animate().rotation(180f).setDuration(150).start();
                 }
        }

        popup.show();
    }

    private void showCategoryPopup(View v, TextView targetText) {
        java.util.List<String> categories = java.util.Arrays.asList("All Categories", "Electronics", "Accessories", "Documents", "Pet", "Clothing", "Other");
        showUnifiedDropdown(v, categories, targetText, this::filterResults);
    }

    private void showStatusPopup(View v, TextView targetText) {
        java.util.List<String> statuses = java.util.Arrays.asList("All Statuses", "Open", "Found", "Closed");
        showUnifiedDropdown(v, statuses, targetText, this::filterResults);
    }
}


