package org.weibeld.nytexplore.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.weibeld.nytexplore.R;
import org.weibeld.nytexplore.api.ApiService;
import org.weibeld.nytexplore.model.Doc;
import org.weibeld.nytexplore.model.Multimedium;
import org.weibeld.nytexplore.util.MyDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dw on 24/02/17.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private ArrayList<Doc> mData;
    private Context mContext;

    public ArticleAdapter(ArrayList<Doc> data, Context context) {
        mData = data;
        mContext = context;
    }

    // Called when a new view for an item must be created. This method does not return the view of
    // the item, but a ViewHolder, which holds references to all the elements of the view.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // The view for the item
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        // Create a ViewHolder for this view and return it
        return new ViewHolder(itemView);
    }

    // Populate the elements of the passed view (represented by the ViewHolder) with the data of
    // the item at the specified position.
    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Doc article = mData.get(position);

        vh.tvTitle.setText(getSafeString(article.getHeadline().getMain()));

        if (article.getPubDate() != null) {
            vh.tvDate.setVisibility(View.VISIBLE);
            MyDate date = new MyDate(article.getPubDate());
            vh.tvDate.setText(date.format1());
        } else
            vh.tvDate.setVisibility(View.GONE);

        ArrayList<Multimedium> multimedia = (ArrayList<Multimedium>) article.getMultimedia();
        String thumbUrl = "";
        for (Multimedium m : multimedia) {
            if (m.getType().equals("image") && m.getSubtype().equals("thumbnail")) {
                thumbUrl = ApiService.API_IMAGE_BASE_URL + m.getUrl();
                break;
            }
        }
        Glide.with(mContext).load(thumbUrl).into(vh.ivThumb);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private String getSafeString(String str) {
        if (str == null)
            return "";
        else
            return str;
    }

    public void clear() {
        mData.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    public void append(List<Doc> articles) {
        mData.addAll(articles);
        notifyItemRangeInserted(mData.size() - 1, articles.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvDate;
        TextView tvTitle;

        // Create a viewHolder for the passed view (item view)
        ViewHolder(View view) {
            super(view);
            ivThumb = (ImageView) view.findViewById(R.id.ivThumb);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        }
    }

}
