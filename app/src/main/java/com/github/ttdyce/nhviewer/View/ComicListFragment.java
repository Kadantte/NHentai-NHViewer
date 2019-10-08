package com.github.ttdyce.nhviewer.View;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.ttdyce.nhviewer.Presenter.ComicListPresenter;
import com.github.ttdyce.nhviewer.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class ComicListFragment extends Fragment implements ComicListPresenter.ComicListView {
    private static final String ARG_COLLECTION_NAME = "collectionName";
    private static final String ARG_QUERY = "query";

    private String collectionName;
    private String query;

    private RecyclerView rvComicList;
    private ComicListPresenter presenter;
    private ContentLoadingProgressBar pbComicList;
    private TextView tvComicListDesc;

    public ComicListFragment() {
        // Required empty public constructor
    }

    public static ComicListFragment newInstance(String collectionName, String query) {
        ComicListFragment fragment = new ComicListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLECTION_NAME, collectionName);
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comic_list, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() == null)
            return;

        collectionName = getArguments().getString(ARG_COLLECTION_NAME);
        query = getArguments().getString(ARG_QUERY);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new ComicListPresenter(this, collectionName, query);
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 3);
        rvComicList = view.findViewById(R.id.rvComicList);
        pbComicList = view.findViewById(R.id.pbComicList);
        tvComicListDesc = view.findViewById(R.id.tvComicListDesc);

        tvComicListDesc.setText("Loading from " + collectionName + "...");

        rvComicList.setHasFixedSize(true);
        rvComicList.setAdapter(presenter.getAdapter());
        rvComicList.setLayoutManager(layoutManager);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!presenter.inSelectionMode())
            inflater.inflate(R.menu.app_bar_items, menu);
        else
            inflater.inflate(R.menu.app_bar_selection_mode, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(presenter.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);

    }

    @Override
    public ComicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comic_list, parent, false);
        return new ComicListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ComicListViewHolder holder, int position, String title, String thumbUrl, int numOfPages) {
        holder.tvTitle.setText(title);
        holder.tvNumOfPages.setText(String.format(Locale.ENGLISH, "%dp", numOfPages));

        Glide.with(requireContext())
                .load(thumbUrl)
                .placeholder(new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorSecondary)))
                .into(holder.ivThumb);

    }

    @Override
    public void updateList(Boolean isLoading) {
        RecyclerView.Adapter adapter = rvComicList.getAdapter();
        adapter.notifyDataSetChanged();

        toggleLoadingDesc(isLoading);
    }

    private void toggleLoadingDesc(boolean loading) {
        if (loading) {
            pbComicList.show();
            tvComicListDesc.setVisibility(View.VISIBLE);
        } else {
            pbComicList.hide();
            tvComicListDesc.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public FragmentActivity getRequiredActivity() {
        return requireActivity();
    }

    @Override
    public void showAdded(boolean isAdded, String collectionName) {
        View root = requireActivity().findViewById(R.id.rootMain);
        if (isAdded)
            Snackbar.make(root, Html.fromHtml(String.format(Locale.ENGLISH, "Comic is added to <font color=\"yellow\">%s</font>", collectionName)), Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(root, Html.fromHtml(String.format(Locale.ENGLISH, "Comic is already exist in <font color=\"red\">%s</font>", collectionName)), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleted(Boolean isDone, String title, String collectionName) {
        View root = requireActivity().findViewById(R.id.rootMain);

        if (isDone)
            Snackbar.make(root, Html.fromHtml(String.format(Locale.ENGLISH, "Deleted from <font color=\"yellow\">%s</font>, named %s", collectionName, title)), Snackbar.LENGTH_LONG).show();
        else
            Toast.makeText(requireContext(), String.format(Locale.ENGLISH, "Error when deleting comic named %s", title), Toast.LENGTH_SHORT).show();

    }

}