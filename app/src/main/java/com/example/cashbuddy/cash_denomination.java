package com.example.cashbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class cash_denomination extends Fragment {

    private TextInputEditText etSystemCash;
    private TextView tvTotalAndDiff;
    private LinearLayout llDenominations;
    private ImageButton btnReload,btnShare;
    private Map<Integer, TextInputEditText> denominationInputs = new HashMap<>();
    private int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cash_denomination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSystemCash = view.findViewById(R.id.etSystemCash);
        tvTotalAndDiff = view.findViewById(R.id.tvTotalAndDiff);
        llDenominations = view.findViewById(R.id.llDenominations);
        btnReload = view.findViewById(R.id.btnReload);
        btnShare = view.findViewById(R.id.btnShare);

        // Dynamically add denomination cards
        for (int denom : denominations) {
            View card = LayoutInflater.from(getContext()).inflate(R.layout.item_denomination_card, llDenominations, false);
            TextView tvDenom = card.findViewById(R.id.tvDenomination);
            TextInputEditText etCount = card.findViewById(R.id.etCount);

            tvDenom.setText("₹" + denom);
            llDenominations.addView(card);
            denominationInputs.put(denom, etCount);

            // Scroll to focused input
            etCount.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    llDenominations.post(() -> llDenominations.scrollTo(0, v.getTop()));
                }
            });

            // Live calculation
            etCount.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateTotalAndDifference();
                }
            });
        }

        // System cash input live calculation
        etSystemCash.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalAndDifference();
            }
        });

        // Reload button clears all
        btnReload.setOnClickListener(v -> resetAllInputs());

        sharebtn(btnShare);
    }

    private void calculateTotalAndDifference() {
        int totalCash = 0;

        for (int denom : denominations) {
            TextInputEditText et = denominationInputs.get(denom);
            int count = 0;
            if (et != null && !et.getText().toString().isEmpty()) {
                try { count = Integer.parseInt(et.getText().toString()); }
                catch (NumberFormatException e) { count = 0; }
            }
            totalCash += denom * count;
        }

        int systemCash = 0;
        if (!etSystemCash.getText().toString().isEmpty()) {
            try { systemCash = Integer.parseInt(etSystemCash.getText().toString()); }
            catch (NumberFormatException e) { systemCash = 0; }
        }

        int difference = totalCash - systemCash;
        String text = "Total: ₹" + totalCash + " | Difference: ₹" + difference;
        SpannableString spannable = new SpannableString(text);

        int diffStart = text.indexOf("₹" + difference);
        int diffEnd = diffStart + ("₹" + difference).length();

        int color;
        if (difference == 0) color = getResources().getColor(R.color.green);
        else if (difference < 0) color = getResources().getColor(R.color.red);
        else color = getResources().getColor(R.color.blue);

        spannable.setSpan(new ForegroundColorSpan(color), diffStart, diffEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTotalAndDiff.setText(spannable);
    }

    private void resetAllInputs() {
        etSystemCash.setText("");
        for (TextInputEditText et : denominationInputs.values()) {
            et.setText("");
        }
        tvTotalAndDiff.setText("Total: ₹0 | Difference: ₹0");
        tvTotalAndDiff.setTextColor(getResources().getColor(R.color.textPrimary));
    }

    // ---------------- SimpleTextWatcher helper class ----------------
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void afterTextChanged(Editable s) { }
    }

    private String generateShareText() {
        StringBuilder sb = new StringBuilder();

        // Get current date and time
        String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        sb.append("🕒 *Cash Report*: ").append(currentDateTime).append("\n\n");

        sb.append("💵 *Cash Denomination Summary* 💵\n\n");

        // Header for denominations
        sb.append("Denomination | Count | Total\n");
        sb.append("---------------------------\n");

        int totalCash = 0;
        boolean hasAnyDenom = false;

        for (int denom : denominations) {
            TextInputEditText et = denominationInputs.get(denom);
            int count = 0;
            if (et != null && !et.getText().toString().isEmpty()) {
                try { count = Integer.parseInt(et.getText().toString()); }
                catch (NumberFormatException e) { count = 0; }
            }

            if (count == 0) continue; // Skip zero-count denominations

            hasAnyDenom = true;
            int subTotal = denom * count;
            totalCash += subTotal;

            sb.append("₹").append(denom)
                    .append("       x ").append(count)
                    .append("       = ₹").append(subTotal).append("\n");
        }

        if (!hasAnyDenom) {
            sb.append("No denominations entered.\n");
        }

        // Get system cash
        int systemCash = 0;
        if (!etSystemCash.getText().toString().isEmpty()) {
            try { systemCash = Integer.parseInt(etSystemCash.getText().toString()); }
            catch (NumberFormatException e) { systemCash = 0; }
        }

        // Append Total Cash
        sb.append("\n*Total Cash:* ₹").append(totalCash);

        // Append System Cash and Difference only if System Cash > 0
        if (systemCash > 0) {
            int difference = totalCash - systemCash;
            sb.append("\n*System Cash:* ₹").append(systemCash)
                    .append("\n*Difference:* ₹").append(difference);

            if (difference != 0) {
                sb.append("\n\n⚠️ Difference detected! Please verify.");
            } else {
                sb.append("\n\n✅ Everything matches!");
            }
        } else {
            sb.append("\n\n✅ No system cash entered.");
        }

        return sb.toString();
    }

    private void sharebtn(ImageButton btnShare){

        btnShare.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share_options, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            LinearLayout llShareCash = dialogView.findViewById(R.id.llShareCash);
            LinearLayout llShareApp = dialogView.findViewById(R.id.llShareApp);

            llShareCash.setOnClickListener(view -> {
                String shareText = generateShareText();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(intent, "Share via"));
                dialog.dismiss();
            });

            llShareApp.setOnClickListener(view -> {
                String appLink = "https://github.com/Nikhilk32535/CashBuddy";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Check out this CashBuddy app: " + appLink);
                startActivity(Intent.createChooser(intent, "Share via"));
                dialog.dismiss();
            });

            dialog.show();
        });

    }

}
