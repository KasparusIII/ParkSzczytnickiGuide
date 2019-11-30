package com.guide.park_szczytnicki;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.guide.park_szczytnicki.Objects.ParkObject;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static com.guide.park_szczytnicki.Objects.Upload.create;

public class FragmentMainMap extends Fragment implements OnMapReadyCallback
{
    private final static float DEFAULT_ZOOM = 15.5f;
    private final static float MAX_ZOOM = 13.f;
    private final static float MIN_ZOOM = 18.f;
    private final static double DEFAULT_LATITUDE  = 51.1124;
    private final static double DEFAULT_LONGITUDE = 17.0814;


    private final static boolean[] DEFAULT_FILTER_CATEGORIES   = new boolean[] {false, false, false};
    private final static String    DEFAULT_FILTER_NAME         = "";
    private final static int       DEFAULT_FILTER_RATE         = -1;
    private final static int       DEFAULT_FILTER_MIN_RATE_NUM = 0;
    private final static int[][]   IMAGES_ID                   = new int[][]
            {
                    {R.drawable.pointer_nature,  R.drawable.pointer_nature_filter},
                    {R.drawable.pointer_history, R.drawable.pointer_history_filter},
                    {R.drawable.pointer_culture, R.drawable.pointer_culture_filter},
            };

    public final static int[] itemsId = new int[] {R.id.button_categories, R.id.button_routs, R.id.button_achievements};
    private List<ParkObject> parkObjects;

    private boolean[] filterCategories;
    private ImageView[] categoryImages;
    private String filterName;
    private int filterRate;
    private ImageButton[] rateButtons;
    private int filterMinRateNum;
    private int filterMaxRateNum;
    private int defaultMaxRatesNumber;
    private AppCompatEditText[] textInputs;

    private DrawerLayout drawer;
    private View navigationObject;
    private SparseArray<ParkObject> hashMarkerMap;
    private ParkObject currentObject;
    private ImageButton[] myRateButtons;
    private int myRate;
    //FragmentActivity activity;
    private GoogleMap googleMap;

    DatabaseReference databaseRootReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference databaseChildReference = databaseRootReference.child("ParkObject");
    StorageReference  storageReference = FirebaseStorage.getInstance().getReference("images");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {    return inflater.inflate(R.layout.fragment_main_map, container, false);    }

    @Override
    public void onStart()
    {
        super.onStart();

        navigationObject = ((NavigationView)getView().findViewById(R.id.navigation_object)).getHeaderView(0);
        ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        drawer = getView().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView)
            {    hideKeyboard(drawerView);     }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        getView().findViewById(R.id.button_filter).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {    drawer.openDrawer(GravityCompat.START);    }
        });

        (navigationObject.findViewById(R.id.button_back)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {    drawer.closeDrawer(GravityCompat.END);    }
        });

        filtersListeners(((NavigationView)getView().findViewById(R.id.navigation_filters)).getHeaderView(0));
        //sendParkObjects();

        databaseChildReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                parkObjects = new LinkedList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    ParkObject parkObject = snapshot.getValue(ParkObject.class);
                    parkObjects.add(parkObject);

                    //todo set as default in layout
                    if (currentObject == null)
                    {
                        String key = snapshot.getKey();
                        if(key != null && key.equals("Park Szczytnicki") && parkObject != null)
                        {
                            setParkObject(parkObject);
                            drawer.openDrawer(GravityCompat.END);
                        }
                    }
                }
                //todo add swiping images https://www.youtube.com/watch?v=zQekzaAgIlQ ,    https://www.youtube.com/watch?v=HtOhZcKVFOY
                //todo load
                defaultMaxRatesNumber = 101;
                setDefaultFilters();
                setMarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {}
        });
    }

    public void setMarkers()
    {
        hashMarkerMap = new SparseArray<>();
        for (ParkObject object : parkObjects)
        {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(object.getLatitude(), object.getLongitude()))
                    .snippet(object.getHeading())
                    .title(object.getName())
                    .icon(BitmapDescriptorFactory.//fromResource(object.getPointerType())));
                            fromBitmap(createCustomMarker(getContext(), object.getPointerType(), object.getName()))));

            object.setMarker(marker);
            hashMarkerMap.put(marker.hashCode(), object);
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                setParkObject(hashMarkerMap.get(marker.hashCode()));
                drawer.openDrawer(GravityCompat.END);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        this.googleMap = googleMap;

        String TAG = "17.0814";

        googleMap.clear();
        googleMap.setMinZoomPreference(MAX_ZOOM);
        googleMap.setMaxZoomPreference(MIN_ZOOM);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), DEFAULT_ZOOM));

        try
        {
            if (!googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getContext()), R.raw.map_style)))
                Log.e(TAG, "Style parsing failed.");
        }   catch (Resources.NotFoundException e)
        {       Log.e(TAG, "Can't find style. Error: ", e);     }

        //setMarkers();
    }
    //TODO
    private ImageView image;
    private void setParkObject(ParkObject parkObject)
    {

        currentObject = parkObject;
        //TODO
        image = navigationObject.findViewById(R.id.image_object);
        Button b = navigationObject.findViewById(R.id.button_correction);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        setRate(navigationObject, parkObject.getRate());
        ((TextView)navigationObject.findViewById(R.id.text_object_title)).setText(parkObject.getName());
        setWebTextJustify((WebView)navigationObject.findViewById(R.id.text_info_content), parkObject.getInfo());
        setWebTextJustify((WebView)navigationObject.findViewById(R.id.text_additional_info_content), parkObject.getAdditionalInfo());

        myRateButtons = new ImageButton[]
                {
                        navigationObject.findViewById(R.id.button_my_rate1),
                        navigationObject.findViewById(R.id.button_my_rate2),
                        navigationObject.findViewById(R.id.button_my_rate3),
                        navigationObject.findViewById(R.id.button_my_rate4),
                        navigationObject.findViewById(R.id.button_my_rate5)
                };
        //TODO muci być zalogowany aby wysłać ocenę
        setStarsImages(currentObject.getMyRate(), myRateButtons);
        OnClickListener ratesFilterListener = new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s  = v.getResources().getResourceName(v.getId());
                int index = s.charAt(s.length() - 1) - '0';
                myRate  = index == myRate ? 0 : index;

                setStarsImages(myRate, myRateButtons);
                if (currentObject.getRateNum() < 50)
                {
                    float rateDif = (float)(myRate - currentObject.getMyRate())/currentObject.getRateNum();
                    if(Math.abs(rateDif) > .1f)
                        setRate(navigationObject, currentObject.getRate() + rateDif);
                }


                //TODO prześlij ocenę do systemu
            }
        };

        for (ImageButton rateButton : myRateButtons)
            rateButton.setOnClickListener(ratesFilterListener);
    }

    public void setStarsImages(int fullStars, ImageButton[] buttonStars)
    {
        for (int i = 0; i < buttonStars.length; i++)
            if (i < fullStars)
                buttonStars[i].setImageResource(R.drawable.ic_filled_star);
            else
                buttonStars[i].setImageResource(R.drawable.ic_empty_star);
    }

    private void setDefaultFilters()
    {
        filterCategories = DEFAULT_FILTER_CATEGORIES.clone();
        filterName       = DEFAULT_FILTER_NAME;
        filterRate       = DEFAULT_FILTER_RATE;
        filterMinRateNum = DEFAULT_FILTER_MIN_RATE_NUM;
        filterMaxRateNum = defaultMaxRatesNumber;
    }


    public void setWebTextJustify(WebView webView, String text)
    {
        if (text == null)
            text = "";

        text =      "<html>\n<body align=\"justify\" style=\"color: #FFFFFF; font-size: 1em;\">\n"
                +   text.replace("\t", "&emsp;").replace("\n", "</p>")
                +   "</body>\n</html>";
        webView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        //webView.loadData(text, "text/html", "us-ascii");
        webView.setBackgroundColor(Color.TRANSPARENT);
    }

    public void setRate(View objectsLayout, float rate)
    {
        ((TextView) objectsLayout.findViewById(R.id.text_rate)).setText(String.format(Locale.UK,"%3.1f", rate));
        int   iRate = (int) rate;
        float fRate = rate % 1.f;
        ImageButton[] rateButtons = new ImageButton[]
                {
                        objectsLayout.findViewById(R.id.button_rate1),
                        objectsLayout.findViewById(R.id.button_rate2),
                        objectsLayout.findViewById(R.id.button_rate3),
                        objectsLayout.findViewById(R.id.button_rate4),
                        objectsLayout.findViewById(R.id.button_rate5)
                };

        for (int i = 0; i < rateButtons.length; i++)
            if (iRate > i)
                rateButtons[i].setImageResource(R.drawable.ic_filled_star);
            else if (i == iRate)
                rateButtons[i].setImageBitmap(mergeBitmaps(R.drawable.ic_filled_star, R.drawable.ic_empty_star, fRate));
            else
                rateButtons[i].setImageResource(R.drawable.ic_empty_star);
    }

    public void/*ArrayList<ParkObject>*/ sendParkObjects()
    {
        //TODO check
        Scanner sc = new Scanner(getResources().openRawResource(R.raw.objects));//.useDelimiter("\n");
        List<ParkObject> parkObjects = create(sc);
        /*
        parkObjects.add(new ParkObject(
                0,
                0,
                "Park Szczytnicki",
                "",
                "\tPark Szczytnicki, jest drugim co do wielkości parkiem Wrocławia, zajmuję powierzchnię około 87.3 hekta, pod względem powierzchniowym wyprzedza go jedynie park Tysiąclecia, który jest o niecałe 3 hektary większy. Park Szczytnicki, położony jest na wschód od Starej Odry, na terenie dawnej wsi Szczytniki, włączonej w obręb miasta w 1868 roku. Park ma charakter krajobrazowy i duże walory kompozycyjne oraz dendrologiczne, około 400 gatunków drzew i krzewów, pod tymi względami jest to najbogatrzy park Wrocławia.\n" +
                        "\n" +
                        "Historia:\n" +
                        "\tW XVI wieku wieś Szczytniki została podzielona na Nowe i Stare Szczytniki, które w XVII wieku zamieniły się w podmiejskie osiedla rezydencjonalne. Las na terenie Starych Szczytnik już w połowie XVIII wieku cieszył się powodzeniem wśród wrocławian. W 1783 roku Fryderyk Ludwik Hohenlohe, komendant garnizonu wrocławskiego, wykupił go i założył tu jeden z pierwszych parków na kontynencie europejskim urządzonych w stylu angielskim.\n" +
                        "\tPark został zdewastowany przez żołnierzy napoleońskich podczas oblężenia miasta w grudniu 1806 roku. Po wojnie większość szkód naprawiono. W 1833 roku w parku Szczytnickim odbyły się pierwsze sportowe wyścigi konne we Wrocławiu. Wyścigi organizowano aż do roku 1907, na terenach przylegających do obecnej Hali Stulecia.\n" +
                        "\tW parku znajduje się Ogród Japoński założony w latach 1909–1912, w związku z Wystawą Stulecia z 1913 roku, z inicjatywy hrabiego Fritza von Hochberga, i zaprojektowany przez japońskiego ogrodnika Mankichiego Araia. Po wystawie zabrano jednak większość z wypożyczonych detali decydujących o japońskim charakterze ogrodu. W 1994 roku przy współpracy ambasady japońskiej, prof. Ikuya Nishikawy i ogrodników z Nagoi rozpoczęto prace przywracające ogrodowi japoński charakter. Współcześnie jest to już Ogród Japoński nie tylko z nazwy. Stanowi unikatowy w Europie żywy fragment japońskiej kultury. Rząd Japonii przekazał do Ogrodu kilka granitowych latarń z XIX wieku. Japończycy nazwali ogród Hakkoen, tzn. Ogród białoczerwony.",
                ParkObject.POINTER_OTHER,
                3.9f,
                101,
                "",
                5));

        parkObjects.add(new ParkObject(
                51.112,
                17.083,
                "Fontanna multimedialna",
                "Największa fontanna w polsce o powierzchni 1ha",
                "Położona w malowniczym parku Szczytnickim, otoczona pergolą i w bezpośrednim sąsiedztwie Hali Stulecia, multimedialna wrocławska fontanna jest największym tego typu obiektem w Polsce i jednym z największych w Europie. Jej powierzchnia wynosi ok. 1 hektara. W fontannie zainstalowano prawie 300 dysz, z których tryska woda w formie gejzerów, mgiełki, piany itp. na wysokość nawet 40 m. Tym sposobem tworzy się ogromny ekran wodny, na którym wyświetlane są wizualizacje z towarzyszeniem muzyki i efektów laserowych.",
                ParkObject.POINTER_CULTURE,
                3.9f,
                12,
                "<a href=\"https://pik.wroclaw.pl/wroclawska-fontanna-multimedialna-pergola-pokazy-specjalne\">Pokazy Specjalne</a>",
                1));
        //*/

        Map<String, ParkObject> parkObjectMap = new HashMap<>();
        for (ParkObject parkObject :  parkObjects)
            parkObjectMap.put(parkObject.getName(), parkObject);
        databaseChildReference.setValue(parkObjectMap);
    }

    private static final int REQUEST_IMAGE = 1;
    private Uri uriImage;
    public void openImageChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE);
        //startActivityForResult(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE//&& resultCode == RESULT_OK
            && data != null && data.getData() != null)
        {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getContext()).getContentResolver(), data.getData());
                //image.setImageBitmap();
                image = Bitmap.createScaledBitmap(image, 1920/2, 1080/2, false);
                uploadImage(image);
            } catch (IOException e) {e.printStackTrace();}

        }
    }

    public void uploadImage(Bitmap image)
    {
        if(image != null)
        {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpeg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 90, baos);

            UploadTask uploadTask = fileRef.putBytes(baos.toByteArray());
            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                }
            });
        }
    }

    public void filtersListeners(final View filtersLayout)
    {
        //filtersLayout.setFocusedByDefault();
        //TextInputLayout l  = filtersLayout.findViewById(R.id.input_maxRatings);
        //l.setHint(""+defaultMaxRatesNumber);

        categoryImages = new ImageView[]
                {
                        filtersLayout.findViewById(R.id.image_nature1),
                        filtersLayout.findViewById(R.id.image_history2),
                        filtersLayout.findViewById(R.id.image_culture3)
                };

        OnClickListener categoriesFilterListener = new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = v.getResources().getResourceName(v.getId());
                int index = s.charAt(s.length() - 1) - '1';
                filterCategories[index] = !filterCategories[index];

                if (filterCategories[index])
                    categoryImages[index].setImageResource(IMAGES_ID[index][1]);
                else
                    categoryImages[index].setImageResource(IMAGES_ID[index][0]);

                upDateFilters();
                hideKeyboard(filtersLayout);
            }
        };

        for (ImageView categoryImage : categoryImages)
            categoryImage.setOnClickListener(categoriesFilterListener);

        rateButtons = new ImageButton[]
                {
                        filtersLayout.findViewById(R.id.button_rate1),
                        filtersLayout.findViewById(R.id.button_rate2),
                        filtersLayout.findViewById(R.id.button_rate3),
                        filtersLayout.findViewById(R.id.button_rate4),
                        filtersLayout.findViewById(R.id.button_rate5)
                };

        OnClickListener ratesFilterListener = new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = v.getResources().getResourceName(v.getId());
                int index = s.charAt(s.length() - 1) - '0';
                filterRate = index == filterRate ? 0 : index;

                setStarsImages(filterRate, rateButtons);
                upDateFilters();
                hideKeyboard(filtersLayout);
            }
        };

        for (ImageButton rateButton : rateButtons)
            rateButton.setOnClickListener(ratesFilterListener);

        textInputs = new AppCompatEditText[]
                {
                        filtersLayout.findViewById(R.id.input_search),
                        filtersLayout.findViewById(R.id.input_minRatings),
                        filtersLayout.findViewById(R.id.input_maxRatings)
                };

        textInputs[0].addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                filterName = s.toString().toLowerCase();
                upDateFilters();
            }
        });


        textInputs[1].addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length() != 0)
                    filterMinRateNum = Integer.parseInt(s.toString());
                else
                    filterMinRateNum = DEFAULT_FILTER_MIN_RATE_NUM;
                upDateFilters();
            }
        });

        textInputs[2].addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length() != 0)
                    filterMaxRateNum = Integer.parseInt(s.toString());
                else
                    filterMaxRateNum = defaultMaxRatesNumber;
                upDateFilters();
            }
        });

        (filtersLayout.findViewById(R.id.text_my_rate)).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for (ImageButton rateButton : rateButtons)
                    rateButton.setImageResource(R.drawable.ic_empty_star);

                for (int i = 0; i < categoryImages.length; i++)
                    categoryImages[i].setImageResource(IMAGES_ID[i][0]);

                for (AppCompatEditText textInput : textInputs)
                    textInput.setText("");

                setDefaultFilters();
                upDateFilters();
            }
        });
    }

    public void hideKeyboard(View v)
    {
        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void upDateFilters()
    {
        for (ParkObject o : parkObjects)
            o.filterMarker(filterCategories, filterName, filterRate, filterMinRateNum, filterMaxRateNum);
    }

    public Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name)
    {
        /*
            View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);

            ImageView markerImage = marker.findViewById(R.id.user_dp);
            //markerImage.setColorFilter(R.color.white);
            markerImage.setImageResource(resource);
            TextView txt_name = marker.findViewById(R.id.name);
            txt_name.setText(_name);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            marker.setLayoutParams(new ViewGroup.LayoutParams(152, ViewGroup.LayoutParams.WRAP_CONTENT));
            marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            marker.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            marker.draw(canvas);
        */

        //bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
        //profileImage.setImageBitmap(Bitmap.createScaledBitmap(b, 120, 120, false));
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), resource), 38, 60, false);
    }

    public Bitmap pieceBitmaps(int leftDrawableId, float partOfLeft)
    {
        Bitmap bitmapTopLeft = BitmapFactory.decodeResource(this.getResources(), leftDrawableId);
        Bitmap bitmapMarriage = Bitmap.createBitmap(bitmapTopLeft.getWidth(), bitmapTopLeft.getHeight(), bitmapTopLeft.getConfig());

        bitmapTopLeft = Bitmap.createBitmap(bitmapTopLeft, 0, 0, Math.round(bitmapTopLeft.getWidth()*partOfLeft), bitmapTopLeft.getHeight());

        Canvas canvas = new Canvas(bitmapMarriage);
        canvas.drawBitmap(bitmapTopLeft, 0, 0, null);
        return bitmapMarriage;
    }

    public Bitmap mergeBitmaps(int leftDrawableId, int rightDrawableId, float partOfLeft)
    {
        Bitmap bitmapTopLeft = BitmapFactory.decodeResource(getResources(), leftDrawableId);
        bitmapTopLeft = Bitmap.createBitmap(bitmapTopLeft, 0, 0, Math.round(bitmapTopLeft.getWidth()*partOfLeft), bitmapTopLeft.getHeight());

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;

        Bitmap bitmapMarriage = BitmapFactory.decodeResource(getResources(), rightDrawableId, opt);
        Canvas canvas = new Canvas(bitmapMarriage);
        canvas.drawBitmap(bitmapTopLeft, 0, 0, null);
        return bitmapMarriage;
    }
}