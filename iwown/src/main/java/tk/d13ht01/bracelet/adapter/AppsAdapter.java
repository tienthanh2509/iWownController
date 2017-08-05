package tk.d13ht01.bracelet.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tk.d13ht01.bracelet.R;
import tk.d13ht01.bracelet.common.Constants;
import tk.d13ht01.bracelet.model.AppNotification;

/**
 * Created by Aloyan Dmitry on 16.09.2015
 */
public class AppsAdapter extends ArrayAdapter<ApplicationInfo> {
    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public AppsAdapter(Context context, int textViewResourceId, List<ApplicationInfo> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;

        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.applist_item, null);
        }

        ApplicationInfo data = appsList.get(position);
        if (null != data) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String packageName = (String) buttonView.getTag();

                    if (isChecked) {
                        AppNotification.disableApp(packageName);
                    } else {
                        AppNotification.enableApp(packageName, Constants.ALERT_TYPE_MESSAGE);
                    }
                }
            });

            ((TextView) view.findViewById(R.id.title)).setText(data.loadLabel(packageManager));
            ((TextView) view.findViewById(R.id.description)).setText(data.packageName);
            ((ImageView) view.findViewById(R.id.icon))
                    .setImageDrawable(data.loadIcon(packageManager));

            view.setTag(position);
        }
        return view;
    }


}
