package com.crypta.gui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.crypta.R;
import com.crypta.util.FileThumbnailRequestHandler;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for file list
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MetadataViewHolder> {
    private final Picasso mPicasso;
    private final Callback mCallback;
    private List<Metadata> mFiles;

    public FilesAdapter(Picasso picasso, Callback callback) {
        mPicasso = picasso;
        mCallback = callback;
    }

    public void setFiles(List<Metadata> files) {
        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        notifyDataSetChanged();
    }

    public List<Metadata> getFiles() {
        return mFiles;
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.files_item, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).getPathLower().hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public interface Callback {
        void onFolderClicked(FolderMetadata folder);

        void onFileClicked(FileMetadata file);
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private final ImageView actionImageView;
        private Metadata mItem;
        private BottomSheetBehavior mBottomSheetBehavior;

        public MetadataViewHolder(final View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.image);
            actionImageView = (ImageView) itemView.findViewById(R.id.arrow);
            mTextView = (TextView)itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
            actionImageView.setImageResource(R.drawable.ic_arrow_drop_down_circle_24dp);
            actionImageView.setColorFilter(Color.parseColor("#b4b1b1"));
        }

        @Override
        public void onClick(View v) {

            if (mItem instanceof FolderMetadata) {
                mCallback.onFolderClicked((FolderMetadata) mItem);
            }  else if (mItem instanceof FileMetadata) {
                mCallback.onFileClicked((FileMetadata)mItem);
            }
        }

        public void bind(Metadata item) {
            mItem = item;
            mTextView.setText(mItem.getName());

            // Load based on file path
            // Prepending a magic scheme to get it to
            // be picked up by DropboxPicassoRequestHandler

            if (item instanceof FileMetadata) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = item.getName().substring(item.getName().lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);

                if (ext != null && ext.length() > 0) {
                    if (type != null && type.startsWith("image/")) {
                        mPicasso.load(FileThumbnailRequestHandler.buildPicassoUri((FileMetadata) item))
                                .placeholder(R.drawable.page_white48)
                                .error(R.drawable.page_white48)
                                .into(mImageView);
                    } else if (type != null && type.startsWith("audio/")) {
                        mPicasso.load(R.drawable.music48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("pdf")) {
                        mPicasso.load(R.drawable.page_white_acrobat48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("doc") || ext.equals("docx")) {
                        mPicasso.load(R.drawable.word48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("txt")) {
                        mPicasso.load(R.drawable.page_white_text48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("ppt") || ext.equals("pptx")) {
                        mPicasso.load(R.drawable.powerpoint48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("xls") || ext.equals("xlsx")) {
                        mPicasso.load(R.drawable.excel48)
                                .noFade()
                                .into(mImageView);
                    } else if (ext.equals("zip") || ext.equals("tap") || ext.equals("rar")) {
                        mPicasso.load(R.drawable.page_white_zip48)
                                .noFade()
                                .into(mImageView);
                    } else {

                        mPicasso.load(R.drawable.page_white48)
                                .noFade()
                                .into(mImageView);

                    }
                }

               /* if (type != null && type.startsWith("image/")) {
                    mPicasso.load(FileThumbnailRequestHandler.buildPicassoUri((FileMetadata) item))
                            .placeholder(R.drawable.ic_photo_grey_600_36dp)
                            .error(R.drawable.ic_photo_grey_600_36dp)
                            .into(mImageView);
                } else {
                    mPicasso.load(R.drawable.ic_insert_drive_file_blue_36dp)
                            .noFade()
                            .into(mImageView);
                }*/
            } else if (item instanceof FolderMetadata) {
                if (((FolderMetadata) item).getSharingInfo() != null) {
                    mPicasso.load(R.drawable.folder_user48)
                            .noFade()
                            .into(mImageView);
                } else {
                    mPicasso.load(R.drawable.folder48)
                            .noFade()
                            .into(mImageView);
                }
            }
        }
    }
}
