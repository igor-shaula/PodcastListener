package igor.shaula.podcast_listener.rv_listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import igor.shaula.podcast_listener.utils.L;

/**
 * this class lets us react on clicking RecyclerView items \
 * <p/>
 * it is made abstract to simply avoid creation of inner interface for connection to activity \
 */
public abstract class RVOnItemTouchListener implements RecyclerView.OnItemTouchListener {

    private static final String CN = "RVOnItemTouchListener ` ";

    // for avoiding creation of this object for every touch event I took its declaration here \
    private GestureDetector gestureDetector;

    // complex constructor - I decided to initialize working object here at once and save fields \
    public RVOnItemTouchListener(Context context) {
        // preparing the gesture detector for onItemTouchListener \
        GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
            // for now we need only simple clicks \
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        };
        gestureDetector = new GestureDetector(context, onGestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {

        View child = rv.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

        if (child != null && gestureDetector.onTouchEvent(motionEvent)) {
//            int childAdapterPosition = rv.getChildAdapterPosition(child);
            int childLayoutPosition = rv.getChildLayoutPosition(child);
            L.l(CN + "childLayoutPosition = " + childLayoutPosition);

            // i decided avoid using EventBus and interface-based way for such method triggering \
            onListItemTouch(childLayoutPosition);

            return true;
        }
        return false;
    }

    // this method acts as a pipe, in which we pass interesting for us info \
    public abstract void onListItemTouch(int whichIndex);

    // unused \
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        L.l(CN + "onTouchEvent");
    }

    // unused \
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        L.l(CN + "onRequestDisallowInterceptTouchEvent = " + disallowIntercept);
    }
}