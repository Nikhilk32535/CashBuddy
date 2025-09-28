package com.example.cashbuddy;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class cash_denomination extends Fragment {

    private TextInputEditText etSystemCash;
    private TextView tvTotalAndDiff;
    private LinearLayout llDenominations;
    private ImageButton btnReload, btnShare;
    private Map<Integer, TextInputEditText> denominationInputs = new HashMap<>();
    private int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};
    private SharedPreferences prefs;
    private boolean isLoadingData = false; // flag to prevent saving while loading
    MaterialButton btnGoStockregister;

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
        btnGoStockregister=view.findViewById(R.id.btnGostockregister);

        btnGoStockregister.setOnClickListener(v -> {
            Stock_register_format fragment = new Stock_register_format();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });




        prefs = requireContext().getSharedPreferences("CashBuddyPrefs", getContext().MODE_PRIVATE);

        // Dynamically add denomination cards
        for (int denom : denominations) {
            View card = LayoutInflater.from(getContext()).inflate(R.layout.item_denomination_card, llDenominations, false);
            TextView tvDenom = card.findViewById(R.id.tvDenomination);
            TextInputEditText etCount = card.findViewById(R.id.etCount);
            TextView tvSubTotal = card.findViewById(R.id.tvSubTotal);

            tvDenom.setText("₹" + denom);
            llDenominations.addView(card);
            denominationInputs.put(denom, etCount);

            // Scroll to focused input
            etCount.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) llDenominations.post(() -> llDenominations.scrollTo(0, v.getTop()));
            });


            etCount.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int enteredCount = 0;
                    try {
                        enteredCount = Integer.parseInt(s.toString());
                    } catch (NumberFormatException ignored) {}

                    int subTotal = denom * enteredCount;
                    tvSubTotal.setText("= ₹" + NumberUtils.formatIndianNumber(subTotal));

                    calculateTotalAndDifference();
                }
            });


            // TextWatcher for live calculation & saving
            etCount.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateTotalAndDifference();
                    saveData();
                }
            });
        }


        // System cash live calculation & save
        etSystemCash.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalAndDifference();
                saveData();
            }
        });

        // Load saved data after all inputs are initialized
        loadData();

        // Reload button clears all
        btnReload.setOnClickListener(v -> resetAllInputs());

        // Share button
        setupShareButton(btnShare);
    }

    // ---------------- Calculate total and difference ----------------
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
        String text = "Total: ₹" + NumberUtils.formatIndianNumber(totalCash) + " | Difference: ₹" + difference;
        SpannableString spannable = new SpannableString(text);

        int diffStart = text.indexOf("₹" + difference);
        int diffEnd = diffStart + ("₹" + difference).length();

        int color;
        if (difference > 0) color = getResources().getColor(R.color.green);
        else if (difference < 0) color = getResources().getColor(R.color.red);
        else color = getResources().getColor(R.color.blue);

        spannable.setSpan(new ForegroundColorSpan(color), diffStart, diffEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTotalAndDiff.setText(spannable);
    }

    // ---------------- Reset inputs and clear saved data ----------------
    private void resetAllInputs() {
        etSystemCash.setText("");
        for (TextInputEditText et : denominationInputs.values()) et.setText("");
        tvTotalAndDiff.setText("Total: ₹0 | Difference: ₹0");
        tvTotalAndDiff.setTextColor(getResources().getColor(R.color.textPrimary));
        prefs.edit().clear().apply();
    }

    // ---------------- SimpleTextWatcher helper class ----------------
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void afterTextChanged(Editable s) { }
    }

    // ---------------- Save and load data using SharedPreferences ----------------
    private void saveData() {
        if (isLoadingData) return; // do not save while loading

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("systemCash", etSystemCash.getText().toString());

        for (int denom : denominations) {
            TextInputEditText et = denominationInputs.get(denom);
            String count = (et != null) ? et.getText().toString() : "0";
            editor.putString("denom_" + denom, count);
        }

        editor.apply();
    }

    private void loadData() {
        isLoadingData = true; // prevent overwriting while loading

        etSystemCash.setText(prefs.getString("systemCash", ""));
        for (int denom : denominations) {
            TextInputEditText et = denominationInputs.get(denom);
            if (et != null) et.setText(prefs.getString("denom_" + denom, ""));
        }

        calculateTotalAndDifference();
        isLoadingData = false;
    }

    // ---------------- Generate Share Text ----------------
    private String generateShareText() {
        StringBuilder sb = new StringBuilder();

        String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        sb.append("🕒 *Cash Report*: ").append(currentDateTime).append("\n\n");
        sb.append("💵 *Cash Denomination Summary* 💵\n\n");
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
            if (count == 0) continue;
            hasAnyDenom = true;
            int subTotal = denom * count;
            totalCash += subTotal;

            sb.append("₹").append(denom)
                    .append("       x ").append(count)
                    .append("       = ₹").append(subTotal).append("\n");
        }

        if (!hasAnyDenom) sb.append("No denominations entered.\n");

        int systemCash = 0;
        if (!etSystemCash.getText().toString().isEmpty()) {
            try { systemCash = Integer.parseInt(etSystemCash.getText().toString()); }
            catch (NumberFormatException e) { systemCash = 0; }
        }

        sb.append("\n*Total Cash:* ₹").append(totalCash);

        if (systemCash > 0) {
            int difference = totalCash - systemCash;
            sb.append("\n*System Cash:* ₹").append(systemCash)
                    .append("\n*Difference:* ₹").append(difference);

            if (difference != 0) sb.append("\n\n⚠️ Difference detected! Please verify.");
            else sb.append("\n\n✅ Everything matches!");
        } else {
            sb.append("\n\n✅ No system cash entered.");
        }

        return sb.toString();
    }

    // ---------------- Setup Share Button ----------------
    private void setupShareButton(ImageButton btnShare) {
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
