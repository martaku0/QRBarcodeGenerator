package com.example.barcodegenerator.ui.barqr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

        binding.generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrcodeTxt = String.valueOf(binding.textToCode.getText());
                ImageView qrcodeImgView = binding.qrcodeImage;
                generateBarcode(qrcodeImgView, qrcodeTxt);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void generateBarcode(ImageView imageView, String text) {
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
    }
}