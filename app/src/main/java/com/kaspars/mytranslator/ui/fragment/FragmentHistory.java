package com.kaspars.mytranslator.ui.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaspars.mytranslator.R;
import com.kaspars.mytranslator.data.History;
import com.kaspars.mytranslator.data.HistoryItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class FragmentHistory extends Fragment {


    private Menu menu;
    @Bind(R.id.history_list)
    protected LinearLayout mHistoryLayout;

    @Bind(R.id.history_card)
    protected CardView mHistoryContainerCard;

    private OnHistoryItemClickedListener mListener;
    private Subscription mSubscription;
    private String fragmentName;
    private String actualFragmentName;
    private boolean isMenuItemHidenHistory;
    private boolean isMenuItemHidenFavorites;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() instanceof OnHistoryItemClickedListener) {
            mListener = (OnHistoryItemClickedListener) getActivity();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        fragmentName = getArguments().getString("add");
        if (fragmentName.equals("history")) {History.whereStatement = "is_added_to_favorites = 1 OR is_added_to_favorites = 0";
            History.orderByStatement = "date_history DESC";
        }
        if (fragmentName.equals("favorites")) {History.whereStatement =
                "is_added_to_favorites = 1 OR is_added_to_favorites = 2";
            History.orderByStatement = "date_favorites DESC";

        }

        mSubscription = History.getLastItems(getActivity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setHistory);

        return view;
    }

    private void setHistory(List<HistoryItem> historyItems) {
        mHistoryLayout.removeAllViews();

        setVisiblilityCardContainer(!historyItems.isEmpty());

        if (menu != null) {
        if (historyItems.isEmpty() && (fragmentName.equals(actualFragmentName))) {
            hideMenuItem(R.id.delete, true);}}

        if (fragmentName.equals("history") && (historyItems.isEmpty()) ) isMenuItemHidenHistory = true;
        if (fragmentName.equals("history") && (!historyItems.isEmpty()) ) isMenuItemHidenHistory = false;


        if (fragmentName.equals("favorites") && (historyItems.isEmpty()) ) isMenuItemHidenFavorites = true;
        if (fragmentName.equals("favorites") && (!historyItems.isEmpty()) ) isMenuItemHidenFavorites = false;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (int i = 0; i < historyItems.size(); i++) {
            HistoryItem item = historyItems.get(i);

            View itemView = inflater.inflate(R.layout.item_history, mHistoryLayout, false);
            TextView originalText = (TextView) itemView.findViewById(R.id.original);
            TextView direction = (TextView) itemView.findViewById(R.id.direction);
            TextView translateText = (TextView) itemView.findViewById(R.id.translate);
            LinearLayout historyItem = (LinearLayout) itemView.findViewById(R.id.history_item);
            ImageButton favoritesButton = (ImageButton) itemView.findViewById(R.id.item_history_add_to_favorites);
            favoritesButton.setImageResource((item.getIsAddedToFavorites() == 1 || item.getIsAddedToFavorites() == 2) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
            originalText.setText(item.getOriginal());
            translateText.setText(item.getTranslate());
            direction.setText(item.getLang());
            mHistoryLayout.addView(itemView);

            if (i != historyItems.size() - 1) {
                View separator = inflater.inflate(R.layout.item_separator, mHistoryLayout, false);
                mHistoryLayout.addView(separator);            }

            historyItem.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onHistoryItemClicked(item);
                }
            });

            historyItem.setOnLongClickListener(view -> {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete?")
                        .setPositiveButton("Delete",
                (interfaceDialog, which) -> {

                    if (item.getIsAddedToFavorites() == 0 || item.getIsAddedToFavorites() == 2) {
                    new Thread(() -> History.deleteObject(getContext(), new HistoryItem(item.getOriginal(),
                            item.getTranslate(), item.getLang(), item.getIsAddedToFavorites()))).start();}

                    if (item.getIsAddedToFavorites() == 1 && fragmentName.equals("favorites")) {
                        String id = item.getLang()+item.getOriginal();
                        new Thread(() -> History.updateItems(getContext(),
                                new HistoryItem(0),"is_added_to_favorites = 1 AND id = " + "'"+id+"'")).start();}

                    if (item.getIsAddedToFavorites() == 1 && fragmentName.equals("history")) {
                         String id = item.getLang()+item.getOriginal();
                         new Thread(() -> History.updateItems(getContext(),
                                 new HistoryItem(2),"is_added_to_favorites = 1 AND id = " + "'"+id+"'")).start();}

                    actualFragmentName = fragmentName;

                })
                        .setCancelable(false)
                        .setNeutralButton("Cancel", null)
                        .create();
                dialog.show();

                return true;
            });

            favoritesButton.setOnClickListener(view -> {
                actualFragmentName = fragmentName;
                String id = item.getLang() + item.getOriginal();

                if (item.getIsAddedToFavorites() == 0) {
                    new Thread(() -> History.updateItems(getContext(),
                            new HistoryItem(1), "is_added_to_favorites = " + item.getIsAddedToFavorites() + " AND id = " + "'"+id+"'")).start();}

                if (item.getIsAddedToFavorites() == 1) {
                    new Thread(() -> History.updateItems(getContext(),
                            new HistoryItem(0), "is_added_to_favorites = " + item.getIsAddedToFavorites() + " AND id = " + "'" + id + "'")).start();}

                if (item.getIsAddedToFavorites() == 2) {
                    new Thread(() -> History.deleteObject(getContext(), new HistoryItem(item.getOriginal(),
                            item.getLang(), 2))).start();
                }
            });
        }
    }

    private void setVisiblilityCardContainer(boolean visibility) {
        mHistoryContainerCard.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mSubscription.unsubscribe();
    }

    public interface OnHistoryItemClickedListener {
        void onHistoryItemClicked(HistoryItem item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_history, menu);

        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;

        if (fragmentName.equals("history")) {hideMenuItem(R.id.delete, isMenuItemHidenHistory);}
        if (fragmentName.equals("favorites")) {hideMenuItem(R.id.delete, isMenuItemHidenFavorites);}
    }

    private void hideMenuItem(int id, boolean isMenuItemHiden) {
        menu.findItem(id).setVisible(!isMenuItemHiden);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.delete)
        {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete all items?")
                    .setPositiveButton("Delete",
                            (interfaceDialog, which) -> {
                                actualFragmentName = fragmentName;
                                boolean isItemListEmpty = History.getItems(getContext(), "is_added_to_favorites = 1").isEmpty();
                                if (fragmentName.equals("history")) {

                                   History.deleteItems(getContext(), "is_added_to_favorites = 0");

                                    if (!isItemListEmpty)
                                    History.updateItems(getContext(), new HistoryItem(2), "is_added_to_favorites = 1");

                                    hideMenuItem(R.id.delete, true);
                                }else {
                                    History.deleteItems(getContext(), "is_added_to_favorites = 2");
                                    if (!isItemListEmpty)
                                    History.updateItems(getContext(), new HistoryItem(0), "is_added_to_favorites = 1");
                                    hideMenuItem(R.id.delete, true);

                                }
                            })
                    .setCancelable(false)
                    .setNeutralButton("Cancel", null)
                    .create();

            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
