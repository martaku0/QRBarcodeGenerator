package com.example.barcodegenerator.ui.barqr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.barcodegenerator.databinding.FragmentBarqrBinding;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class BarqrFragment extends Fragment {

    private FragmentBarqrBinding binding;

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
        String[] values = {"QRCODE", "EAN_13"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        binding.generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrcodeTxt = String.valueOf(binding.textToCode.getText());
                ImageView qrcodeImgView = binding.qrcodeImage;
                if(spinner.getSelectedItemId() == 0){
                    generateBarcode(qrcodeImgView, qrcodeTxt);
                }
                else if(spinner.getSelectedItemId() == 1){
                    generateBarcodeEAN(qrcodeImgView, qrcodeTxt);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void generateBarcode(ImageView imageView, String text) {
        if(!text.equals("")) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
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

    private void generateBarcodeEAN(ImageView imageView, String text) {
        if(text.length() == 13 || text.length() == 12) {
            if((text.length() == 13 && isChecksumDigitCorrect(text)) || text.length() == 12){
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.EAN_13, 300, 100);
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
                Toast.makeText(getActivity(), "Wrong checksum!",
                        Toast.LENGTH_LONG).show();
                        imageView.setImageBitmap(null);
            }
        }
        else{
            Toast.makeText(getActivity(), "Input should be 12 or 13 char length!",
                    Toast.LENGTH_LONG).show();
                    imageView.setImageBitmap(null);
        }
    }

    private static boolean isChecksumDigitCorrect(String barcode) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += i % 2 == 0 ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        if(checksum == Character.getNumericValue(barcode.charAt(12))){
            return true;
        }
        else{
            return false;
        }
    }
}