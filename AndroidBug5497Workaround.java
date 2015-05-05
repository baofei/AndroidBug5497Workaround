public class AndroidBug5497Workaround implements ViewTreeObserver.OnGlobalLayoutListener {

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static AndroidBug5497Workaround assistActivity(Activity activity) {
        return new AndroidBug5497Workaround(activity);
    }

    private final View mChildOfContent;

    private int usableHeightPrevious;

    private int hideusableHeight;

    private OnKeyboardListener listener;

    public interface OnKeyboardListener {
        public void onKeyboardVisible(int Keyboardheight, int layoutHeight);

        public void onKeyboardHidden();
    }

    //private final FrameLayout.LayoutParams frameLayoutParams;

    public OnKeyboardListener getKeyboardListener() {
        return listener;
    }

    public void setKeyboardListener(OnKeyboardListener listener) {
        this.listener = listener;
    }

    public void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mChildOfContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        if (listener != null) {
            listener.onKeyboardHidden();
        }
    }

    private AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this);
        //frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    @Override
    public void onGlobalLayout() {
        possiblyResizeChildOfContent();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4) && hideusableHeight > 0) {
                // keyboard probably just became visible
                //frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                if (listener != null) {
                    listener.onKeyboardVisible(hideusableHeight - usableHeightNow,
                            usableHeightSansKeyboard - heightDifference);
                }
            } else {
                // keyboard probably just became hidden
                //frameLayoutParams.height = usableHeightSansKeyboard;
                hideusableHeight = mChildOfContent.getHeight();
                if (listener != null) {
                    listener.onKeyboardHidden();
                }
            }
            // mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

}
