package com.example.barcodegenerator.ui.barqr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.barcodegenerator.MainActivity;
import com.example.barcodegenerator.R;
import com.example.barcodegenerator.databinding.FragmentBarqrBinding;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class BarqrFragment extends Fragment {

    private FragmentBarqrBinding binding;
    private ImageView qrcodeImgView;
    private int barcodeWidth = 500;
    private int barcodeHeight = 500;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BarqrViewModel barqrViewModel =
                new ViewModelProvider(this).get(BarqrViewModel.class);

        binding = FragmentBarqrBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = binding.spinner;
        String[] values = {"QRCODE", "CODE_128"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        binding.settBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettDialog();
            }
        });

        binding.generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrcodeTxt = String.valueOf(binding.textToCode.getText());
                qrcodeImgView = binding.qrcodeImage;
                qrcodeImgView.getLayoutParams().width = barcodeWidth;
                qrcodeImgView.getLayoutParams().height = barcodeHeight;
                if(spinner.getSelectedItemId() == 0){
                    generateBarcodeQRCODE(qrcodeImgView, qrcodeTxt);
                }
                else if(spinner.getSelectedItemId() == 1){
                    generateBarcodeCODE128(qrcodeImgView, qrcodeTxt);
                }
            }
        });

        binding.qrcodeImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) binding.qrcodeImage.getDrawable();
                if(drawable != null) {
                    Bitmap bitmap = drawable.getBitmap();
                    saveImageToGallery(getContext(), bitmap);
                    return true; // indicate that the long click event is consumed
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void generateBarcodeQRCODE(ImageView imageView, String text) {
        if(!text.equals("")) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, barcodeWidth, barcodeHeight);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                imageView.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(getActivity(), "Input cannot be empty!",
                    Toast.LENGTH_LONG).show();
            imageView.setImageBitmap(null);
        }
    }

    private void generateBarcodeCODE128(ImageView imageView, String text) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.CODE_128, barcodeWidth, barcodeHeight);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToGallery(Context context, Bitmap bitmap) {
        String fileName = "image_" + System.currentTimeMillis() + ".png";
//        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //FIXME: custom dir
        String customDirectoryPath = Environment.getExternalStorageDirectory() + "/custom";
        File customDirectory = new File(customDirectoryPath);
        // Create the directory if it doesn't exist
        if (!customDirectory.exists()) {
            customDirectory.mkdirs();
            Log.d("TAG", "saveImageToGallery: ");
        }
        File storageDirectory = customDirectory;
        //ENDFIXME

        File imageFile = new File(storageDirectory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), imageFile.getAbsolutePath(), fileName, null);

            Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("SETTINGS")
                .setMessage("Change barcode settings:");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // width
        final EditText widthEditText = new EditText(requireContext());
        widthEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        widthEditText.setHint("width [current:" + barcodeWidth + "]");
        layout.addView(widthEditText);

        // height
        final EditText heightEditText = new EditText(requireContext());
        heightEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        heightEditText.setHint("height [current:" + barcodeHeight + "]");
        layout.addView(heightEditText);

        builder.setView(layout);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String widthInp = widthEditText.getText().toString();
                        String heightInp = heightEditText.getText().toString();
                        if(!TextUtils.isEmpty(widthInp) && !TextUtils.isEmpty(heightInp)) {
                            try{
                                int w = Integer.parseInt(widthInp);
                                int h = Integer.parseInt(heightInp);
                                changeSize(w,h);
                            }
                            catch (Exception ex){
                                Log.e("Size settings", "Error: " + ex);
                            }
                        }

                        Toast.makeText(requireContext(), "Settings changed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeSize(int w, int h){
        barcodeWidth = w;
        barcodeHeight = h;
        qrcodeImgView.getLayoutParams().width = w;
        qrcodeImgView.getLayoutParams().height = h;
    }
}