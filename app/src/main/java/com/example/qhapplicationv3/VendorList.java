package com.example.qhapplicationv3;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.List;

//Class lists vendor information and handles click functionality
public class VendorList extends RecyclerView.Adapter<VendorList.VH> {

    //Runs when row is clicked
    public interface OnRowClick { void onClick(JSONObject obj); }
    private final List<JSONObject> items;
    private final OnRowClick onRowClick;

    //Handles list and click functionality
    public VendorList(List<JSONObject> items, OnRowClick onRowClick) {
        this.items = items;
        this.onRowClick = onRowClick;
    }

    //Creates the row layout for the list
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_page, parent, false);
        return new VH(v);
    }

    //Populates rows with vendor information
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        JSONObject o = items.get(position);
        String title = o.optString("[* Trading name]", "-");
        String reg = o.optString("[* Registration number]", "-");
        String lga = o.optString("[LGA Name]", "-");
        String id = String.valueOf(o.opt("id"));
        h.title.setText(title);
        h.subtitle.setText(reg + " • " + lga);
        h.itemView.setOnClickListener(v -> onRowClick.onClick(o));
        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        boolean isPublic = role == null || role.equalsIgnoreCase("PUBLIC");
        boolean isQH = role != null && (
                role.equalsIgnoreCase("QH")
        );
        boolean isCouncil = role != null && role.equalsIgnoreCase("COUNCIL");
        boolean canDelete;
        if (isPublic) {
            canDelete = false;
        } else if (isQH) {
            canDelete = true;
        } else if (isCouncil && !TextUtils.isEmpty(council)) {
            String rec = lga == null ? "" : lga;
            boolean okExact = council.equals(rec);
            boolean okSlug = council.equalsIgnoreCase(slug(rec));
            boolean okDisplay = rec.equalsIgnoreCase(council.replace("-", " "));
            canDelete = okExact || okSlug || okDisplay;
        } else {
            canDelete = false;
        }
        h.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        h.btnDelete.setOnClickListener(v -> {
            if (TextUtils.isEmpty(id) || "null".equalsIgnoreCase(id)) return;
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete vendor")
                    .setMessage("Delete \"" + title + "\"?")
                    .setPositiveButton("Delete", (d, w) -> {
                        VendorDetails.deleteVendor(v.getContext(), id, lga, () -> {
                            int p = h.getAdapterPosition();
                            if (p != RecyclerView.NO_POSITION) {
                                items.remove(p);
                                notifyItemRemoved(p);
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    //Gets the number of rows shown
    @Override
    public int getItemCount() { return items.size(); }

    //Handles row view, preview information and additional buttons
    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Button btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
            btnDelete = itemView.findViewById(R.id.btnRowDelete);
        }
    }

    //Replaces unsafe text with hyphens
    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }
}
