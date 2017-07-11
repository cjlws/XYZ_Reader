package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private View.OnClickListener fabButtonListener;
    private TextView bodyView;
    private ImageView mPhotoView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private int default_toolbar_color;
    private int default_byline_color;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }


    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "FRAGMENT - ON CREATE VIEW");
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        fabButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getResources().getString(R.string.share_text_placeholder))
                        .getIntent(), getString(R.string.action_share)));
            }
        };


        mRootView.findViewById(R.id.share_fab).setOnClickListener(fabButtonListener);

        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            Log.d(TAG, "Support Action Bar Was Found");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            Log.d(TAG, "Support Action Bar Was NOT Found");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Back Nav was clicked");
                getActivity().onBackPressed();
            }
        });

        return mRootView;
    }

    private int generateStatusBarColor(int baseColor) {
        int red = (int) (Color.red(baseColor) * 0.9);
        int blue = (int) (Color.blue(baseColor) * 0.9);
        int green = (int) (Color.green(baseColor) * 0.9);
        return Color.argb(255, red, green, blue);
    }

    private void bindViews() {
        Log.d(TAG, "FRAGMENT - BIND VIEWS");
        if (mRootView == null) {
            return;
        }

        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(LinkMovementMethod.getInstance());

        bodyView = (TextView) mRootView.findViewById(R.id.article_body);

        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);


            mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            String publishedDate = DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);

            bylineView.setText(getResources().getString(R.string.byline_formatted, publishedDate, author));

            String articleText = mCursor.getString(ArticleLoader.Query.BODY);

            AsyncTask<String, Void, Spanned> bodyLoader = new AsyncTask<String, Void, Spanned>() {
                @Override
                protected Spanned doInBackground(String... params) {
                    Log.d(TAG, "Do in background started");
                    Spanned test = fromHtml(params[0]);
                    Log.d(TAG, "Do in background ended");
                    return test;
                }

                @Override
                protected void onPostExecute(Spanned result) {
                    Log.d(TAG, "ON POST EXECUTE START");
                    Spannable spannable = new SpannableString(result);
                    bodyView.setText(spannable, TextView.BufferType.SPANNABLE);
                    Log.d(TAG, "ON POST EXECUTE END");
                }
            }.execute(articleText);


            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(final ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();

                            Log.d(TAG, "FRAGMENT - IMAGELOADER onResponse");

                            if (bitmap != null) {
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());

                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override

                                    public void onGenerated(Palette palette) {
                                        Log.d(TAG, "FRAGMENT - PALETTE ON GENERATED");
                                        // Generate New Colours based on the loaded image

                                        // Gather default colours in case the palette operation fails to read suitable colours from the image
                                        default_byline_color = ResourcesCompat.getColor(getResources(), R.color.drkgray, null);
                                        default_toolbar_color = ResourcesCompat.getColor(getResources(), R.color.theme_primary, null);

                                        int statusBarColor;
                                        int toolbarColor;
                                        int subtitleColor;

                                        // Attempt to generate both vibrant and muted colours
                                        // If both are available then default to vibrant (as it is happier!)
                                        int mutedToolbarColor = palette.getMutedColor(default_toolbar_color);
                                        int vibrantToolbarColor = palette.getVibrantColor(default_toolbar_color);


                                        if (vibrantToolbarColor == default_toolbar_color) {
                                            Log.d(TAG, "No Vibrant Colour Found - Try muted colours");
                                            if (mutedToolbarColor == default_toolbar_color) {
                                                Log.d(TAG, "No muted was found either - use defaults");
                                                statusBarColor = generateStatusBarColor(default_toolbar_color);
                                                toolbarColor = default_toolbar_color;
                                                subtitleColor = default_byline_color;
                                            } else {
                                                Log.d(TAG, "Muted colour was found - " + String.valueOf(mutedToolbarColor));
                                                statusBarColor = generateStatusBarColor(palette.getMutedColor(default_toolbar_color));
                                                toolbarColor = palette.getMutedColor(default_toolbar_color);
                                                subtitleColor = palette.getDarkMutedColor(default_byline_color);
                                            }
                                        } else {
                                            Log.d(TAG, "Vibrant Colour Was Found - " + String.valueOf(vibrantToolbarColor));
                                            statusBarColor = generateStatusBarColor(palette.getVibrantColor(default_toolbar_color));
                                            toolbarColor = palette.getVibrantColor(default_toolbar_color);
                                            subtitleColor = palette.getDarkVibrantColor(default_byline_color);
                                        }

                                        // Set the background colour of the collapsing toolbar and then use a darker version to set the status bar
                                        mCollapsingToolbarLayout.setContentScrimColor(toolbarColor);
                                        mCollapsingToolbarLayout.setStatusBarScrimColor(statusBarColor);

                                        // Colour the background of the subtitle/byline
                                        mRootView.findViewById(R.id.nestedLinearLayout).setBackgroundColor(subtitleColor);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "ON LOAD FINISHED");
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
