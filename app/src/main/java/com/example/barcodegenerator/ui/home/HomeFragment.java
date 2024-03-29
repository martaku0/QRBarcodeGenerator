package com.example.barcodegenerator.ui.home;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.pm.ActivityInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.barcodegenerator.MyCaptureActivity;
import com.example.barcodegenerator.databinding.FragmentHomeBinding;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.File;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private TextView resultText;
    private Button scanBtn;
    private ImageView resultImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        resultText = binding.resultText;
        scanBtn = binding.scanBtn;
        resultImage = binding.resultImage;

//        askPermission();

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermission();
            }
        });

        resultText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard((String) resultText.getText());
                return false;
            }
        });

        return root;
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                initQRCodeScanner();
            }
        } else {
            initQRCodeScanner();
        }
    }


    private void initQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(requireActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE, IntentIntegrator.CODE_128);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setBarcodeImageEnabled(true);
        integrator.setPrompt("");
        qrCodeScannerLauncher.launch(integrator.createScanIntent());
    }
    private final ActivityResultLauncher<Intent> qrCodeScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult scanningResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (scanningResult != null) {
                    if (scanningResult.getContents() == null) {
                        Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        resultText.setText(scanningResult.getContents());
                        if(scanningResult.getBarcodeImagePath() != null)
                        {
                            File image = new File(scanningResult.getBarcodeImagePath());
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                            bitmap = Bitmap.createScaledBitmap(bitmap,resultImage.getWidth(),resultImage.getHeight(),true);
                            resultImage.setImageBitmap(bitmap);
                        }
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    initQRCodeScanner();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void copyToClipboard(String text) {
        Context context = getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show();
    }
}
