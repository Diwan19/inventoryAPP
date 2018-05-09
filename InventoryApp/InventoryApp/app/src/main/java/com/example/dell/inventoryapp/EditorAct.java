package com.example.dell.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dell.inventoryapp.data.ProdContract.ProductEntry;

import static android.view.View.GONE;


public class EditorAct extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int getPRODUCT_IMAGE = 1;
    private static int PROD_LOADER = 1;
    String product_image = null;
    Uri imageUri;
    private EditText NameEditTxt;
    private EditText PriceEdittxt;
    private EditText QuantEdittxt;
    private EditText CodeEditTxt;
    private ImageView ImageVi;
    private boolean hasProductChanged = false;
    private Uri currentProductUri = null;
    private View.OnTouchListener TouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityedit);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        Button deleteButton = (Button) findViewById(R.id.delete_button);

        if (currentProductUri == null) {
            imageUri = Uri.parse("android.resource://" + this.getPackageName() + "/drawable/pic");
            setTitle(getString(R.string.editor_activity_title_new_product));
            deleteButton.setVisibility(GONE);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(PROD_LOADER, null, this);
        }

        NameEditTxt = (EditText) findViewById(R.id.edit_prod_name);
        PriceEdittxt = (EditText) findViewById(R.id.edit_product_price);
        QuantEdittxt = (EditText) findViewById(R.id.quantity_edit);
        CodeEditTxt = (EditText) findViewById(R.id.editproduct_code);
        ImageVi = (ImageView) findViewById(R.id.image_view);

        NameEditTxt.setOnTouchListener(TouchListener);
        PriceEdittxt.setOnTouchListener(TouchListener);
        QuantEdittxt.setOnTouchListener(TouchListener);
        CodeEditTxt.setOnTouchListener(TouchListener);

        Button addImageButton = (Button) findViewById(R.id.image_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Select Image From"), getPRODUCT_IMAGE);
                }
            }
        });

        Button saveDataButton = (Button) findViewById(R.id.save_button);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
                finish();
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        Button addButton = (Button) findViewById(R.id.increase_quantity);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int productQuantity;
                String orderQuantity = QuantEdittxt.getText().toString();
                if (TextUtils.isEmpty(orderQuantity)) {
                    QuantEdittxt.setText("0");
                }
                productQuantity = Integer.parseInt(QuantEdittxt.getText().toString());
                productQuantity++;
                QuantEdittxt.setText("" + productQuantity);
            }
        });

        Button subButton = (Button) findViewById(R.id.decrease_quantity);
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int productQuantity;
                String orderQuantity = QuantEdittxt.getText().toString();
                if (TextUtils.isEmpty(orderQuantity)) {
                    QuantEdittxt.setText("0");
                }
                productQuantity = Integer.parseInt(QuantEdittxt.getText().toString());
                if (productQuantity <= 0) {
                    Toast.makeText(EditorAct.this, " Buy some product", Toast.LENGTH_SHORT).show();
                } else {
                    productQuantity--;
                }
                QuantEdittxt.setText("" + productQuantity);
            }
        });
        final Button order = (Button) findViewById(R.id.order_button);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orderName = NameEditTxt.getText().toString();
                String orderQuantity = QuantEdittxt.getText().toString();
                Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
                orderIntent.setData(Uri.parse("mailto:himanshu.diwan1902@gmail.com"));
                orderIntent.putExtra(Intent.EXTRA_TEXT, "order quantity " + orderQuantity + "order name" + orderName);
                startActivity(orderIntent);
            }
        });

    }

    // onactivity method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == getPRODUCT_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            product_image = imageUri.toString();
            ImageVi.setImageURI(imageUri);
        }
    }



    @Override
    public void onBackPressed() {

        if (!hasProductChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME, ProductEntry.COLUMN_PRODUCT_PRICE, ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductEntry.COLUMN_PRODUCT_IMAGE, ProductEntry.COLUMN_PRODUCT_CODE};
        return new CursorLoader(this, currentProductUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {

            int productNameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productMFgDateColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CODE);
            int productImageColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String productName = data.getString(productNameColumnIndex);
            int productPrice = data.getInt(productPriceColumnIndex);
            int productQuantity = data.getInt(productQuantityColumnIndex);
            String productMfgDate = data.getString(productMFgDateColumnIndex);
            String productImage = data.getString(productImageColumnIndex);

            NameEditTxt.setText(productName);
            PriceEdittxt.setText(Integer.toString(productPrice));
            QuantEdittxt.setText(Integer.toString(productQuantity));
            CodeEditTxt.setText(productMfgDate);
            imageUri = Uri.parse(productImage);
            ImageVi = (ImageView) findViewById(R.id.image_view);
            ImageVi.setImageURI(imageUri);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        NameEditTxt.setText("");
        PriceEdittxt.setText("");
        QuantEdittxt.setText("");
        CodeEditTxt.setText("");
        ImageVi.setImageResource(R.drawable.inventory);
    }

    private void saveProduct() {

        String productName = NameEditTxt.getText().toString().trim();
        String productPrice = PriceEdittxt.getText().toString().trim();
        String productQuantity = QuantEdittxt.getText().toString().trim();
        String productMfgDate = CodeEditTxt.getText().toString().trim();
        String productImage = imageUri.toString();

        if (currentProductUri == null &&
                TextUtils.isEmpty(productName) || TextUtils.isEmpty(productPrice) ||
                TextUtils.isEmpty(productQuantity) || TextUtils.isEmpty(productImage) || TextUtils.isEmpty(productMfgDate)) {
            Toast.makeText(EditorAct.this, "Fill Details", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentVal = new ContentValues();
        contentVal.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        int price = 0;
        if (!TextUtils.isEmpty(productPrice)) {
            price = Integer.parseInt(productPrice);
        }
        contentVal.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        int quantity = 0;
        if (!TextUtils.isEmpty(productQuantity)) {
            quantity = Integer.parseInt(productQuantity);
        }
        contentVal.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        contentVal.put(ProductEntry.COLUMN_PRODUCT_IMAGE, productImage);
        contentVal.put(ProductEntry.COLUMN_PRODUCT_CODE, productMfgDate);

        if (currentProductUri == null) {

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentVal);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, contentVal, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_productFailed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.Update_productsuccessful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deletedialogmsg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if (currentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            if (rowsDeleted == 0) {

                Toast.makeText(this, getString(R.string.delete_productfailed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.delete_productsuccessful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsavedchanges_dialogmsg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
