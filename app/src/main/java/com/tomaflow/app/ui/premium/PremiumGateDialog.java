package com.tomaflow.app.ui.premium;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tomaflow.app.R;

public class PremiumGateDialog extends BottomSheetDialogFragment {

    public static PremiumGateDialog newInstance() {
        return new PremiumGateDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_premium_gate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        view.findViewById(R.id.btn_upgrade_now).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PremiumActivity.class));
            dismiss();
        });
        
        view.findViewById(R.id.btn_maybe_later).setOnClickListener(v -> dismiss());
    }
}
