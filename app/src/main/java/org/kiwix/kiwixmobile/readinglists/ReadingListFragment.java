package org.kiwix.kiwixmobile.readinglists;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;

import org.kiwix.kiwixmobile.KiwixMobileActivity;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.database.ReadingListFolderDao;
import org.kiwix.kiwixmobile.readinglists.entities.BookmarkArticle;
import org.kiwix.kiwixmobile.readinglists.entities.ReadinglistFolder;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListArticleItem;

import java.util.ArrayList;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadingListFragment extends Fragment implements FastAdapter.OnClickListener<ReadingListArticleItem> {


    private FastAdapter<ReadingListArticleItem> fastAdapter;
    private ItemAdapter<ReadingListArticleItem> itemAdapter;
    private final String FRAGMENT_ARGS_FOLDER_TITLE = "requested_folder_title";
    private ReadingListFolderDao readinglistFoldersDao;
    private ArrayList<BookmarkArticle> articles;
    private ActionModeHelper mActionModeHelper;
    private RecyclerView readinglistRecyclerview;


    public ReadingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reading_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readinglistRecyclerview = (RecyclerView) view.findViewById(R.id.readinglist_articles_list);
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();
        mActionModeHelper = new ActionModeHelper(fastAdapter, R.menu.menu_bookmarks, new ActionBarCallBack());

        fastAdapter.withOnClickListener(this);
        fastAdapter.withSelectOnLongClick(false);
        fastAdapter.withSelectable(false);
        readinglistRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        readinglistRecyclerview.setAdapter(itemAdapter.wrap(fastAdapter));

        // shoud be injected in presenter when moving to mvp
        readinglistFoldersDao = new ReadingListFolderDao(KiwixDatabase.getInstance(getActivity()));
        loadArticlesOfFolder();

    }


    void loadArticlesOfFolder() {
        String requestedFolderTitle = this.getArguments().getString(FRAGMENT_ARGS_FOLDER_TITLE);
        articles = readinglistFoldersDao.getArticlesOfFolder(new ReadinglistFolder(requestedFolderTitle));
        for (BookmarkArticle article: articles) {
            itemAdapter.add(new ReadingListArticleItem(article.getBookmarkTitle()));
        }
    }


    @Override
    public boolean onClick(View v, IAdapter<ReadingListArticleItem> adapter, ReadingListArticleItem item, int position) {

        Intent intent = new Intent(getActivity(), KiwixMobileActivity.class);
        if (!item.getArticle_url().equals("null")) {
            intent.putExtra("choseXURL", item.getArticle_url());
        } else {
            intent.putExtra("choseXTitle", item.getTitle());
        }
        intent.putExtra("bookmarkClicked", true);
        getActivity().finish();
        return true;
    }


    /**
     * Our ActionBarCallBack to showcase the CAB
     */
    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_bookmarks_delete:
                    deleteSelectedItems();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }


    private void deleteSelectedItems() {
        Set<ReadingListArticleItem> selectedItems = fastAdapter.getSelectedItems();
        readinglistFoldersDao.deleteArticles(selectedItems);
        loadArticlesOfFolder();
    }



}