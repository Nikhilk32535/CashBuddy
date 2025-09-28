package com.example.cashbuddy;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.HashMap;
import java.util.Map;

public class Stock_register_format extends Fragment {

    private TableLayout tableLayout;
    private int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};
    private Map<Integer, Integer> counts = new HashMap<>();
    private Map<Integer, Integer> bundles = new HashMap<>();
    private int bundleSize = 100; // bundle = 100 notes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stock_register_format, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableLayout = view.findViewById(R.id.tableLayoutStock);
        Button btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        // Handle device back button using Navigation Component
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Navigate back via NavController
                        NavHostFragment.findNavController(Stock_register_format.this).popBackStack();
                    }
                }
        );

        loadData();
        setupTable();
    }

    private void loadData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("CashBuddyPrefs", getContext().MODE_PRIVATE);

        for (int denom : denominations) {
            int loose = prefs.getInt("denom_" + denom, 0); // read as int
            int bundle = loose / bundleSize; // number of bundles
            int remainingLoose = loose % bundleSize;

            counts.put(denom, remainingLoose);
            bundles.put(denom, bundle);
        }

    }

    private void setupTable() {
        tableLayout.removeAllViews();
        tableLayout.setStretchAllColumns(true);

        // Header row
        TableRow header = createRow();
        addCell(header, "Denomination", true);
        addCell(header, "Bundle+Loose", true);
        addCell(header, "Bundle Cash", true);
        addCell(header, "Loose Cash", true);
        addCell(header, "Total", true);
        tableLayout.addView(header);

        int totalBundleCash = 0;
        int totalLooseCash = 0;
        int grandTotal = 0;

        // Data rows
        for (int denom : denominations) {
            TableRow row = createRow();

            int bundleCount = bundles.get(denom);
            int looseCount = counts.get(denom);

            int bundleCash = bundleCount * bundleSize * denom;
            int looseCash = looseCount * denom;
            int total = bundleCash + looseCash;

            totalBundleCash += bundleCash;
            totalLooseCash += looseCash;
            grandTotal += total;

            addCell(row, "₹" + denom, false);
            addCell(row, bundleCount + " + " + looseCount, false);
            addCell(row, "₹" + NumberUtils.formatIndianNumber(bundleCash), false);
            addCell(row, "₹" + NumberUtils.formatIndianNumber(looseCash), false);
            addCell(row, "₹" + NumberUtils.formatIndianNumber(total), false);

            tableLayout.addView(row);
        }

        // Total row
        TableRow totalRow = createRow();
        addCell(totalRow, "Total", true);
        addCell(totalRow, "-", true);
        addCell(totalRow, "₹" + NumberUtils.formatIndianNumber(totalBundleCash), true);
        addCell(totalRow, "₹" + NumberUtils.formatIndianNumber(totalLooseCash), true);
        addCell(totalRow, "₹" + NumberUtils.formatIndianNumber(grandTotal), true);
        tableLayout.addView(totalRow);
    }

    private TableRow createRow() {
        TableRow row = new TableRow(getContext());
        row.setBackgroundColor(Color.WHITE); // row border
        row.setPadding(1, 1, 1, 1);
        return row;
    }

    private void addCell(TableRow row, String text, boolean isHeader) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setPadding(20, 20, 20, 20);
        tv.setTextSize(14);
        tv.setEllipsize(null); // remove truncating
        tv.setSingleLine(false);
        tv.setGravity(Gravity.CENTER);

        // Set background and text color
        if (isHeader) {
            tv.setBackgroundColor(Color.DKGRAY); // dark background for header and total
            tv.setTextColor(Color.WHITE);
        } else {
            tv.setBackgroundColor(Color.WHITE); // light background for normal rows
            tv.setTextColor(Color.BLACK);
        }

        row.addView(tv);
    }

}
