package igor.shaula.podcast_listener.rv_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import igor.shaula.podcast_listener.R;
import igor.shaula.podcast_listener.entity.InfoEntity;
import igor.shaula.podcast_listener.utils.L;

/**
 * adapts incoming data set to be shown into RecyclerView \
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private static final String CN = "RVAdapter ` ";
    private static final String PDT = "PDT";

    private InfoEntity[] infoEntityArray;

    // created only once in constructor and kept over every onCreateViewHolder call \
    private LayoutInflater layoutInflater;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private TextView tvPubDate;
        private TextView tvSummary;

        public ViewHolder(View rootView) {
            super(rootView);

            tvTitle = (TextView) rootView.findViewById(R.id.tvTitle);
            tvPubDate = (TextView) rootView.findViewById(R.id.tvPubDate);
            tvSummary = (TextView) rootView.findViewById(R.id.tvSummary);
        }
    }

    public RVAdapter(Context context, InfoEntity[] infoEntityArray) {

        this.infoEntityArray = infoEntityArray;
        // as retrieving LayoutInflater is hardy job - it has to be done only once and better here \
        layoutInflater = LayoutInflater.from(context);
        // just for keeping link to context only inside constructor - but not globally \
    }

    @Override
    public int getItemCount() {
        return infoEntityArray != null ? infoEntityArray.length : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        L.l(CN + "onCreateViewHolder");

        return new ViewHolder(layoutInflater.inflate(R.layout.item_for_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        L.l(CN + "onBindViewHolder for position " + position); // too much output

        holder.tvTitle.setText(String.valueOf(infoEntityArray[position].getTitle()));

        // as i noticed repeating text "PDT" - i decided not to show it to the user \
        String reducedPubDate = String.valueOf(infoEntityArray[position].getPubDate());
        int pdtIndex = reducedPubDate.indexOf(PDT);
        if (pdtIndex > 0)
            reducedPubDate = reducedPubDate.substring(0, pdtIndex).trim();
        holder.tvPubDate.setText(reducedPubDate);

        holder.tvSummary.setText(String.valueOf(infoEntityArray[position].getSummary()));

    } // end of onBindViewHolder-method \\
}