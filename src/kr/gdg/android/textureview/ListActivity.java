
package kr.gdg.android.textureview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Demo[] demos = {
                new Demo("Camera", CameraActivity.class),
                new Demo("GL Triangle", GLTriangleActivity.class),

                new Demo("Demo repository", null)
        };

        super.onCreate(savedInstanceState);
        final ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<Demo>(this, R.layout.list,
                R.id.activity_name, demos) {

            @Override
            public View getView(int position, View convertView,
                    ViewGroup parent) {
                final LinearLayout view = (LinearLayout) super.getView(
                        position, convertView, parent);
                final Demo demo = getItem(position);
                final TextView activityClass = (TextView) view
                        .findViewById(R.id.activity_class);
                if (demo.classType != null) {
                    activityClass.setText(demo.classType.toString());
                } else {
                    activityClass.setText("");
                }
                return view;
            }
        });
        listView.setClickable(true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final Demo demo = (Demo) parent.getAdapter().getItem(
                        position);
                final Intent intent;
                if (demo.classType != null) {
                    intent = new Intent(ListActivity.this, demo.classType);
                } else {
                    final String url = "https://github.com/dalinaum/textureviewdemo";
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                }
                startActivity(intent);
            }
        });
        setContentView(listView);
    }

    private class Demo {
        public String name;
        public Class<?> classType;

        public Demo(String name, Class<?> classType) {
            this.name = name;
            this.classType = classType;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
