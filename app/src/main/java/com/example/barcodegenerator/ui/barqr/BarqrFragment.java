package com.example.barcodegenerator.ui.barqr;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class BarqrFragment extends Fragment {

    private FragmentBarqrBinding binding;
    private ImageView qrcodeImgView;
    private int barcodeWidth = 512;
    private int barcodeHeight = 512;
    private String pathToSave = "BarcodeGenerator";
    private String nameToSave = "Image";
    private Bitmap imageBitmap = null;
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
        qrcodeImgView = binding.qrcodeImage;
        qrcodeImgView.getLayoutParams().width = barcodeWidth;
        qrcodeImgView.getLayoutParams().height = barcodeHeight;

        String[] values = {"QRCODE", "CODE_128"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        binding.settBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                showSettDialog();
            }
        });

        binding.generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrcodeTxt = String.valueOf(binding.textToCode.getText());
                if(spinner.getSelectedItemId() == 0){
                    generateBarcodeQRCODE(qrcodeImgView, qrcodeTxt);
                }
                else if(spinner.getSelectedItemId() == 1){
                    generateBarcodeCODE128(qrcodeImgView, qrcodeTxt);
                }
                hideKeyboard();
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

        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageBitmap != null){
                    shareImageUri(saveImage(imageBitmap));
                }
                else{
                    Toast.makeText(getActivity(), "Cannot share image!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.textToCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                copyToClipboard(String.valueOf(binding.textToCode.getText()));

                return false;
            }
        });
    }

    private Uri saveImage(Bitmap image) {
        File imagesFolder = new File(getContext().getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "share.png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(getContext(), "com.mydomain.fileprovider", file);

        } catch (IOException e) {
            Log.e("ERROR", "IOException while trying to write file for sharing: " + e.getMessage());
            Toast.makeText(getContext(), "Error while sharing image", Toast.LENGTH_LONG).show();
        }
        return uri;
    }

    private void shareImageUri(Uri uri){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivity(intent);
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
                imageBitmap = bitmap;
                binding.shareBtn.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(getActivity(), "Input cannot be empty!",
                    Toast.LENGTH_LONG).show();
            imageView.setImageBitmap(null);
            imageBitmap = null;
            binding.shareBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void generateBarcodeCODE128(ImageView imageView, String text) {
        if(!text.equals("")) {
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
                imageBitmap = bitmap;
                binding.shareBtn.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(getActivity(), "Input cannot be empty!",
                    Toast.LENGTH_LONG).show();
            imageView.setImageBitmap(null);
            imageBitmap = null;
            binding.shareBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void saveImageToGallery(Context context, Bitmap bitmap) {
        String fileName = nameToSave + "_" + System.currentTimeMillis() + ".png";

        String customDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + pathToSave;
        File customDirectory = new File(customDirectoryPath);

        if (!customDirectory.exists()) {
            customDirectory.mkdirs();
        }

        File imageFile = new File(customDirectory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            MediaScannerConnection.scanFile(context,
                    new String[]{imageFile.getAbsolutePath()},
                    new String[]{"image/png"}, null);

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

        //match parent width seekbar
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // width
        LinearLayout widthLayout = new LinearLayout(requireContext());
        widthLayout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView labelWidth = new TextView(requireContext());
        labelWidth.setText("Width:");
        widthLayout.addView(labelWidth);

        final TextView currentWidth = new TextView(requireContext());
        currentWidth.setText(String.valueOf(barcodeWidth) + "px");
        widthLayout.addView(currentWidth);

        final SeekBar widthSB = new SeekBar(requireContext());
        widthSB.setMax(1024);
        widthSB.setMin(100);
        widthSB.setLayoutParams(layoutParams);
        widthSB.setProgress(barcodeWidth);
        widthSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentWidth.setText(String.valueOf(progress) + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        widthLayout.addView(widthSB);

        layout.addView(widthLayout);

        // height
        LinearLayout heightLayout = new LinearLayout(requireContext());
        heightLayout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView labelHeight = new TextView(requireContext());
        labelHeight.setText("Height:");
        heightLayout.addView(labelHeight);

        final TextView currentHeight = new TextView(requireContext());
        currentHeight.setText(String.valueOf(barcodeHeight) + "px");
        heightLayout.addView(currentHeight);

        final SeekBar heightSB = new SeekBar(requireContext());
        heightSB.setMax(1024);
        heightSB.setMin(100);
        heightSB.setLayoutParams(layoutParams);
        heightSB.setProgress(barcodeHeight);
        heightSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentHeight.setText(String.valueOf(progress) + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        heightLayout.addView(heightSB);

        layout.addView(heightLayout);

        // path
        LinearLayout pathLayout = new LinearLayout(requireContext());
        pathLayout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView dirLabel = new TextView(requireContext());
        dirLabel.setText("Directory:");
        pathLayout.addView(dirLabel);

        final EditText dirET = new EditText(requireContext());
        dirET.setHint(pathToSave);
        pathLayout.addView(dirET);

        layout.addView(pathLayout);

        // name
        LinearLayout nameLayout = new LinearLayout(requireContext());
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView nameLabel = new TextView(requireContext());
        nameLabel.setText("Name:");
        nameLayout.addView(nameLabel);

        final Spinner nameSpin = new Spinner(requireContext());
        String[] values = {"default", "custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nameSpin.setAdapter(adapter);
        nameLayout.addView(nameSpin);

        layout.addView(nameLayout);

        final EditText nameET = new EditText(requireContext());
        nameET.setHint("custom name");
        nameSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    layout.addView(nameET);
                }
                else{
                    layout.removeView(nameET);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setView(layout);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int w = widthSB.getProgress();
                        int h = heightSB.getProgress();
                        changeSize(w,h);
                        String dir = String.valueOf(dirET.getText());
                        boolean custom = true;
                        if(nameSpin.getSelectedItemId() == 1){
                            String name = String.valueOf(nameET.getText());
                            custom = changeName(name);
                        }
                        hideKeyboard();
                        if(changePath(dir) && custom){
                            Toast.makeText(requireContext(), "All settings changed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(requireContext(), "Inputs should be letters", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard();
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
    private boolean changePath(String dir){
        String regex = "^[a-zA-Z]+$";
        if(dir.matches(regex)){
            pathToSave = dir;
            return true;
        }
        return false;
    }

    private boolean changeName(String nm){
        String regex = "^[a-zA-Z]+$";
        if(nm.matches(regex)){
            nameToSave = nm;
            return true;
        }
        return false;
    }

    public void hideKeyboard() {
        View view = getView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void copyToClipboard(String text) {
        Context context = getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show();
    }

}