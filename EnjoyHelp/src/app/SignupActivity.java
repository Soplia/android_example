package app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.enjoyhelp.R;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class SignupActivity extends Activity implements OnWheelChangedListener  
{  
    /** 
     * ��ȫ����ʡ��������Ϣ��json�ĸ�ʽ���棬������ɺ�ֵΪnull 
     */  
    private JSONObject mJsonObj;  
    /** 
     * ʡ��WheelView�ؼ� 
     */  
    private WheelView mProvince;  
    /** 
     * �е�WheelView�ؼ� 
     */  
    private WheelView mCity;  
    /** 
     * ����WheelView�ؼ� 
     */  
    private WheelView mArea;  
  
    /** 
     * ����ʡ 
     */  
    private String[] mProvinceDatas;  
    /** 
     * key - ʡ value - ��s 
     */  
    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();  
    /** 
     * key - �� values - ��s 
     */  
    private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();  
    /** 
     * ��ǰʡ������ 
     */  
    private String mCurrentProviceName;  
    /** 
     * ��ǰ�е����� 
     */  
    private String mCurrentCityName;  
    /** 
     * ��ǰ�������� 
     */  
    private String mCurrentAreaName ="";  
    private ImageView user_pic;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;
    private ImageView back;
    @Override  
    protected void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.wheel_local);  
        //Toast.makeText(this, "test into singnup", 1).show();
        initJsonData();  
        mProvince = (WheelView) findViewById(R.id.id_province);  
        mCity = (WheelView) findViewById(R.id.id_city);  
        mArea = (WheelView) findViewById(R.id.id_area);  
        user_pic = (ImageView) findViewById(R.id.user_pic);
        user_pic.setOnClickListener(user_pic_BtnListener);
        back=(ImageView)findViewById(R.id.singup_back); 
        back.setOnClickListener(back_BtnListener);
        initDatas();  
        mProvince.setViewAdapter(new ArrayWheelAdapter<String>(this, mProvinceDatas));  
        // ���change�¼�  
        mProvince.addChangingListener(this);  
        // ���change�¼�  
        mCity.addChangingListener(this);  
        // ���change�¼�  
        mArea.addChangingListener(this);  
        mProvince.setVisibleItems(5);  
        mCity.setVisibleItems(5);  
        mArea.setVisibleItems(5);  
        updateCities();  
        updateAreas();  
    }  
    private View.OnClickListener back_BtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
    };
    
    private View.OnClickListener user_pic_BtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showChoosePicDialog();
		}
    };
    /**
     * ��ʾ�޸�ͷ��ĶԻ���
     */
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("����ͷ��");
        String[] items = { "ѡ�񱾵���Ƭ", "����" };
        builder.setNegativeButton("ȡ��", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case CHOOSE_PICTURE: // ѡ�񱾵���Ƭ
                    Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    openAlbumIntent.setType("image/*");
                    startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                    break;
                case TAKE_PICTURE: // ����
                    Intent openCameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    		tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
                    // ָ����Ƭ����·����SD������image.jpgΪһ����ʱ�ļ���ÿ�����պ����ͼƬ���ᱻ�滻
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                    startActivityForResult(openCameraIntent, TAKE_PICTURE);
                    break;
                }
            }
        });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // ����������ǿ����õ�
            switch (requestCode) {
            case TAKE_PICTURE:
                startPhotoZoom(tempUri); // ��ʼ��ͼƬ���вü�����
                break;
            case CHOOSE_PICTURE:
                startPhotoZoom(data.getData()); // ��ʼ��ͼƬ���вü�����
                break;
            case CROP_SMALL_PICTURE:
                if (data != null) {
                    setImageToView(data); // �øղ�ѡ��ü��õ���ͼƬ��ʾ�ڽ�����
                }
                break;
            }
        }
    }
    /**
     * �ü�ͼƬ����ʵ��
     * 
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // ���òü�
        intent.putExtra("crop", "true");
        // aspectX aspectY �ǿ�ߵı���
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY �ǲü�ͼƬ���
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * ����ü�֮���ͼƬ����
     * 
     * @param
     * 
     * @param picdata
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            photo = Utils.toRoundBitmap(photo, tempUri); // ���ʱ���ͼƬ�Ѿ��������Բ�ε���
            user_pic.setImageBitmap(photo);
            uploadPic(photo);
        }
    }

    private void uploadPic(Bitmap bitmap) {
        // �ϴ���������
        // ... �����������Bitmapת����file��Ȼ��õ�file��url�����ļ��ϴ�����
        // ע������õ���ͼƬ�Ѿ���Բ��ͼƬ��
        // bitmap��û������Բ�δ���ģ����Ѿ����ü���

        String imagePath = Utils.savePhoto(bitmap, Environment
                .getExternalStorageDirectory().getAbsolutePath(), String
                .valueOf(System.currentTimeMillis()));
        Log.e("imagePath", imagePath+"");
        if(imagePath != null){
            // ����imagePath�ϴ���
            // ...
        }
    }
    
    /** 
     * ���ݵ�ǰ���У�������WheelView����Ϣ 
     */  
    private void updateAreas()  
    {  
        int pCurrent = mCity.getCurrentItem();  
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];  
        String[] areas = mAreaDatasMap.get(mCurrentCityName);  
        if (areas == null)  
        {  
            areas = new String[] { "" };  
        }  
        mArea.setViewAdapter(new ArrayWheelAdapter<String>(this, areas));  
        mArea.setCurrentItem(0);  
    }  
  
    /** 
     * ���ݵ�ǰ��ʡ��������WheelView����Ϣ 
     */  
    private void updateCities()  
    {  
        int pCurrent = mProvince.getCurrentItem();  
        mCurrentProviceName = mProvinceDatas[pCurrent];  
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);  
        if (cities == null)  
        {  
            cities = new String[] { "" };  
        }  
        mCity.setViewAdapter(new ArrayWheelAdapter<String>(this, cities));  
        mCity.setCurrentItem(0);  
        updateAreas();  
    }  
  
    /** 
     * ��������Json������ɺ��ͷ�Json������ڴ� 
     */  
    private void initDatas()  
    {  
        try  
        {  
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");  
            mProvinceDatas = new String[jsonArray.length()];  
            for (int i = 0; i < jsonArray.length(); i++)  
            {  
                JSONObject jsonP = jsonArray.getJSONObject(i);// ÿ��ʡ��json����  
                String province = jsonP.getString("name");// ʡ����  
                mProvinceDatas[i] = province;  
                JSONArray jsonCs = null;  
                try  
                {  
                    /** 
                     * Throws JSONException if the mapping doesn't exist or is 
                     * not a JSONArray. 
                     */  
                    jsonCs = jsonP.getJSONArray("city");  
                } catch (Exception e1)  
                {  
                    continue;  
                }  
                String[] mCitiesDatas = new String[jsonCs.length()];  
                for (int j = 0; j < jsonCs.length(); j++)  
                {  
                    JSONObject jsonCity = jsonCs.getJSONObject(j);  
                    String city = jsonCity.getString("name");// ������  
                    mCitiesDatas[j] = city;  
                    JSONArray jsonAreas = null;  
                    try  
                    {  
                        /** 
                         * Throws JSONException if the mapping doesn't exist or 
                         * is not a JSONArray. 
                         */  
                        jsonAreas = (JSONArray)jsonCity.getJSONArray("area");  
                    } catch (Exception e)  
                    {  
                        continue;  
                    }  
                    
                    String[] mAreasDatas = new String[jsonAreas.length()];// ��ǰ�е�������  
                    for (int k = 0; k < jsonAreas.length(); k++)  
                    {  
                        //String area = jsonAreas.getJSONObject(k).getString("s");// ���������  
                    	//String area = jsonAreas.getJSONObject(k).toString();
                    	String area=jsonAreas.getString(k);
                        mAreasDatas[k] = area;  
                    }  
                    mAreaDatasMap.put(city, mAreasDatas);  
                }  
                mCitisDatasMap.put(province, mCitiesDatas);  
            }  
  
        } catch (JSONException e)  
        {  
            e.printStackTrace();  
        }  
        mJsonObj = null;  
    }  
  
    /** 
     * ��assert�ļ����ж�ȡʡ������json�ļ���Ȼ��ת��Ϊjson���� 
     */  
    private void initJsonData()  
    {  
        try  
        {  
            StringBuffer sb = new StringBuffer();  
            InputStream is = getAssets().open("city.json");  
            int len = -1;  
            byte[] buf = new byte[2048];  
            while ((len = is.read(buf)) != -1)  
            {  
                sb.append(new String(buf, 0, len, "gbk"));  
            }  
            is.close();  
            mJsonObj = new JSONObject(sb.toString());  
        } catch (IOException e)  
        {  
            e.printStackTrace();  
        } catch (JSONException e)  
        {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * change�¼��Ĵ��� 
     */  
    @Override  
    public void onChanged(WheelView wheel, int oldValue, int newValue)  
    {  
        if (wheel == mProvince)  
        {  
            updateCities();  
        } else if (wheel == mCity)  
        {  
            updateAreas();  
        } else if (wheel == mArea)  
        {  
            mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName)[newValue];  
        }  
    }  
  
    public void showChoose(View view)  
    {  
        Toast.makeText(this, mCurrentProviceName + mCurrentCityName + mCurrentAreaName, 1).show();  
    }

}  
