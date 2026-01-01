package com.megaproject.connecto;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UpdateStatusBottomSheet extends BottomSheetDialogFragment {

    private String itemName = "Item";
    private String selectedStatus = "active"; // "Active", "Matched", "Recovered", "Closed" (Using Title Case for display/match usually, but let's stick to consistent casing)
    private OnStatusUpdateListener listener;

    public interface OnStatusUpdateListener {
        void onStatusUpdated(String newStatus);
    }

    public void setListener(OnStatusUpdateListener listener) {
        this.listener = listener;
    }

    // Views
    private LinearLayout optionActive, optionMatched, optionRecovered, optionClosed;
    private ImageView rbActive, rbMatched, rbRecovered, rbClosed;
    private TextView tvUpdateInfo;

    public static UpdateStatusBottomSheet newInstance(String itemName, String currentStatus) {
        UpdateStatusBottomSheet fragment = new UpdateStatusBottomSheet();
        Bundle args = new Bundle();
        args.putString("item_name", itemName);
        args.putString("current_status", currentStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemName = getArguments().getString("item_name", "Item");
            selectedStatus = getArguments().getString("current_status", "Active");
        }
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_update_status, container, false);
        
        tvUpdateInfo = view.findViewById(R.id.tvUpdateInfo);
        tvUpdateInfo.setText("Updating: " + itemName);

        optionActive = view.findViewById(R.id.optionActive);
        optionMatched = view.findViewById(R.id.optionMatched);
        optionRecovered = view.findViewById(R.id.optionRecovered);
        optionClosed = view.findViewById(R.id.optionClosed);

        rbActive = view.findViewById(R.id.rbActive);
        rbMatched = view.findViewById(R.id.rbMatched);
        rbRecovered = view.findViewById(R.id.rbRecovered);
        rbClosed = view.findViewById(R.id.rbClosed);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnUpdateStatus).setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusUpdated(selectedStatus);
            }
            dismiss();
        });

        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.optionActive) updateSelection("Active");
            else if (id == R.id.optionMatched) updateSelection("Matched");
            else if (id == R.id.optionRecovered) updateSelection("Recovered");
            else if (id == R.id.optionClosed) updateSelection("Closed");
        };

        optionActive.setOnClickListener(listener);
        optionMatched.setOnClickListener(listener);
        optionRecovered.setOnClickListener(listener);
        optionClosed.setOnClickListener(listener);

        // Initialize selection 
        updateSelection(selectedStatus);

        return view;
    }

    private void updateSelection(String status) {
        selectedStatus = status;
        
        resetOption(optionActive, rbActive);
        resetOption(optionMatched, rbMatched);
        resetOption(optionRecovered, rbRecovered);
        resetOption(optionClosed, rbClosed);

        if (status == null) status = "Active";

        switch (status) { // Case insensitive check might be safer or normalize strings
            case "Active": 
            case "active":
                selectOption(optionActive, rbActive); break;
            case "Matched": 
            case "matched":
                selectOption(optionMatched, rbMatched); break;
            case "Recovered": 
            case "recovered":
                selectOption(optionRecovered, rbRecovered); break;
            case "Closed": 
            case "closed":
                selectOption(optionClosed, rbClosed); break;
            default:
                selectOption(optionActive, rbActive); break;
        }
    }

    private void resetOption(View container, ImageView rb) {
        container.setBackgroundResource(R.drawable.bg_status_option_default);
        rb.setImageResource(R.drawable.ic_radio_unchecked);
        rb.setImageTintList(ColorStateList.valueOf(Color.parseColor("#DBE0E6")));
    }

    private void selectOption(View container, ImageView rb) {
        container.setBackgroundResource(R.drawable.bg_status_option_selected);
        rb.setImageResource(R.drawable.ic_radio_checked_blue);
        rb.setImageTintList(null); // Clear tint to show blue
    }
}
